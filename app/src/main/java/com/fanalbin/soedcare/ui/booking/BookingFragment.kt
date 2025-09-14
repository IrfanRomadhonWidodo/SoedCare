package com.fanalbin.soedcare.ui.booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.fanalbin.soedcare.R

class BookingFragment : Fragment() {

    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var spinnerService: Spinner
    private lateinit var etDate: EditText
    private lateinit var etTime: EditText
    private lateinit var etNotes: EditText
    private lateinit var btnSubmit: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_booking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi view
        etName = view.findViewById(R.id.et_name)
        etPhone = view.findViewById(R.id.et_phone)
//        spinnerService = view.findViewById(R.id.spinner_service)
        etDate = view.findViewById(R.id.et_date)
        etTime = view.findViewById(R.id.et_time)
        etNotes = view.findViewById(R.id.et_notes)
        btnSubmit = view.findViewById(R.id.btn_submit)

        // Setup listener
        btnSubmit.setOnClickListener {
            submitBooking()
        }
    }

    private fun submitBooking() {
        // Validasi input
        if (etName.text.toString().trim().isEmpty()) {
            etName.error = "Nama tidak boleh kosong"
            return
        }

        if (etPhone.text.toString().trim().isEmpty()) {
            etPhone.error = "Nomor telepon tidak boleh kosong"
            return
        }

        if (etDate.text.toString().trim().isEmpty()) {
            etDate.error = "Tanggal tidak boleh kosong"
            return
        }

        if (etTime.text.toString().trim().isEmpty()) {
            etTime.error = "Waktu tidak boleh kosong"
            return
        }

        // Proses booking
        // Di sini Anda bisa menambahkan logika untuk mengirim data booking ke server
        Toast.makeText(requireContext(), "Booking berhasil dikirim!", Toast.LENGTH_SHORT).show()

        // Kembali ke halaman sebelumnya
        activity?.onBackPressed()
    }
}