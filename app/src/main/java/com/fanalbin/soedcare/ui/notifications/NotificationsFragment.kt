//NotificationsFragment.kt
package com.fanalbin.soedcare.ui.notifications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fanalbin.soedcare.R
import com.fanalbin.soedcare.model.Notification
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date


class NotificationsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var rvNotifications: RecyclerView
    private lateinit var tvEmptyNotifications: TextView
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var cbSelectAll: CheckBox
    private lateinit var btnMarkAllRead: Button
    private lateinit var btnDeleteSelected: Button
    private val notificationList = mutableListOf<Notification>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_notifications, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        rvNotifications = view.findViewById(R.id.rv_notifications)
        tvEmptyNotifications = view.findViewById(R.id.tv_empty_notifications)
        cbSelectAll = view.findViewById(R.id.cb_select_all)
        btnMarkAllRead = view.findViewById(R.id.btn_mark_all_read)
        btnDeleteSelected = view.findViewById(R.id.btn_delete_selected)

        setupRecyclerView()
        setupActionButtons()
        loadNotifications()
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(
            notificationList,
            { notification ->
                markAsRead(notification)
                showNotificationDetail(notification)
            },
            { updateActionButtonsState() }  // Callback untuk perubahan seleksi
        )

        rvNotifications.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notificationAdapter
        }
    }

    private fun setupActionButtons() {
        cbSelectAll.setOnCheckedChangeListener { _, isChecked ->
            selectAllNotifications(isChecked)
        }

        btnMarkAllRead.setOnClickListener {
            markAllAsRead()
        }

        btnDeleteSelected.setOnClickListener {
            deleteSelectedNotifications()
        }

        // Initial state
        updateActionButtonsState()
    }

    private fun updateActionButtonsState() {
        val hasSelectedItems = notificationList.any { it.isSelected }
        val hasUnreadItems = notificationList.any { !it.isRead }

        btnDeleteSelected.isEnabled = hasSelectedItems
        btnMarkAllRead.isEnabled = hasUnreadItems

        // Update select all checkbox state
        val allSelected = notificationList.isNotEmpty() && notificationList.all { it.isSelected }
        cbSelectAll.setOnCheckedChangeListener(null)  // Temporarily remove listener
        cbSelectAll.isChecked = allSelected
        cbSelectAll.setOnCheckedChangeListener { _, isChecked ->
            selectAllNotifications(isChecked)
        }
    }

    private fun selectAllNotifications(select: Boolean) {
        notificationList.forEachIndexed { index, notification ->
            if (notification.isSelected != select) {
                notificationList[index] = notification.copy(isSelected = select)
                notificationAdapter.notifyItemChanged(index)
            }
        }
        updateActionButtonsState()
    }

    private fun markAllAsRead() {
        val batch = firestore.batch()
        val unreadNotifications = notificationList.filter { !it.isRead }

        if (unreadNotifications.isEmpty()) {
            Toast.makeText(context, "Semua notifikasi sudah dibaca", Toast.LENGTH_SHORT).show()
            return
        }

        unreadNotifications.forEach { notification ->
            val docRef = firestore.collection("notifications").document(notification.id)
            batch.update(docRef, "isRead", true)
        }

        batch.commit()
            .addOnSuccessListener {
                // Update local data
                notificationList.forEachIndexed { index, notification ->
                    if (!notification.isRead) {
                        notificationList[index] = notification.copy(isRead = true)
                        notificationAdapter.notifyItemChanged(index)
                    }
                }
                Toast.makeText(context, "Semua notifikasi ditandai sebagai dibaca", Toast.LENGTH_SHORT).show()
                updateActionButtonsState()
            }
            .addOnFailureListener { e ->
                Log.e("Notifications", "Gagal menandai notifikasi sebagai dibaca", e)
                Toast.makeText(context, "Gagal menandai notifikasi sebagai dibaca", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteSelectedNotifications() {
        val selectedNotifications = notificationList.filter { it.isSelected }

        if (selectedNotifications.isEmpty()) {
            Toast.makeText(context, "Tidak ada notifikasi yang dipilih", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Notifikasi")
            .setMessage("Apakah Anda yakin ingin menghapus ${selectedNotifications.size} notifikasi yang dipilih?")
            .setPositiveButton("Hapus") { dialog, _ ->
                val batch = firestore.batch()

                selectedNotifications.forEach { notification ->
                    val docRef = firestore.collection("notifications").document(notification.id)
                    batch.delete(docRef)
                }

                batch.commit()
                    .addOnSuccessListener {
                        // Update local data
                        val iterator = notificationList.iterator()
                        while (iterator.hasNext()) {
                            val notification = iterator.next()
                            if (notification.isSelected) {
                                val position = notificationList.indexOf(notification)
                                iterator.remove()
                                notificationAdapter.notifyItemRemoved(position)
                            }
                        }
                        Toast.makeText(context, "Notifikasi berhasil dihapus", Toast.LENGTH_SHORT).show()
                        updateActionButtonsState()
                        updateUIVisibility()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Notifications", "Gagal menghapus notifikasi", e)
                        Toast.makeText(context, "Gagal menghapus notifikasi", Toast.LENGTH_SHORT).show()
                    }

                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun loadNotifications() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            showEmptyState()
            return
        }

        firestore.collection("notifications")
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("Notifications", "Error listening for notifications", e)
                    return@addSnapshotListener
                }

                if (snapshots == null || snapshots.isEmpty) {
                    notificationList.clear()
                    notificationAdapter.notifyDataSetChanged()
                    showEmptyState()
                    return@addSnapshotListener
                }

                val newNotifications = snapshots.documents.map { doc ->
                    val notif = doc.toObject(Notification::class.java)!!.copy(id = doc.id)
                    val localIndex = notificationList.indexOfFirst { it.id == doc.id }

                    // Pertahankan hanya isSelected dari lokal (bukan isRead)
                    if (localIndex != -1) {
                        notif.copy(isSelected = notificationList[localIndex].isSelected)
                    } else notif
                }

                val existingIds = notificationList.map { it.id }.toSet()
                val newIds = newNotifications.map { it.id }.toSet()

                // --- 1️⃣ Deteksi notifikasi baru ---
                val addedNotifications = newNotifications.filter { it.id !in existingIds }

                // --- 2️⃣ Deteksi notifikasi yang dihapus ---
                val removedIds = existingIds - newIds

                // --- 3️⃣ Deteksi notifikasi yang diubah ---
                val modifiedNotifications = newNotifications.filter { it.id in existingIds && it.id !in removedIds }

                // 🔹 Hapus notifikasi yang sudah tidak ada
                if (removedIds.isNotEmpty()) {
                    val iterator = notificationList.iterator()
                    while (iterator.hasNext()) {
                        val notification = iterator.next()
                        if (notification.id in removedIds) {
                            val position = notificationList.indexOf(notification)
                            iterator.remove()
                            notificationAdapter.notifyItemRemoved(position)
                        }
                    }
                }

                // 🔹 Tambahkan notifikasi baru
                if (addedNotifications.isNotEmpty()) {
                    val startPosition = notificationList.size
                    notificationList.addAll(addedNotifications.sortedByDescending { it.timestamp })
                    notificationAdapter.notifyItemRangeInserted(startPosition, addedNotifications.size)

                    val toastMsg = if (addedNotifications.size == 1)
                        "Ada notifikasi baru"
                    else
                        "Ada ${addedNotifications.size} notifikasi baru"
                    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                }

                // 🔹 Update notifikasi yang berubah
                modifiedNotifications.forEach { modified ->
                    val index = notificationList.indexOfFirst { it.id == modified.id }
                    if (index != -1) {
                        val localIsSelected = notificationList[index].isSelected
                        // ⛔️ Tidak mempertahankan isRead dari lokal — biarkan Firestore yang menentukan
                        notificationList[index] = modified.copy(isSelected = localIsSelected)
                        notificationAdapter.notifyItemChanged(index)
                    }
                }

                updateUIVisibility()
                updateActionButtonsState()
            }
    }


    private fun showEmptyState() {
        tvEmptyNotifications.visibility = View.VISIBLE
        rvNotifications.visibility = View.GONE
    }

    private fun updateUIVisibility() {
        if (notificationList.isEmpty()) showEmptyState()
        else {
            tvEmptyNotifications.visibility = View.GONE
            rvNotifications.visibility = View.VISIBLE
        }
    }

    private fun markAsRead(notification: Notification) {
        val docRef = firestore.collection("notifications").document(notification.id)
        docRef.update("isRead", true)
            .addOnSuccessListener {
                val index = notificationList.indexOfFirst { it.id == notification.id }
                if (index != -1) {
                    notificationList[index] = notificationList[index].copy(isRead = true)
                    notificationAdapter.notifyItemChanged(index)
                    updateActionButtonsState()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Notifications", "Gagal menandai notifikasi sebagai dibaca", e)
            }
    }

    private fun showNotificationDetail(notification: Notification) {
        val message = """
            Pertanyaan: ${notification.questionTitle}
            
            Balasan: ${notification.answerContent}
            
            Dijawab oleh: ${notification.answeredBy} ${if (notification.isDoctor) "(Dokter)" else ""}
            
            Waktu: ${formatTimestamp(notification.timestamp)}
        """.trimIndent()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Detail Notifikasi")
            .setMessage(message)
            .setPositiveButton("Tutup") { dialog, _ -> dialog.dismiss() }
            .setNeutralButton("Hapus") { dialog, _ ->
                deleteNotification(notification)
                dialog.dismiss()
            }
            .show()
    }

    private fun formatTimestamp(timestamp: java.util.Date): String {
        val diff = Date().time - timestamp.time
        val minutes = diff / 60000
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "$days hari yang lalu"
            hours > 0 -> "$hours jam yang lalu"
            minutes > 0 -> "$minutes menit yang lalu"
            else -> "Baru saja"
        }
    }

    private fun deleteNotification(notification: Notification) {
        firestore.collection("notifications").document(notification.id)
            .delete()
            .addOnSuccessListener {
                val index = notificationList.indexOfFirst { it.id == notification.id }
                if (index != -1) {
                    notificationList.removeAt(index)
                    notificationAdapter.notifyItemRemoved(index)
                    updateUIVisibility()
                    updateActionButtonsState()
                }
                Toast.makeText(context, "Notifikasi berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("Notifications", "Gagal menghapus notifikasi", e)
                Toast.makeText(context, "Gagal menghapus notifikasi", Toast.LENGTH_SHORT).show()
            }
    }
}