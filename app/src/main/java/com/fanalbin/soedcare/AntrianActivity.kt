package com.fanalbin.soedcare

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fanalbin.soedcare.model.Booking
import com.fanalbin.soedcare.ui.antrian.AntrianFragment

class AntrianActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Nomor Antrian"

        // Tambahkan fragment dengan data booking jika ada
        if (savedInstanceState == null) {
            // Ambil data booking dari intent
            val booking = intent.getParcelableExtra<Booking>("booking_data")

            // Buat instance AntrianFragment
            val fragment = AntrianFragment().apply {
                // Siapkan arguments untuk fragment
                arguments = Bundle().apply {
                    putParcelable("booking_data", booking)
                }
            }

            // Tambahkan fragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitNow()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        // Cek apakah fragment saat ini adalah AntrianFragment
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        // Jika ada, panggil metode onBackPressed di fragment jika ada
        if (currentFragment is AntrianFragment) {
            // Lanjutkan dengan back press default karena tidak ada input yang perlu disimpan
            super.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }
}