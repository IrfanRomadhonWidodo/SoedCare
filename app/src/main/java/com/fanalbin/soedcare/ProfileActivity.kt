package com.fanalbin.soedcare.ui.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.fanalbin.soedcare.R

class ProfileActivity : AppCompatActivity() {

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
        if (savedInstanceState == null) {
            when (fragmentType) {
                "EDIT_PROFILE" -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, EditProfileFragment())
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