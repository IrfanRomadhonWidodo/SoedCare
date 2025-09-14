package com.fanalbin.soedcare.ui.profile

import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "UserProfileViewModel"

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> = _userEmail

    private val _profileImageBase64 = MutableLiveData<String>()
    val profileImageBase64: LiveData<String> = _profileImageBase64

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d(TAG, "Loading profile for user: ${currentUser.uid}")

            // Set default values dari Firebase Auth
            _userEmail.value = currentUser.email ?: ""
            _profileImageBase64.value = "" // Default kosong

            // Ambil data dari Firestore
            firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val fullName = document.getString("fullName") ?: ""
                        val profileImageBase64 = document.getString("profileImageBase64") ?: ""

                        // Update userName dengan fullName dari Firestore
                        if (fullName.isNotEmpty()) {
                            _userName.value = fullName
                        } else {
                            // Jika fullName kosong, gunakan email
                            val email = currentUser.email ?: ""
                            val rawName = email.substringBefore("@")
                            val cleanName = rawName.filter { it.isLetter() }
                            _userName.value = cleanName.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase() else it.toString()
                            }
                        }

                        // Update gambar profil
                        _profileImageBase64.value = profileImageBase64

                        Log.d(TAG, "User profile loaded: name=${_userName.value}, imageLength=${profileImageBase64.length}")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting user document", exception)
                    // Jika gagal, gunakan email sebagai fallback
                    val email = currentUser.email ?: ""
                    val rawName = email.substringBefore("@")
                    val cleanName = rawName.filter { it.isLetter() }
                    _userName.value = cleanName.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase() else it.toString()
                    }
                    _profileImageBase64.value = ""
                }
        } else {
            _userName.value = "User"
            _userEmail.value = ""
            _profileImageBase64.value = ""
        }
    }

    fun refreshUserProfile() {
        loadUserProfile()
    }
}