package com.fanalbin.soedcare.ui.profile

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fanalbin.soedcare.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.* // Ganti import
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance() // Ganti dengan Realtime Database
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
            val email = currentUser.email ?: ""
            // Set default dulu supaya langsung muncul
            _userName.value = email.substringBefore("@").replaceFirstChar { it.uppercaseChar() }
            _userEmail.value = email
            _profileImageBase64.value = "" // default icon

            // Ganti dengan Realtime Database
            val userRef = database.reference.child("users").child(currentUser.uid)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("fullName").value?.toString() ?: ""
                        val phone = snapshot.child("phone").value?.toString() ?: ""
                        val address = snapshot.child("address").value?.toString() ?: ""
                        val profileImageBase64 = snapshot.child("profileImageBase64").value?.toString() ?: ""

                        if (name.isNotEmpty()) {
                            _userName.value = name.replaceFirstChar { it.uppercaseChar() }
                        }
                        _userFullName.value = name
                        _userPhone.value = phone
                        _userAddress.value = address
                        _profileImageBase64.value = profileImageBase64
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // tetap pakai default
                }
            })
        } else {
            // user belum login
            _userName.value = "Guest"
            _userEmail.value = ""
            _profileImageBase64.value = ""
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
            val userRef = database.reference.child("users").child(currentUser.uid)

            // Jika ada gambar baru, konversi ke base64
            if (imageUri != null) {
                try {
                    val base64String = imageUriToBase64(imageUri, context)
                    // Update data ke Realtime Database dengan base64
                    updateUserData(userRef, fullName, phone, address, base64String, callback)
                } catch (e: Exception) {
                    callback(false, "Failed to process image: ${e.message}")
                }
            } else {
                // Jika tidak ada gambar baru, gunakan base64 yang ada
                val currentImageBase64 = _profileImageBase64.value ?: ""
                updateUserData(userRef, fullName, phone, address, currentImageBase64, callback)
            }
        } else {
            callback(false, "User not authenticated")
        }
    }

    private fun imageUriToBase64(uri: Uri, context: Context): String {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        return bitmapToBase64(bitmap)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        // Kompresi gambar untuk mengurangi ukuran
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun updateUserData(
        userRef: DatabaseReference, // Ganti tipe parameter
        fullName: String,
        phone: String,
        address: String,
        profileImageBase64: String,
        callback: (Boolean, String) -> Unit
    ) {
        // Buat HashMap untuk update
        val updates = HashMap<String, Any>()
        updates["fullName"] = fullName
        updates["phone"] = phone
        updates["address"] = address
        updates["profileImageBase64"] = profileImageBase64
        updates["updatedAt"] = System.currentTimeMillis() // Ganti dengan timestamp biasa

        // Update data di Realtime Database
        userRef.updateChildren(updates)
            .addOnSuccessListener {
                // Update data di ViewModel
                _userFullName.value = fullName
                _userPhone.value = phone
                _userAddress.value = address
                _profileImageBase64.value = profileImageBase64
                // Update userName untuk display
                _userName.value = fullName.replaceFirstChar { it.uppercaseChar() }
                callback(true, "Profile updated successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to update profile: ${e.message}")
            }
    }

    fun logout(context: Context) {
        auth.signOut()
        // Arahkan ke halaman login
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}