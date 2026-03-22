package com.example.myapitest.utils

import com.google.firebase.storage.FirebaseStorage
import android.net.Uri
import java.util.*

fun uploadImageToFirebase(uri: Uri, onComplete: (String?) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference
    val imageRef = storageRef.child("cars/${UUID.randomUUID()}.jpg")

    imageRef.putFile(uri)
        .addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                onComplete(downloadUrl.toString())
            }
        }
        .addOnFailureListener {
            onComplete(null)
        }
}