package com.fanalbin.soedcare.ui.antrian

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.fanalbin.soedcare.R
import com.fanalbin.soedcare.model.Booking
import com.fanalbin.soedcare.BookingActivity  // Perbaikan: import dari paket utama
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AntrianFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var booking: Booking? = null

    // Views
    private lateinit var cardNoQueue: CardView
    private lateinit var cardQueueNumber: CardView
    private lateinit var cardBookingInfo: CardView
    private lateinit var tvQueueNumber: TextView
    private lateinit var tvFaskesName: TextView
    private lateinit var tvBookingDate: TextView
    private lateinit var tvBookingTime: TextView
    private lateinit var tvQueueStatus: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnBackToHome: Button
    private lateinit var btnBookingNow: Button

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

        // Inisialisasi views
        cardNoQueue = view.findViewById(R.id.card_no_queue)
        cardQueueNumber = view.findViewById(R.id.card_queue_number)
        cardBookingInfo = view.findViewById(R.id.card_booking_info)
        tvQueueNumber = view.findViewById(R.id.tv_queue_number)
        tvFaskesName = view.findViewById(R.id.tv_faskes_name)
        tvBookingDate = view.findViewById(R.id.tv_booking_date)
        tvBookingTime = view.findViewById(R.id.tv_booking_time)
        tvQueueStatus = view.findViewById(R.id.tv_queue_status)
        tvStatus = view.findViewById(R.id.tv_status)
        btnBackToHome = view.findViewById(R.id.btn_back_to_home)
        btnBookingNow = view.findViewById(R.id.btn_booking_now)

        // Ambil data booking dari arguments
        booking = arguments?.getParcelable("booking_data")
        Log.d("AntrianFragment", "Received booking data: $booking")

        // Jika tidak ada data booking, cek apakah ada booking aktif untuk hari ini
        if (booking == null) {
            checkActiveBooking()
        } else {
            // Tampilkan data booking yang diterima
            displayBookingData()
        }

        // Set listener untuk tombol booking sekarang
        btnBookingNow.setOnClickListener {
            val intent = Intent(requireContext(), BookingActivity::class.java)
            startActivity(intent)
        }

        // Set listener untuk tombol kembali
        btnBackToHome.setOnClickListener {
            activity?.finish()
        }
    }

    private fun checkActiveBooking() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            showNoBookingUI()
            return
        }

        // Dapatkan tanggal hari ini dalam format yyyy-MM-dd
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        firestore.collection("bookings")
            .whereEqualTo("userId", currentUser.uid)
            .whereEqualTo("bookingDate", today)
            .whereIn("status", listOf("confirmed", "pending"))
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Tidak ada booking aktif hari ini
                    showNoBookingUI()
                } else {
                    // Ambil booking pertama (seharusnya hanya ada satu)
                    val document = documents.first()
                    booking = document.toObject(Booking::class.java)
                    displayBookingData()
                }
            }
            .addOnFailureListener { e ->
                Log.e("AntrianFragment", "Error checking active booking", e)
                showNoBookingUI()
            }
    }

    private fun showNoBookingUI() {
        // Tampilkan card untuk belum ada antrian
        cardNoQueue.visibility = View.VISIBLE
        cardQueueNumber.visibility = View.GONE
        cardBookingInfo.visibility = View.GONE
        btnBackToHome.visibility = View.GONE
    }

    private fun displayBookingData() {
        if (booking == null) {
            showNoBookingUI()
            return
        }

        Log.d("AntrianFragment", "Displaying booking data. Queue number: ${booking?.queueNumber}")

        // Tampilkan card untuk antrian
        cardNoQueue.visibility = View.GONE
        cardQueueNumber.visibility = View.VISIBLE
        cardBookingInfo.visibility = View.VISIBLE
        btnBackToHome.visibility = View.VISIBLE

        // Gunakan queueNumberFormatted yang sudah disimpan di database
        val formattedQueueNumber = booking?.queueNumberFormatted ?: formatQueueNumber(booking?.queueNumber ?: 0)

        tvQueueNumber.text = formattedQueueNumber

        // Format tanggal untuk tampilan (dd/MM/yyyy)
        val storageDate = booking?.bookingDate
        val displayDate = if (storageDate != null && storageDate.contains("-")) {
            val parts = storageDate.split("-")
            "${parts[2]}/${parts[1]}/${parts[0]}" // Konversi yyyy-MM-dd ke dd/MM/yyyy
        } else {
            storageDate ?: ""
        }

        tvFaskesName.text = booking?.faskesName
        tvBookingDate.text = displayDate
        tvBookingTime.text = booking?.bookingTime

        // Status selalu "Terkonfirmasi"
        tvQueueStatus.text = "Terkonfirmasi"
        tvStatus.text = "Terkonfirmasi"
    }
    private fun formatQueueNumber(queueNumber: Int): String {
        return String.format("%03d", queueNumber)
    }
}