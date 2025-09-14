package com.fanalbin.soedcare.ui.faskes

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.fanalbin.soedcare.R
import com.fanalbin.soedcare.model.Faskes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FaskesFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var btnFaskes1: Button
    private lateinit var btnFaskes2: Button
    private var selectedFaskesId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_faskes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Inisialisasi tombol
        btnFaskes1 = view.findViewById(R.id.btn_select_faskes_1)
        btnFaskes2 = view.findViewById(R.id.btn_select_faskes_2)

        // Set selector untuk tombol - menggunakan faskes_button_selector
        btnFaskes1.setBackgroundResource(R.drawable.faskes_button_selector)
        btnFaskes2.setBackgroundResource(R.drawable.faskes_button_selector)

        // Cek faskes yang sudah dipilih sebelumnya
        checkSelectedFaskes()

        // Set listener untuk tombol pilih faskes
        btnFaskes1.setOnClickListener {
            saveFaskesToFirestore(
                "faskes_1",
                "Faskes Unsoed 1",
                "Kampus Unsoed Purwokerto",
                "Jl. Prof. Dr. HR. Boenyamin, Purwokerto",
                "Senin - Jumat: 08:00 - 16:00",
                "(0281) 635552"
            )
        }

        btnFaskes2.setOnClickListener {
            saveFaskesToFirestore(
                "faskes_2",
                "Faskes Unsoed 2",
                "Fakultas Teknik Unsoed",
                "Jl.Mayor Jenderal Sungkono Km. 05, Blater, Kalimanah, Purbalingga",
                "Senin - Jumat: 08:00 - 16:00",
                "(0281) 632650"
            )
        }
    }

    private fun checkSelectedFaskes() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.d("FaskesFragment", "User not logged in")
            Toast.makeText(context, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            // Reset tombol jika user belum login
            updateButtonStates()
            return
        }

        Log.d("FaskesFragment", "Checking faskes for user: ${currentUser.uid}")
        firestore.collection("user_faskes")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                Log.d("FaskesFragment", "Document exists: ${document.exists()}")
                if (document != null && document.exists()) {
                    val faskes = document.toObject(Faskes::class.java)
                    selectedFaskesId = faskes?.id
                    Log.d("FaskesFragment", "Selected faskes ID: $selectedFaskesId")
                } else {
                    Log.d("FaskesFragment", "No faskes selected yet")
                    selectedFaskesId = null
                }
                // Update tampilan tombol berdasarkan faskes yang dipilih
                updateButtonStates()
            }
            .addOnFailureListener { e ->
                Log.e("FaskesFragment", "Error loading faskes data", e)
                Toast.makeText(context, "Gagal memuat data faskes: ${e.message}", Toast.LENGTH_SHORT).show()
                // Reset tombol jika gagal memuat
                selectedFaskesId = null
                updateButtonStates()
            }
    }

    private fun updateButtonStates() {
        when (selectedFaskesId) {
            "faskes_1" -> {
                btnFaskes1.isEnabled = true // jangan disable
                btnFaskes1.text = "Faskes Dipilih"
                // Gunakan selector, bukan set background color langsung
                btnFaskes1.setBackgroundResource(R.drawable.faskes_button_selector)
                btnFaskes2.isEnabled = true
                btnFaskes2.text = "Pilih Faskes Ini"
                btnFaskes2.setBackgroundResource(R.drawable.faskes_button_selector)
            }
            "faskes_2" -> {
                btnFaskes2.isEnabled = true
                btnFaskes2.text = "Faskes Dipilih"
                // Gunakan selector, bukan set background color langsung
                btnFaskes2.setBackgroundResource(R.drawable.faskes_button_selector)
                btnFaskes1.isEnabled = true
                btnFaskes1.text = "Pilih Faskes Ini"
                btnFaskes1.setBackgroundResource(R.drawable.faskes_button_selector)
            }
            else -> {
                btnFaskes1.isEnabled = true
                btnFaskes1.text = "Pilih Faskes Ini"
                btnFaskes1.setBackgroundResource(R.drawable.faskes_button_selector)
                btnFaskes2.isEnabled = true
                btnFaskes2.text = "Pilih Faskes Ini"
                btnFaskes2.setBackgroundResource(R.drawable.faskes_button_selector)
            }
        }
    }

    private fun saveFaskesToFirestore(
        id: String,
        name: String,
        location: String,
        address: String,
        operatingHours: String,
        phone: String
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("FaskesFragment", "Saving faskes: $id for user: ${currentUser.uid}")

        // Jika faskes yang dipilih sama dengan yang sudah tersimpan, tidak perlu update
        if (selectedFaskesId == id) {
            Toast.makeText(context, "Faskes ini sudah Anda pilih", Toast.LENGTH_SHORT).show()
            return
        }

        // Buat objek Faskes
        val faskes = Faskes(
            id = id,
            name = name,
            location = location,
            address = address,
            operatingHours = operatingHours,
            phone = phone,
            userId = currentUser.uid
        )

        // Simpan ke Firestore
        firestore.collection("user_faskes")
            .document(currentUser.uid)
            .set(faskes)
            .addOnSuccessListener {
                Log.d("FaskesFragment", "Faskes saved successfully")
                selectedFaskesId = id
                updateButtonStates()
                Toast.makeText(context, "Faskes berhasil disimpan", Toast.LENGTH_SHORT).show()
                // Navigasi ke halaman booking atau halaman berikutnya
                // Misalnya: findNavController().navigate(R.id.action_faskesFragment_to_bookingFragment)
            }
            .addOnFailureListener { e ->
                Log.e("FaskesFragment", "Error saving faskes", e)
                Toast.makeText(context, "Gagal menyimpan faskes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}