package com.fanalbin.soedcare.ui.profile

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.fanalbin.soedcare.R
import com.google.android.material.textfield.TextInputEditText
import android.util.Base64
import android.graphics.BitmapFactory

class EditProfileFragment : Fragment() {
    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var progressDialog: ProgressDialog
    private var selectedImageUri: Uri? = null

    private val galleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedImageUri = uri
                    view?.findViewById<ImageView>(R.id.image_profile)?.setImageURI(uri)
                }
            }
        }

    private val cameraLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val bitmap = result.data?.extras?.get("data") as? android.graphics.Bitmap
                bitmap?.let {
                    // Simpan bitmap ke Uri sementara
                    selectedImageUri = getImageUriFromBitmap(it)
                    view?.findViewById<ImageView>(R.id.image_profile)?.setImageBitmap(it)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        progressDialog = ProgressDialog(requireContext()).apply {
            setTitle("Saving Profile")
            setMessage("Please wait...")
            setCancelable(false)
        }

        val editFullName = view.findViewById<TextInputEditText>(R.id.edit_full_name)
        val editPhone = view.findViewById<TextInputEditText>(R.id.edit_phone)
        val editAddress = view.findViewById<TextInputEditText>(R.id.edit_address)
        val imageProfile = view.findViewById<ImageView>(R.id.image_profile)
        val btnSave = view.findViewById<Button>(R.id.btn_save)
        val txtChangePicture = view.findViewById<TextView>(R.id.txt_change_picture)

        // Ambil data dari intent yang dikirim dari ProfileFragment
        val fullName = arguments?.getString("EXTRA_FULL_NAME") ?: ""
        val phone = arguments?.getString("EXTRA_PHONE") ?: ""
        val address = arguments?.getString("EXTRA_ADDRESS") ?: ""
        val profileImageBase64 = arguments?.getString("EXTRA_PROFILE_IMAGE_BASE64") ?: ""

        // Set data ke view
        editFullName.setText(fullName)
        editPhone.setText(phone)
        editAddress.setText(address)

        // Tampilkan gambar dari Base64 jika ada
        if (profileImageBase64.isNotEmpty()) {
            try {
                val decodedString = Base64.decode(profileImageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                imageProfile.setImageBitmap(bitmap)
            } catch (e: Exception) {
                imageProfile.setImageResource(R.drawable.ic_profile)
            }
        } else {
            imageProfile.setImageResource(R.drawable.ic_profile)
        }

        // Listener untuk mengganti gambar
        txtChangePicture.setOnClickListener {
            showImagePickerOptions()
        }

        imageProfile.setOnClickListener {
            showImagePickerOptions()
        }

        // Listener untuk tombol simpan
        btnSave.setOnClickListener {
            val newFullName = editFullName.text.toString().trim()
            val newPhone = editPhone.text.toString().trim()
            val newAddress = editAddress.text.toString().trim()

            // Validasi input
            if (newFullName.isEmpty()) {
                editFullName.error = "Full name is required"
                return@setOnClickListener
            }
            if (newPhone.isEmpty()) {
                editPhone.error = "Phone number is required"
                return@setOnClickListener
            }
            if (newAddress.isEmpty()) {
                editAddress.error = "Address is required"
                return@setOnClickListener
            }

            // Tampilkan progress dialog
            progressDialog.show()

            // Update data ke ViewModel
            profileViewModel.updateUserProfile(
                newFullName,
                newPhone,
                newAddress,
                selectedImageUri,
                requireContext()
            ) { success, message ->
                progressDialog.dismiss()

                if (success) {
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    activity?.onBackPressed()
                } else {
                    Toast.makeText(requireContext(), "Failed to update profile: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }

    private fun showImagePickerOptions() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Change Profile Picture")
        builder.setItems(options) { dialog, item ->
            when (options[item]) {
                "Take Photo" -> {
                    openCamera()
                }
                "Choose from Gallery" -> {
                    openGallery()
                }
                "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            cameraLauncher.launch(takePictureIntent)
        }
    }

    private fun openGallery() {
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(pickPhotoIntent)
    }

    private fun getImageUriFromBitmap(bitmap: android.graphics.Bitmap): Uri {
        val path = MediaStore.Images.Media.insertImage(
            requireContext().contentResolver,
            bitmap,
            "ProfileImage",
            null
        )
        return Uri.parse(path.toString())
    }
}