package com.fanalbin.soedcare.ui.booking

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.fanalbin.soedcare.R
import com.fanalbin.soedcare.model.Booking
import com.fanalbin.soedcare.model.Faskes
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

import com.fanalbin.soedcare.model.QueueCounter


class BookingFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var etBookingDate: TextInputEditText
    private lateinit var etNotes: TextInputEditText
    private lateinit var btnBooking: Button
    private lateinit var btnPilihFaskes: Button
    private lateinit var btnLengkapiData: Button
    private lateinit var cardInfoFaskes: CardView
    private lateinit var cardFaskesInfo: CardView
    private lateinit var cardInfoUser: CardView
    private lateinit var cardUserInfo: CardView
    private var selectedFaskes: Faskes? = null
    private var userName: String = ""
    private var userPhone: String = ""
    private var userAddress: String = ""
    private var isUserDataComplete: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_booking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Inisialisasi view
        etBookingDate = view.findViewById(R.id.et_booking_date)
        etNotes = view.findViewById(R.id.et_notes)
        btnBooking = view.findViewById(R.id.btn_booking)
        btnPilihFaskes = view.findViewById(R.id.btn_pilih_faskes)
        btnLengkapiData = view.findViewById(R.id.btn_lengkapi_data)
        cardInfoFaskes = view.findViewById(R.id.card_info_faskes)
        cardFaskesInfo = view.findViewById(R.id.card_faskes_info)
        cardInfoUser = view.findViewById(R.id.card_info_user)
        cardUserInfo = view.findViewById(R.id.card_user_info)

        // Ambil data faskes yang dipilih dari Firestore
        loadSelectedFaskesFromFirestore()

        // Ambil data user dari Firestore
        loadUserData()

        // Set listener untuk tombol pilih faskes
        btnPilihFaskes.setOnClickListener {
            val intent = Intent(requireContext(), com.fanalbin.soedcare.FaskesActivity::class.java)
            startActivity(intent)
        }

        // Set listener untuk tombol lengkapi data
        btnLengkapiData.setOnClickListener {
            val intent = Intent(
                requireContext(),
                com.fanalbin.soedcare.ui.profile.ProfileActivity::class.java
            )
            startActivity(intent)
        }

        // Set listener untuk tanggal
        etBookingDate.setOnClickListener {
            showDatePicker()
        }

        // Set listener untuk tombol booking
        btnBooking.setOnClickListener {
            if (validateForm()) {
                checkExistingBooking()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadSelectedFaskesFromFirestore()
        loadUserData()
    }

    private fun loadSelectedFaskesFromFirestore() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("user_faskes")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        selectedFaskes = document.toObject(Faskes::class.java)
                        Log.d("BookingFragment", "Loaded faskes: ${selectedFaskes?.name}")
                        displayFaskesInfo()
                    } else {
                        Log.d("BookingFragment", "No faskes selected yet")
                        cardInfoFaskes.visibility = View.VISIBLE
                        cardFaskesInfo.visibility = View.GONE
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("BookingFragment", "Error loading faskes data", e)
                    Toast.makeText(
                        context,
                        "Gagal memuat data faskes: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    cardInfoFaskes.visibility = View.VISIBLE
                    cardFaskesInfo.visibility = View.GONE
                }
        }
    }

    private fun displayFaskesInfo() {
        if (selectedFaskes != null) {
            cardInfoFaskes.visibility = View.GONE
            cardFaskesInfo.visibility = View.VISIBLE

            view?.findViewById<TextView?>(R.id.tv_faskes_name)?.text = selectedFaskes?.name
            view?.findViewById<TextView?>(R.id.tv_faskes_location)?.text = selectedFaskes?.location
            view?.findViewById<TextView?>(R.id.tv_faskes_hours)?.text =
                selectedFaskes?.operatingHours
        } else {
            cardInfoFaskes.visibility = View.VISIBLE
            cardFaskesInfo.visibility = View.GONE
        }
    }

    // Tambahkan metode ini di BookingFragment.kt
    fun onBackPressed(): Boolean {
        // Cek apakah ada input yang sudah diisi
        val hasInput = !etBookingDate.text.toString().trim().isEmpty() ||
                !etNotes.text.toString().trim().isEmpty()

        if (hasInput) {
            // Tampilkan dialog konfirmasi jika ada input yang sudah diisi
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Batalkan Booking")
                .setMessage("Apakah Anda yakin ingin membatalkan booking? Data yang sudah diisi akan hilang.")
                .setPositiveButton("Ya") { _, _ ->
                    // Tutup activity supaya kembali ke home
                    requireActivity().finish()
                }

                .setNegativeButton("Tidak", null)
                .show()

            return true // Menandakan bahwa back press telah ditangani
        }

        return false // Lanjutkan dengan back press default
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        userName =
                            document.getString("fullName") ?: document.getString("name") ?: ""
                        userPhone = document.getString("phone") ?: document.getString("phoneNumber")
                                ?: document.getString("noHp") ?: ""
                        userAddress =
                            document.getString("address") ?: document.getString("alamat") ?: ""

                        isUserDataComplete =
                            userName.isNotEmpty() && userPhone.isNotEmpty() && userAddress.isNotEmpty()
                        displayUserInfo()
                    } else {
                        Log.d("BookingFragment", "User document does not exist")
                        isUserDataComplete = false
                        displayUserInfo()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("BookingFragment", "Error loading user data", e)
                    Toast.makeText(
                        context,
                        "Gagal memuat data user: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    isUserDataComplete = false
                    displayUserInfo()
                }
        }
    }

    private fun displayUserInfo() {
        if (isUserDataComplete) {
            cardInfoUser.visibility = View.GONE
            cardUserInfo.visibility = View.VISIBLE

            view?.findViewById<TextView?>(R.id.tv_user_name)?.text = userName
            view?.findViewById<TextView?>(R.id.tv_user_phone)?.text = userPhone
            view?.findViewById<TextView?>(R.id.tv_user_address)?.text = userAddress
        } else {
            cardInfoUser.visibility = View.VISIBLE
            cardUserInfo.visibility = View.GONE
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDay = String.format("%02d", selectedDay)
                val formattedMonth = String.format("%02d", selectedMonth + 1)
                val selectedDate = "$formattedDay/$formattedMonth/$selectedYear"
                etBookingDate.setText(selectedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun validateForm(): Boolean {
        if (selectedFaskes == null) {
            Toast.makeText(context, "Silakan pilih faskes terlebih dahulu", Toast.LENGTH_SHORT)
                .show()
            return false
        }

        if (!isUserDataComplete) {
            Toast.makeText(
                context,
                "Silakan lengkapi data pemesan terlebih dahulu",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        val date = etBookingDate.text.toString().trim()
        if (date.isEmpty()) {
            Toast.makeText(context, "Silakan pilih tanggal booking", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun checkExistingBooking() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val faskesId = selectedFaskes?.id
        if (faskesId.isNullOrEmpty()) {
            Toast.makeText(context, "ID Faskes tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val bookingDateDisplay = etBookingDate.text.toString().trim()
        if (bookingDateDisplay.isEmpty()) {
            Toast.makeText(context, "Tanggal booking tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val dateParts = bookingDateDisplay.split("/")
        if (dateParts.size != 3) {
            Toast.makeText(context, "Format tanggal tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val bookingDateStorage = "${dateParts[2]}-${dateParts[1]}-${dateParts[0]}" // yyyy-MM-dd

        // Cek apakah user sudah memiliki booking untuk faskes yang sama pada hari yang sama
        firestore.collection("bookings")
            .whereEqualTo("userId", currentUser.uid)
            .whereEqualTo("faskesId", faskesId)
            .whereEqualTo("bookingDate", bookingDateStorage)
            .whereIn("status", listOf("confirmed", "pending"))
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Tidak ada booking yang sama, lanjutkan membuat booking
                    createBooking()
                } else {
                    // User sudah memiliki booking untuk faskes yang sama pada hari yang sama
                    Toast.makeText(
                        context,
                        "Anda sudah memiliki booking untuk faskes ini pada hari ini",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("BookingFragment", "Error checking existing booking", e)
                Toast.makeText(context, "Gagal memeriksa booking: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun createBooking() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val faskesId = selectedFaskes?.id
        if (faskesId.isNullOrEmpty()) {
            Toast.makeText(context, "ID Faskes tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val bookingDateDisplay = etBookingDate.text.toString().trim()
        if (bookingDateDisplay.isEmpty()) {
            Toast.makeText(context, "Tanggal booking tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val dateParts = bookingDateDisplay.split("/")
        if (dateParts.size != 3) {
            Toast.makeText(context, "Format tanggal tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val bookingDateStorage = "${dateParts[2]}-${dateParts[1]}-${dateParts[0]}" // yyyy-MM-dd
        val bookingsRef = firestore.collection("bookings")

        // Generate nomor antrian dengan pendekatan baru
        generateQueueNumber(faskesId, bookingDateStorage) { queueNumber ->
            val formattedQueueNumber = formatQueueNumber(queueNumber)

            val newBookingRef = bookingsRef.document()
            val booking = Booking(
                id = newBookingRef.id,
                userId = currentUser.uid,
                faskesId = faskesId,
                faskesName = selectedFaskes?.name ?: "",
                userName = userName,
                userPhone = userPhone,
                userAddress = userAddress,
                bookingDate = bookingDateStorage,
                bookingTime = selectedFaskes?.operatingHours ?: "",
                notes = etNotes.text.toString(),
                queueNumber = queueNumber,
                queueNumberFormatted = formattedQueueNumber,
                status = "confirmed"
            )

            newBookingRef.set(booking)
                .addOnSuccessListener {
                    Log.d("BookingFragment", "Booking saved with queue number: $formattedQueueNumber")
                    Toast.makeText(context, "Booking berhasil dengan nomor antrian: $formattedQueueNumber", Toast.LENGTH_SHORT).show()

                    val intent = Intent(requireContext(), com.fanalbin.soedcare.AntrianActivity::class.java)
                    intent.putExtra("booking_data", booking)
                    startActivity(intent)
                    activity?.finish()
                }
                .addOnFailureListener { e ->
                    Log.e("BookingFragment", "Error saving booking", e)
                    Toast.makeText(context, "Gagal melakukan booking: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun generateQueueNumber(faskesId: String, bookingDate: String, callback: (Int) -> Unit) {
        val queueId = "$faskesId-$bookingDate"
        val queueRef = firestore.collection("queue_numbers").document(queueId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(queueRef)
            val currentNumber = if (snapshot.exists()) {
                snapshot.toObject(QueueNumber::class.java)?.lastNumber ?: 0
            } else {
                0
            }
            val newNumber = currentNumber + 1

            // Update nomor antrian di koleksi queue_numbers
            val queueNumber = QueueNumber(
                id = queueId,
                faskesId = faskesId,
                date = bookingDate,
                lastNumber = newNumber,
                updatedAt = System.currentTimeMillis()
            )

            transaction.set(queueRef, queueNumber)
            newNumber
        }.addOnSuccessListener { queueNumber ->
            Log.d("BookingFragment", "Generated queue number: $queueNumber for faskes: $faskesId, date: $bookingDate")
            callback(queueNumber)
        }.addOnFailureListener { e ->
            Log.e("BookingFragment", "Error generating queue number", e)
            // Fallback: coba cari nomor terakhir dari koleksi bookings
            firestore.collection("bookings")
                .whereEqualTo("faskesId", faskesId)
                .whereEqualTo("bookingDate", bookingDate)
                .orderBy("queueNumber", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    val lastQueueNumber = if (documents.isEmpty) {
                        1
                    } else {
                        val lastBooking = documents.documents[0].toObject(Booking::class.java)
                        (lastBooking?.queueNumber ?: 0) + 1
                    }
                    Log.d("BookingFragment", "Fallback queue number: $lastQueueNumber")
                    callback(lastQueueNumber)
                }
                .addOnFailureListener { e2 ->
                    Log.e("BookingFragment", "Fallback failed", e2)
                    // Jika semua metode gagal, gunakan 1
                    callback(1)
                }
        }
    }
    private fun formatQueueNumber(queueNumber: Int): String {
        return String.format("%03d", queueNumber)
    }


}