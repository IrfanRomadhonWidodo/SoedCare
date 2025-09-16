package com.fanalbin.soedcare.ui.antrian

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.fanalbin.soedcare.R
import com.fanalbin.soedcare.model.Booking
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AntrianFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var booking: Booking? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_antrian, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Ambil data booking dari arguments
        booking = arguments?.getParcelable("booking_data")

        // Tampilkan data booking
        if (booking != null) {
            displayBookingData()
        }

        // Set listener untuk tombol batalkan
        view.findViewById<android.widget.Button>(R.id.btn_cancel_booking).setOnClickListener {
            showCancelConfirmationDialog()
        }

        // Set listener untuk tombol kembali - menggunakan activity
        view.findViewById<android.widget.Button>(R.id.btn_back_to_home).setOnClickListener {
            // Kembali ke home activity
            activity?.finish()
            // Atau jika ingin membuka HomeActivity secara eksplisit:
            // val intent = Intent(requireContext(), HomeActivity::class.java)
            // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            // startActivity(intent)
        }
    }

    private fun displayBookingData() {
        view?.findViewById<android.widget.TextView>(R.id.tv_queue_number)?.text =
            String.format("%03d", booking?.queueNumber ?: 0)

        view?.findViewById<android.widget.TextView>(R.id.tv_faskes_name)?.text = booking?.faskesName
        view?.findViewById<android.widget.TextView>(R.id.tv_booking_date)?.text = booking?.bookingDate
        view?.findViewById<android.widget.TextView>(R.id.tv_booking_time)?.text = booking?.bookingTime

        val statusText = when (booking?.status) {
            "pending" -> "Menunggu Konfirmasi"
            "confirmed" -> "Terkonfirmasi"
            "completed" -> "Selesai"
            "cancelled" -> "Dibatalkan"
            else -> "Unknown"
        }

        view?.findViewById<android.widget.TextView>(R.id.tv_queue_status)?.text = statusText
        view?.findViewById<android.widget.TextView>(R.id.tv_status)?.text = statusText
    }

    private fun showCancelConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Batalkan Booking")
            .setMessage("Apakah Anda yakin ingin membatalkan booking ini?")
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Ya") { dialog, _ ->
                cancelBooking()
                dialog.dismiss()
            }
            .show()
    }

    private fun cancelBooking() {
        // Simpan ID ke variabel lokal yang aman
        val bookingId = booking?.id

        // Periksa variabel lokal tersebut
        if (bookingId.isNullOrEmpty()) {
            Toast.makeText(context, "Data booking tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        // Gunakan variabel lokal yang sudah pasti tidak null
        firestore.collection("bookings")
            .document(bookingId)
            .update("status", "cancelled")
            .addOnSuccessListener {
                Toast.makeText(context, "Booking berhasil dibatalkan", Toast.LENGTH_SHORT).show()
                // Update status booking lokal setelah berhasil di Firebase
                booking = booking?.copy(status = "cancelled")
                displayBookingData()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Gagal membatalkan booking: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}