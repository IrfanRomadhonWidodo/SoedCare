package com.fanalbin.soedcare.ui.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fanalbin.soedcare.R
import com.fanalbin.soedcare.model.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var rvHistoryBooking: RecyclerView
    private lateinit var tvEmptyHistory: TextView
    private lateinit var historyAdapter: HistoryBookingAdapter
    private val bookingList = mutableListOf<Booking>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Inisialisasi view
        rvHistoryBooking = view.findViewById(R.id.rv_history_booking)
        tvEmptyHistory = view.findViewById(R.id.tv_empty_history)

        // Setup RecyclerView
        setupRecyclerView()

        // Load data booking history
        loadBookingHistory()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryBookingAdapter(bookingList)

        rvHistoryBooking.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
        }
    }

    private fun loadBookingHistory() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            tvEmptyHistory.visibility = View.VISIBLE
            rvHistoryBooking.visibility = View.GONE
            return
        }

        firestore.collection("bookings")
            .whereEqualTo("userId", currentUser.uid)
            //.orderBy("bookingDate", Query.Direction.DESCENDING)  // disable dulu
            .get()
            .addOnSuccessListener { documents ->
                bookingList.clear()
                for (document in documents) {
                    val booking = document.toObject(Booking::class.java)
                    bookingList.add(booking)
                }

                if (bookingList.isEmpty()) {
                    tvEmptyHistory.visibility = View.VISIBLE
                    rvHistoryBooking.visibility = View.GONE
                } else {
                    tvEmptyHistory.visibility = View.GONE
                    rvHistoryBooking.visibility = View.VISIBLE
                    historyAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                tvEmptyHistory.visibility = View.VISIBLE
                rvHistoryBooking.visibility = View.GONE
                Log.e("HistoryFragment", "Error loading booking history", e)
                Toast.makeText(context, "Gagal memuat riwayat booking", Toast.LENGTH_SHORT).show()
            }
    }

    private fun formatDateForDisplay(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    override fun onResume() {
        super.onResume()
        loadBookingHistory()
    }
}