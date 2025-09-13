package com.fanalbin.soedcare.ui.home
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.fanalbin.soedcare.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.os.Handler
import android.os.Looper
import androidx.viewpager2.widget.ViewPager2
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewPager: ViewPager2
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Menampilkan username - PERBAIKAN DI SINI
        displayUsername()

        // Menampilkan tanggal dan waktu
        updateDateTime()

        viewPager = binding.root.findViewById(com.fanalbin.soedcare.R.id.viewpager_services)
        setupAutoSwipe()

        return root
    }

    // Perbaikan method ini
    private fun displayUsername() {
        val user = FirebaseAuth.getInstance().currentUser
        val displayName = getDisplayName(user)

        // Pisahkan "Hello, " dan nama pengguna ke TextView yang berbeda
        binding.textGreeting.text = "Hello, "  // Teks statis
        binding.textUsername.text = displayName  // Nama pengguna dinamis
    }

    private fun getDisplayName(user: FirebaseUser?): String {
        // Cek apakah user ada
        if (user == null) return "User"

        // Prioritaskan displayName jika ada
        if (!user.displayName.isNullOrEmpty()) {
            return user.displayName!!
        }

        // Jika displayName kosong, ambil dari email
        val email = user.email ?: "User"
        val rawName = email.substringBefore("@")
        val cleanName = rawName.filter { it.isLetter() }
        return cleanName.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }
    }

    private fun updateDateTime() {
        // Format tanggal
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        val currentDate = dateFormat.format(Date())
        binding.textDate.text = currentDate

        // Format waktu
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
        val currentTime = timeFormat.format(Date())
        binding.textTime.text = currentTime
    }

    private fun setupAutoSwipe() {
        val images = listOf(
            com.fanalbin.soedcare.R.drawable.layanan_1,
            com.fanalbin.soedcare.R.drawable.layanan_2,
            com.fanalbin.soedcare.R.drawable.layanan_3
        )
        viewPager.adapter = ServiceImageAdapter(images)

        val dotsCount = binding.dotsIndicator.childCount
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                for (i in 0 until dotsCount) {
                    binding.dotsIndicator.getChildAt(i)
                        .setBackgroundResource(com.fanalbin.soedcare.R.drawable.dot_unselected)
                }
                binding.dotsIndicator.getChildAt(position)
                    .setBackgroundResource(com.fanalbin.soedcare.R.drawable.dot_selected)
            }
        })

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                val currentItem = viewPager.currentItem
                val nextItem = if (currentItem < images.size - 1) currentItem + 1 else 0
                viewPager.currentItem = nextItem
                handler.postDelayed(this, 3000)
            }
        }
        handler.postDelayed(runnable, 3000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(runnable)
        _binding = null
    }
}