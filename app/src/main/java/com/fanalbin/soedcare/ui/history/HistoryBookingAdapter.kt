package com.fanalbin.soedcare.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fanalbin.soedcare.R
import com.fanalbin.soedcare.model.Booking
import java.text.SimpleDateFormat
import java.util.*

class HistoryBookingAdapter(private val bookings: List<Booking>) :
    RecyclerView.Adapter<HistoryBookingAdapter.BookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(bookings[position])
    }

    override fun getItemCount() = bookings.size

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFaskesName: TextView = itemView.findViewById(R.id.tv_faskes_name)
        private val tvBookingDate: TextView = itemView.findViewById(R.id.tv_booking_date)
        private val tvQueueNumber: TextView = itemView.findViewById(R.id.tv_queue_number)
        private val tvNotes: TextView = itemView.findViewById(R.id.tv_notes)

        fun bind(booking: Booking) {
            tvFaskesName.text = booking.faskesName

            // Format tanggal dari yyyy-MM-dd ke dd/MM/yyyy
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = inputFormat.parse(booking.bookingDate)
                tvBookingDate.text = "Tanggal: ${outputFormat.format(date)}"
            } catch (e: Exception) {
                tvBookingDate.text = "Tanggal: ${booking.bookingDate}"
            }

            tvQueueNumber.text = "No. Antrian: ${booking.queueNumberFormatted}"
            tvNotes.text = "Catatan: ${booking.notes}"
        }
    }
}