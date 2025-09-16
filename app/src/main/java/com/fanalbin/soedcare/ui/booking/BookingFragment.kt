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
            // Buka FaskesActivity untuk memilih faskes
            val intent = Intent(requireContext(), com.fanalbin.soedcare.FaskesActivity::class.java)
            startActivity(intent)
        }

        // Set listener untuk tombol lengkapi data
        btnLengkapiData.setOnClickListener {
            // Buka ProfileActivity untuk melengkapi data
            val intent = Intent(requireContext(), com.fanalbin.soedcare.ui.profile.ProfileActivity::class.java)
            startActivity(intent)
        }

        // Set listener untuk tanggal
        etBookingDate.setOnClickListener {
            showDatePicker()
        }

        // Set listener untuk tombol booking
        btnBooking.setOnClickListener {
            if (validateForm()) {
                createBooking()
            }
        }
    }

    // Tambahkan onResume untuk memuat ulang data faskes dan user setelah kembali dari activity lain
    override fun onResume() {
        super.onResume()
        // Muat ulang data faskes yang dipilih
        loadSelectedFaskesFromFirestore()
        // Muat ulang data user
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
                        // Tampilkan informasi untuk memilih faskes
                        cardInfoFaskes.visibility = View.VISIBLE
                        cardFaskesInfo.visibility = View.GONE
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("BookingFragment", "Error loading faskes data", e)
                    Toast.makeText(context, "Gagal memuat data faskes: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Tampilkan informasi untuk memilih faskes
                    cardInfoFaskes.visibility = View.VISIBLE
                    cardFaskesInfo.visibility = View.GONE
                }
        }
    }

    private fun displayFaskesInfo() {
        if (selectedFaskes != null) {
            // Sembunyikan informasi untuk memilih faskes
            cardInfoFaskes.visibility = View.GONE
            cardFaskesInfo.visibility = View.VISIBLE

            // Tampilkan data faskes
            view?.findViewById<TextView?>(R.id.tv_faskes_name)?.text = selectedFaskes?.name
            view?.findViewById<TextView?>(R.id.tv_faskes_location)?.text = selectedFaskes?.location
            view?.findViewById<TextView?>(R.id.tv_faskes_hours)?.text = selectedFaskes?.operatingHours
        } else {
            // Tampilkan informasi untuk memilih faskes
            cardInfoFaskes.visibility = View.VISIBLE
            cardFaskesInfo.visibility = View.GONE
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Logging untuk melihat semua field yang ada di dokumen
                        Log.d("BookingFragment", "User document data: ${document.data}")

                        // Coba dapatkan semua field yang mungkin
                        userName = document.getString("fullName") ?: document.getString("name") ?: ""
                        userPhone = document.getString("phone") ?: document.getString("phoneNumber") ?: document.getString("noHp") ?: ""
                        userAddress = document.getString("address") ?: document.getString("alamat") ?: ""

                        // Logging nilai yang didapat
                        Log.d("BookingFragment", "Name: $userName")
                        Log.d("BookingFragment", "Phone: $userPhone")
                        Log.d("BookingFragment", "Address: $userAddress")

                        // Periksa apakah data user sudah lengkap
                        isUserDataComplete = userName.isNotEmpty() && userPhone.isNotEmpty() && userAddress.isNotEmpty()

                        // Update UI berdasarkan kelengkapan data
                        displayUserInfo()
                    } else {
                        Log.d("BookingFragment", "User document does not exist")
                        // Tampilkan informasi untuk melengkapi data user
                        isUserDataComplete = false
                        displayUserInfo()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("BookingFragment", "Error loading user data", e)
                    Toast.makeText(context, "Gagal memuat data user: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Tampilkan informasi untuk melengkapi data user
                    isUserDataComplete = false
                    displayUserInfo()
                }
        }
    }

    private fun displayUserInfo() {
        if (isUserDataComplete) {
            // Sembunyikan informasi untuk melengkapi data user
            cardInfoUser.visibility = View.GONE
            cardUserInfo.visibility = View.VISIBLE

            // Tampilkan data user
            view?.findViewById<TextView?>(R.id.tv_user_name)?.text = userName
            view?.findViewById<TextView?>(R.id.tv_user_phone)?.text = userPhone
            view?.findViewById<TextView?>(R.id.tv_user_address)?.text = userAddress
        } else {
            // Tampilkan informasi untuk melengkapi data user
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
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                etBookingDate.setText(selectedDate)
            },
            year,
            month,
            day
        )

        // Set min date to today
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun validateForm(): Boolean {
        if (selectedFaskes == null) {
            Toast.makeText(context, "Silakan pilih faskes terlebih dahulu", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!isUserDataComplete) {
            Toast.makeText(context, "Silakan lengkapi data pemesan terlebih dahulu", Toast.LENGTH_SHORT).show()
            return false
        }

        val date = etBookingDate.text.toString().trim()
        if (date.isEmpty()) {
            Toast.makeText(context, "Silakan pilih tanggal booking", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun createBooking() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        // Generate nomor antrian
        generateQueueNumber { queueNumber ->
            val booking = Booking(
                userId = currentUser.uid,
                faskesId = selectedFaskes?.id ?: "",
                faskesName = selectedFaskes?.name ?: "",
                userName = userName,
                userPhone = userPhone,
                userAddress = userAddress,
                bookingDate = etBookingDate.text.toString(),
                bookingTime = selectedFaskes?.operatingHours ?: "",
                notes = etNotes.text.toString(),
                queueNumber = queueNumber,
                status = "pending"
            )

            // Simpan booking ke Firestore
            firestore.collection("bookings")
                .add(booking)
                .addOnSuccessListener { documentReference ->
                    val bookingId = documentReference.id
                    val updatedBooking = booking.copy(id = bookingId)

                    Toast.makeText(context, "Booking berhasil", Toast.LENGTH_SHORT).show()

                    // Navigasi ke halaman antrian menggunakan activity
                    val intent = Intent(requireContext(), com.fanalbin.soedcare.AntrianActivity::class.java)
                    intent.putExtra("booking_data", updatedBooking)
                    startActivity(intent)
                    activity?.finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Gagal melakukan booking: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun generateQueueNumber(callback: (Int) -> Unit) {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        firestore.collection("bookings")
            .whereEqualTo("bookingDate", today)
            .get()
            .addOnSuccessListener { documents ->
                val count = documents.size()
                callback(count + 1)
            }
            .addOnFailureListener { e ->
                // Jika gagal, gunakan default 1
                callback(1)
            }
    }
}