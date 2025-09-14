package com.fanalbin.soedcare.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.fanalbin.soedcare.R
import com.fanalbin.soedcare.databinding.FragmentProfileBinding
import androidx.fragment.app.viewModels

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val profileViewModel: ProfileViewModel by viewModels()
    private val userProfileViewModel: UserProfileViewModel by activityViewModels()
    private val TAG = "ProfileFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Observasi semua LiveData dari ViewModel
        profileViewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.textName.text = name
        }

        profileViewModel.userEmail.observe(viewLifecycleOwner) { email ->
            binding.textEmail.text = email
        }

        // Observer untuk alamat
        profileViewModel.userAddress.observe(viewLifecycleOwner) { address ->
            binding.textAddress.text = if (address.isNullOrEmpty()) "Belum diisi" else address
        }

        // Observer untuk nomor telepon
        profileViewModel.userPhone.observe(viewLifecycleOwner) { phone ->
            binding.textPhone.text = if (phone.isNullOrEmpty()) "Belum diisi" else phone
        }

        // Observer untuk gambar profil dalam format Base64
        profileViewModel.profileImageBase64.observe(viewLifecycleOwner) { imageBase64 ->
            Log.d(TAG, "Profile image updated, length: ${imageBase64.length}")

            if (imageBase64.isNotEmpty()) {
                try {
                    val decodedString = Base64.decode(imageBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

                    // Gunakan Glide untuk memuat gambar bitmap
                    Glide.with(requireContext())
                        .load(bitmap)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(binding.imageProfile)
                } catch (e: Exception) {
                    Log.e(TAG, "Error decoding image", e)
                    // Tampilkan gambar default dengan Glide
                    Glide.with(requireContext())
                        .load(R.drawable.ic_profile)
                        .into(binding.imageProfile)
                }
            } else {
                // Tampilkan gambar default dengan Glide
                Glide.with(requireContext())
                    .load(R.drawable.ic_profile)
                    .into(binding.imageProfile)
            }
        }

        // Tombol edit profile
        binding.btnEditProfile.setOnClickListener {
            Log.d(TAG, "Opening edit profile")
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            intent.putExtra("FRAGMENT_TYPE", "EDIT_PROFILE")
            intent.putExtra("EXTRA_FULL_NAME", profileViewModel.userFullName.value ?: "")
            intent.putExtra("EXTRA_PHONE", profileViewModel.userPhone.value ?: "")
            intent.putExtra("EXTRA_ADDRESS", profileViewModel.userAddress.value ?: "")
            intent.putExtra("EXTRA_PROFILE_IMAGE_BASE64", profileViewModel.profileImageBase64.value ?: "")
            startActivity(intent)
        }

        // Tombol kebijakan privasi
        binding.btnPrivacyPolicy.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            intent.putExtra("FRAGMENT_TYPE", "PRIVACY_POLICY")
            startActivity(intent)
        }

        // Tombol logout
        binding.btnLogout.setOnClickListener {
            profileViewModel.logout(requireContext())
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called - refreshing profile data")
        // Refresh data saat fragment kembali ditampilkan
        profileViewModel.loadUserProfile()
        // Juga refresh UserProfileViewModel untuk memastikan data terbaru
        userProfileViewModel.refreshUserProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Bersihkan Glide untuk mencegah memory leak
        Glide.with(this).clear(binding.imageProfile)
        _binding = null
    }
}