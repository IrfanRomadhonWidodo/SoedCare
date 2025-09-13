package com.fanalbin.soedcare.ui.profile

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.fanalbin.soedcare.R

class ProfileActivity : AppCompatActivity() {
    private val TAG = "ProfileActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Profile"

        // Tentukan fragment yang akan ditampilkan berdasarkan intent
        val fragmentType = intent.getStringExtra("FRAGMENT_TYPE")
        Log.d(TAG, "Fragment type: $fragmentType")

        if (savedInstanceState == null) {
            when (fragmentType) {
                "EDIT_PROFILE" -> {
                    // Ambil data dari intent
                    val fullName = intent.getStringExtra("EXTRA_FULL_NAME") ?: ""
                    val phone = intent.getStringExtra("EXTRA_PHONE") ?: ""
                    val address = intent.getStringExtra("EXTRA_ADDRESS") ?: ""
                    val profileImageBase64 = intent.getStringExtra("EXTRA_PROFILE_IMAGE_BASE64") ?: ""

                    Log.d(TAG, "Passing data to EditProfileFragment - fullName: $fullName, phone: $phone, address: $address")

                    // Buat bundle untuk argumen
                    val args = Bundle().apply {
                        putString("EXTRA_FULL_NAME", fullName)
                        putString("EXTRA_PHONE", phone)
                        putString("EXTRA_ADDRESS", address)
                        putString("EXTRA_PROFILE_IMAGE_BASE64", profileImageBase64)
                    }

                    // Buat fragment dan set argumen
                    val editProfileFragment = EditProfileFragment().apply {
                        arguments = args
                    }

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, editProfileFragment)
                        .commit()
                }
                "PRIVACY_POLICY" -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, PrivacyPolicyFragment())
                        .commit()
                }
                else -> {
                    // Default ke EditProfileFragment
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, EditProfileFragment())
                        .commit()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}