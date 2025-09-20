package com.fanalbin.soedcare

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fanalbin.soedcare.ui.booking.BookingFragment

class BookingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Booking"

        // Tambahkan fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BookingFragment())
                .commitNow()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        // Cek apakah fragment saat ini adalah BookingFragment
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        // Jika ada booking yang sedang dibuat, tampilkan dialog konfirmasi
        if (currentFragment is BookingFragment) {
            // Panggil metode onBackPressed di fragment jika ada
            if (!currentFragment.onBackPressed()) {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }
}