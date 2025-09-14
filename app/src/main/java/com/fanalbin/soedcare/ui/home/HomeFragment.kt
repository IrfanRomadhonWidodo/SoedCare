package com.fanalbin.soedcare.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.fanalbin.soedcare.R
import com.fanalbin.soedcare.databinding.FragmentHomeBinding
import com.fanalbin.soedcare.ui.profile.UserProfileViewModel
import com.fanalbin.soedcare.ArtikelActivity
import com.fanalbin.soedcare.BookingActivity
import com.fanalbin.soedcare.AntrianActivity
import com.fanalbin.soedcare.FaskesActivity // Tambahkan import ini
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
    // Gunakan activityViewModels untuk berbagi ViewModel antar fragment
    private val userProfileViewModel: UserProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            androidx.lifecycle.ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Observasi perubahan nama pengguna dari UserProfileViewModel
        userProfileViewModel.userName.observe(viewLifecycleOwner) { userName ->
            binding.textUsername.text = userName
        }

        // Observasi perubahan gambar profil dari UserProfileViewModel
        userProfileViewModel.profileImageBase64.observe(viewLifecycleOwner) { imageBase64 ->
            updateProfileImage(binding.imageProfile, imageBase64)
        }

        // Menampilkan tanggal dan waktu
        updateDateTime()
        viewPager = binding.root.findViewById(com.fanalbin.soedcare.R.id.viewpager_services)
        setupAutoSwipe()

        return root
    }

    private fun updateProfileImage(imageView: ImageView, imageBase64: String) {
        Log.d("HomeFragment", "Updating profile image, length: ${imageBase64.length}")
        if (imageBase64.isNotEmpty()) {
            try {
                val decodedString = Base64.decode(imageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                // Gunakan Glide untuk memuat gambar bitmap
                Glide.with(requireContext())
                    .load(bitmap)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .circleCrop() // Membuat gambar menjadi lingkaran
                    .into(imageView)
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error decoding image", e)
                // Tampilkan gambar default dengan Glide
                Glide.with(requireContext())
                    .load(R.drawable.ic_profile)
                    .circleCrop()
                    .into(imageView)
            }
        } else {
            // Tampilkan gambar default dengan Glide
            Glide.with(requireContext())
                .load(R.drawable.ic_profile)
                .circleCrop()
                .into(imageView)
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

    override fun onResume() {
        super.onResume()
        // Refresh data profil saat fragment kembali ditampilkan
        userProfileViewModel.refreshUserProfile()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Gunakan binding untuk mengakses view
        binding.infoArtikel.setOnClickListener {
            val intent = Intent(requireContext(), ArtikelActivity::class.java)
            startActivity(intent)
        }

        // Tambahkan listener untuk booking service
        // Sesuaikan dengan ID yang ada di layout Anda
        binding.bookingService.setOnClickListener {
            val intent = Intent(requireContext(), BookingActivity::class.java)
            startActivity(intent)
        }

        // Tambahkan listener untuk antrian service
        // Sesuaikan dengan ID yang ada di layout Anda
        binding.antrianService.setOnClickListener {
            val intent = Intent(requireContext(), AntrianActivity::class.java)
            startActivity(intent)
        }

        // Tambahkan listener untuk faskes service
        // Sesuaikan dengan ID yang ada di layout Anda
        binding.faskesService.setOnClickListener {
            val intent = Intent(requireContext(), FaskesActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(runnable)
        // Bersihkan Glide untuk mencegah memory leak
        Glide.with(this).clear(binding.imageProfile)
        _binding = null
    }
}