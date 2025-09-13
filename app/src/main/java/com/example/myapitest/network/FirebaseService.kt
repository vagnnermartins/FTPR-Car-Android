package com.example.myapitest.network

import android.net.Uri
import com.example.myapitest.MainActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.storage.StorageReference
import java.util.UUID
import java.util.concurrent.TimeUnit

object FirebaseService {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val storageReference = storage.reference

    fun sendVerificationCode(
        phoneNumber: String,
        activity: MainActivity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyPhoneNumber(
        verificationId: String,
        code: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithCredential(credential, onComplete)
    }

    fun signInWithCredential(
        credential: com.google.firebase.auth.AuthCredential,
        onComplete: (Boolean, String?) -> Unit
    ) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun getCurrentUser() = firebaseAuth.currentUser

    fun signOut() = firebaseAuth.signOut()

    fun isUserSignedIn(): Boolean = firebaseAuth.currentUser != null

    /**
     * Upload an image to Firebase Storage and return the download URL
     * @param imageUri The URI of the image to upload
     * @param folder The folder name in Firebase Storage (e.g., "cars", "profiles")
     * @param onProgress Callback for upload progress (0-100)
     * @param onComplete Callback with success status and URL or error message
     */
    fun uploadImage(
        imageUri: Uri,
        folder: String = "images",
        onProgress: (Int) -> Unit = {},
        onComplete: (Boolean, String?) -> Unit
    ) {
        try {
            // Generate unique filename
            val fileName = "${UUID.randomUUID()}.jpg"
            val imageRef: StorageReference = storageReference.child("$folder/$fileName")

            val uploadTask = imageRef.putFile(imageUri)

            // Monitor upload progress
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress(progress)
            }

            // Handle upload completion
            uploadTask.addOnSuccessListener { taskSnapshot ->
                // Get download URL
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onComplete(true, downloadUri.toString())
                }.addOnFailureListener { exception ->
                    onComplete(false, "Failed to get download URL: ${exception.message}")
                }
            }.addOnFailureListener { exception ->
                onComplete(false, "Upload failed: ${exception.message}")
            }

        } catch (e: Exception) {
            onComplete(false, "Error preparing upload: ${e.message}")
        }
    }

    /**
     * Delete an image from Firebase Storage
     * @param imageUrl The full download URL of the image to delete
     * @param onComplete Callback with success status and error message if any
     */
    fun deleteImage(
        imageUrl: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        try {
            val imageRef = storage.getReferenceFromUrl(imageUrl)
            imageRef.delete()
                .addOnSuccessListener {
                    onComplete(true, null)
                }
                .addOnFailureListener { exception ->
                    onComplete(false, "Delete failed: ${exception.message}")
                }
        } catch (e: Exception) {
            onComplete(false, "Error deleting image: ${e.message}")
        }
    }
}