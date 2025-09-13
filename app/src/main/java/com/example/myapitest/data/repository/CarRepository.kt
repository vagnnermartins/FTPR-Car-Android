package com.example.myapitest.data.repository

import com.example.myapitest.data.api.ApiClient
import com.example.myapitest.domain.model.Car
import com.example.myapitest.domain.model.CarResponse
import com.example.myapitest.domain.model.DeleteResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class CarRepository {
    private val apiService = ApiClient.carApiService

    companion object {
        private fun getErrorMessage(exception: Exception): String {
            return when (exception) {
                is UnknownHostException -> "Sem conexão com a internet. Verifique sua rede."
                is SocketTimeoutException -> "Tempo limite esgotado. Tente novamente."
                is SSLException -> "Erro de segurança na conexão."
                is IOException -> "Erro de rede. Verifique sua conexão."
                is HttpException -> {
                    when (exception.code()) {
                        400 -> "Dados inválidos enviados."
                        401 -> "Não autorizado. Faça login novamente."
                        403 -> "Acesso negado."
                        404 -> "Recurso não encontrado."
                        409 -> "Conflito. Esse recurso já existe."
                        422 -> "Dados não puderam ser processados."
                        500 -> "Erro interno do servidor."
                        502 -> "Servidor indisponível."
                        503 -> "Serviço temporariamente indisponível."
                        else -> "Erro HTTP ${exception.code()}: ${exception.message()}"
                    }
                }

                else -> exception.message ?: "Erro desconhecido"
            }
        }

        private fun getHttpErrorMessage(code: Int, message: String): String {
            return when (code) {
                400 -> "Dados inválidos. Verifique as informações enviadas."
                401 -> "Não autorizado. Faça login novamente."
                403 -> "Acesso negado. Você não tem permissão."
                404 -> "Recurso não encontrado."
                409 -> "Conflito. Este recurso já existe."
                422 -> "Dados não puderam ser processados. Verifique os campos."
                500 -> "Erro interno do servidor. Tente novamente mais tarde."
                502 -> "Servidor indisponível. Tente novamente."
                503 -> "Serviço temporariamente indisponível."
                else -> "Erro HTTP $code: $message"
            }
        }
    }

    suspend fun getCars(): Result<List<Car>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCars()
                if (response.isSuccessful) {
                    val cars = response.body()
                    if (cars != null) {
                        Result.success(cars)
                    } else {
                        Result.failure(Exception("Resposta vazia do servidor"))
                    }
                } else {
                    val errorMsg = getHttpErrorMessage(response.code(), response.message())
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                val errorMsg = getErrorMessage(e)
                Result.failure(Exception(errorMsg))
            }
        }
    }

    suspend fun getCarById(id: String): Result<CarResponse> {
        return withContext(Dispatchers.IO) {
            try {
                if (id.isBlank()) {
                    return@withContext Result.failure(Exception("ID do carro é obrigatório"))
                }

                if (!id.matches(Regex("^[0-9]+$"))) {
                    return@withContext Result.failure(Exception("ID deve conter apenas números"))
                }

                val response = apiService.getCarById(id)
                if (response.isSuccessful) {
                    val carResponse = response.body()
                    if (carResponse != null) {
                        Result.success(carResponse)
                    } else {
                        Result.failure(Exception("Carro com ID $id não foi encontrado"))
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        404 -> "Carro com ID $id não foi encontrado"
                        else -> getHttpErrorMessage(response.code(), response.message())
                    }
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                val errorMsg = getErrorMessage(e)
                Result.failure(Exception(errorMsg))
            }
        }
    }

    suspend fun deleteCar(id: String): Result<DeleteResponse> {
        return withContext(Dispatchers.IO) {
            try {
                if (id.isBlank()) {
                    return@withContext Result.failure(Exception("ID do carro é obrigatório"))
                }

                if (!id.matches(Regex("^[0-9]+$"))) {
                    return@withContext Result.failure(Exception("ID deve conter apenas números"))
                }

                val response = apiService.deleteCar(id)
                if (response.isSuccessful) {
                    val deleteResponse = response.body()
                        ?: DeleteResponse("Carro deletado com sucesso!")
                    Result.success(deleteResponse)
                } else {
                    val errorMsg = when (response.code()) {
                        404 -> "Carro com ID $id não foi encontrado para exclusão"
                        409 -> "Não é possível deletar este carro. Pode estar em uso."
                        else -> getHttpErrorMessage(response.code(), response.message())
                    }
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                val errorMsg = getErrorMessage(e)
                Result.failure(Exception(errorMsg))
            }
        }
    }

    suspend fun addCar(car: Car): Result<Car> {
        return withContext(Dispatchers.IO) {
            try {
                val validationError = validateCarData(car)
                if (validationError != null) {
                    return@withContext Result.failure(Exception(validationError))
                }

                val response = apiService.addCar(car)
                if (response.isSuccessful) {
                    val addedCar = response.body()
                    if (addedCar != null) {
                        Result.success(addedCar)
                    } else {
                        Result.failure(Exception("Resposta vazia do servidor ao adicionar carro"))
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        409 -> "Já existe um carro com o ID ${car.id}"
                        422 -> "Dados do carro são inválidos. Verifique todos os campos."
                        else -> getHttpErrorMessage(response.code(), response.message())
                    }
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                val errorMsg = getErrorMessage(e)
                Result.failure(Exception(errorMsg))
            }
        }
    }

    suspend fun updateCar(id: String, car: Car): Result<Car> {
        return withContext(Dispatchers.IO) {
            try {
                if (id.isBlank()) {
                    return@withContext Result.failure(Exception("ID do carro é obrigatório"))
                }

                if (!id.matches(Regex("^[0-9]+$"))) {
                    return@withContext Result.failure(Exception("ID deve conter apenas números"))
                }

                val validationError = validateCarData(car)
                if (validationError != null) {
                    return@withContext Result.failure(Exception(validationError))
                }

                val response = apiService.updateCar(id, car)
                if (response.isSuccessful) {
                    val updatedCar = response.body()
                    if (updatedCar != null) {
                        Result.success(updatedCar)
                    } else {
                        Result.failure(Exception("Resposta vazia do servidor ao atualizar carro"))
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        404 -> "Carro com ID $id não foi encontrado para atualização"
                        422 -> "Dados do carro são inválidos. Verifique todos os campos."
                        else -> getHttpErrorMessage(response.code(), response.message())
                    }
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                val errorMsg = getErrorMessage(e)
                Result.failure(Exception(errorMsg))
            }
        }
    }

    private fun validateCarData(car: Car): String? {
        return when {
            car.id.isBlank() -> "ID do carro é obrigatório"
            !car.id.matches(Regex("^[0-9]+$")) -> "ID deve conter apenas números"
            car.name.isBlank() -> "Nome do carro é obrigatório"
            car.name.length < 2 -> "Nome deve ter pelo menos 2 caracteres"
            car.name.length > 50 -> "Nome não pode ter mais de 50 caracteres"
            car.year.isBlank() -> "Ano é obrigatório"
            car.licence.isBlank() -> "Placa é obrigatória"
            !car.licence.matches(Regex("^[A-Z]{3}-[0-9]{4}$")) -> "Placa deve estar no formato ABC-1234"
            car.imageUrl.isBlank() -> "URL da imagem é obrigatória"
            car.place.lat == 0.0 && car.place.long == 0.0 -> "Localização é obrigatória"
            else -> null
        }
    }
}