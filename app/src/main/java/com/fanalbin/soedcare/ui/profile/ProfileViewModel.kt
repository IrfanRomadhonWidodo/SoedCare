package com.fanalbin.soedcare.ui.profile

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fanalbin.soedcare.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "ProfileViewModel"

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> = _userEmail

    private val _userFullName = MutableLiveData<String>()
    val userFullName: LiveData<String> = _userFullName

    private val _userPhone = MutableLiveData<String>()
    val userPhone: LiveData<String> = _userPhone

    private val _userAddress = MutableLiveData<String>()
    val userAddress: LiveData<String> = _userAddress

    private val _profileImageBase64 = MutableLiveData<String>()
    val profileImageBase64: LiveData<String> = _profileImageBase64

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d(TAG, "Loading profile for user: ${currentUser.uid}")
            val email = currentUser.email ?: ""

            // Set default values
            _userName.value = email.substringBefore("@").replaceFirstChar { it.uppercaseChar() }
            _userEmail.value = email
            _profileImageBase64.value = ""

            // Ambil data dari Firestore
            firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        Log.d(TAG, "User document found")
                        val name = document.getString("fullName") ?: ""
                        val phone = document.getString("phone") ?: ""
                        val address = document.getString("address") ?: ""
                        val profileImageBase64 = document.getString("profileImageBase64") ?: ""

                        if (name.isNotEmpty()) {
                            _userName.value = name.replaceFirstChar { it.uppercaseChar() }
                        }
                        _userFullName.value = name
                        _userPhone.value = phone
                        _userAddress.value = address
                        _profileImageBase64.value = profileImageBase64
                    } else {
                        Log.d(TAG, "User document not found, creating new one")
                        // Buat dokumen baru jika belum ada
                        createUserDocument(currentUser.uid, email)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting user document", exception)
                }
        } else {
            Log.d(TAG, "No authenticated user")
            _userName.value = "Guest"
            _userEmail.value = ""
            _profileImageBase64.value = ""
        }
    }

    private fun createUserDocument(uid: String, email: String) {
        val userData = hashMapOf(
            "email" to email,
            "fullName" to "",
            "phone" to "",
            "address" to "",
            "profileImageBase64" to "",
            "createdAt" to System.currentTimeMillis()
        )

        firestore.collection("users").document(uid)
            .set(userData)
            .addOnSuccessListener {
                Log.d(TAG, "User document created successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error creating user document", e)
            }
    }

    fun updateUserProfile(
        fullName: String,
        phone: String,
        address: String,
        imageUri: Uri?,
        context: Context,
        callback: (Boolean, String) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d(TAG, "Updating profile for user: ${currentUser.uid}")

            val userRef = firestore.collection("users").document(currentUser.uid)

            // Jika ada gambar baru, konversi ke base64
            if (imageUri != null) {
                try {
                    val base64String = imageUriToBase64(imageUri, context)
                    updateUserData(userRef, fullName, phone, address, base64String, callback)
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing image", e)
                    callback(false, "Failed to process image: ${e.message}")
                }
            } else {
                // Gunakan base64 yang ada
                val currentImageBase64 = _profileImageBase64.value ?: ""
                updateUserData(userRef, fullName, phone, address, currentImageBase64, callback)
            }
        } else {
            Log.e(TAG, "User not authenticated")
            callback(false, "User not authenticated")
        }
    }

    private fun imageUriToBase64(uri: Uri, context: Context): String {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        return bitmapToBase64(bitmap)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun updateUserData(
        userRef: com.google.firebase.firestore.DocumentReference,
        fullName: String,
        phone: String,
        address: String,
        profileImageBase64: String,
        callback: (Boolean, String) -> Unit
    ) {
        val updates = HashMap<String, Any>()
        updates["fullName"] = fullName
        updates["phone"] = phone
        updates["address"] = address
        updates["profileImageBase64"] = profileImageBase64
        updates["updatedAt"] = System.currentTimeMillis()

        Log.d(TAG, "Updating document: ${userRef.path}")

        userRef.set(updates, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Profile updated successfully")
                // Update LiveData
                _userFullName.value = fullName
                _userPhone.value = phone
                _userAddress.value = address
                _profileImageBase64.value = profileImageBase64
                _userName.value = fullName.replaceFirstChar { it.uppercaseChar() }
                callback(true, "Profile updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating profile", e)
                callback(false, "Failed to update profile: ${e.message}")
            }
    }

    fun logout(context: Context) {
        auth.signOut()
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}