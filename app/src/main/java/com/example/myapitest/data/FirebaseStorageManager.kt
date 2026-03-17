package com.example.myapitest.data

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

// RZ - Gerenciador do Firebase Storage.
// A função dele é pegar um arquivo do celular e "jogar na nuvem" do Google.
// Ele retorna um link (URL) que podemos guardar no nosso banco de dados.

object FirebaseStorageManager {

    private val storage = FirebaseStorage.getInstance()

    // RZ - Função reutilizável para fazer upload de uma imagem
    // Recebe a URI da imagem local e retorna a URL pública de download
    suspend fun uploadImage(imageUri: Uri): String? {
        return try {
            // RZ - Gera um nome único para o arquivo para não sobrescrever outros
            val fileName = "cars/${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference.child(fileName)

            // RZ - Faz o upload e espera a conclusão (await)
            storageRef.putFile(imageUri).await()

            // RZ - Busca o link público para acesso
            val downloadUrl = storageRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            // RZ - Em caso de erro (falta de internet ou permissão), retorna nulo
            null
        }
    }
}
