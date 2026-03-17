package com.example.myapitest.data

import android.content.Context
import com.example.myapitest.R
import com.example.myapitest.data.model.Car
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// RZ - Resultado detalhado do Backup para feedback na UI
data class BackupResult(val success: Boolean, val message: String, val collectionPath: String = "")

object FirestoreManager {

    // RZ - Função para obter a instância correta do banco de dados baseada no nome configurado no XML.
    // Isso resolve o erro "database (default) does not exist" quando o usuário cria um banco com nome personalizado.
    private fun getDbInstance(context: Context): FirebaseFirestore {
        val dbName = context.getString(R.string.FirestoreDatabaseName)
        return if (dbName == "(default)") {
            FirebaseFirestore.getInstance()
        } else {
            // RZ - Conecta especificamente ao banco nomeado no console (ex: rzcarapp)
            Firebase.firestore(dbName)
        }
    }

    // RZ - Backup detalhado com callback para atualizar o progresso na UI
    suspend fun backupAllToFirestoreDetailed(
        context: Context, 
        cars: List<Car>,
        onProgress: (String) -> Unit // RZ - Callback para as etapas
    ): BackupResult = withContext(Dispatchers.IO) {
        if (cars.isEmpty()) return@withContext BackupResult(false, "Lista de carros vazia", "")
        
        try {
            onProgress("Conectando ao banco ${context.getString(R.string.FirestoreDatabaseName)}...")
            val db = getDbInstance(context)
            
            onProgress("Criando nova coleção no Firestore...")
            val collectionPrefix = context.getString(R.string.FirestoreCollectionName)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val fullCollectionPath = "${collectionPrefix}_$timeStamp"

            cars.forEachIndexed { index, car ->
                val docId = car.id ?: car.licence ?: UUID.randomUUID().toString()
                onProgress("Enviando item ${index + 1} de ${cars.size} (${car.name})...")
                
                val cleanCar = car.copy(nestedValue = null)
                db.collection(fullCollectionPath)
                    .document(docId)
                    .set(cleanCar)
                    .await()
            }
            
            BackupResult(true, "Backup concluído com sucesso", fullCollectionPath)
        } catch (e: Exception) {
            // RZ - Captura erros comuns como permissão ou banco não inicializado
            val errorMsg = "Erro: ${e.localizedMessage ?: "Falha na conexão"}"
            onProgress(errorMsg)
            BackupResult(false, errorMsg, "")
        }
    }
}
