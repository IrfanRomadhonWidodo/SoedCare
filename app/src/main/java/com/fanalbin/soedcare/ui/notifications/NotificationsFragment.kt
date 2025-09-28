package com.fanalbin.soedcare.ui.notifications

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
import com.fanalbin.soedcare.model.Notification
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var rvNotifications: RecyclerView
    private lateinit var tvEmptyNotifications: TextView
    private lateinit var notificationAdapter: NotificationAdapter
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

        setupRecyclerView()
        loadNotifications()
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(notificationList) { notification ->
            markAsRead(notification)
            showNotificationDetail(notification)
        }

        rvNotifications.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notificationAdapter
        }
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

                if (snapshots == null) {
                    showEmptyState()
                    return@addSnapshotListener
                }

                val newNotifications = snapshots.documents.map { doc ->
                    val notif = doc.toObject(Notification::class.java)!!
                    val localIndex = notificationList.indexOfFirst { it.id == doc.id }
                    if (localIndex != -1) {
                        notif.copy(id = doc.id, isRead = notificationList[localIndex].isRead)
                    } else {
                        notif.copy(id = doc.id)
                    }
                }

                val existingIds = notificationList.map { it.id }.toSet()
                val newIds = newNotifications.map { it.id }.toSet()

                // Notifikasi baru
                val addedNotifications = newNotifications.filter { it.id !in existingIds }

                // Notifikasi yang dihapus
                val removedIds = existingIds - newIds

                // Notifikasi yang diubah
                val modifiedNotifications = newNotifications.filter { it.id in existingIds && it.id !in removedIds }

                // Hapus notifikasi yang sudah tidak ada
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

                // Tambahkan notifikasi baru, pastikan isRead tetap
                if (addedNotifications.isNotEmpty()) {
                    val startPosition = notificationList.size
                    notificationList.addAll(addedNotifications.sortedByDescending { it.timestamp })
                    notificationAdapter.notifyItemRangeInserted(startPosition, addedNotifications.size)

                    val toastMsg = if (addedNotifications.size == 1) "Ada notifikasi baru"
                    else "Ada ${addedNotifications.size} notifikasi baru"
                    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                }

                // Update notifikasi yang diubah
                modifiedNotifications.forEach { modifiedNotification ->
                    val index = notificationList.indexOfFirst { it.id == modifiedNotification.id }
                    if (index != -1) {
                        val localIsRead = notificationList[index].isRead
                        notificationList[index] = modifiedNotification.copy(isRead = localIsRead)
                        notificationAdapter.notifyItemChanged(index)
                    }
                }

                updateUIVisibility()
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
        """.trimIndent()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Detail Notifikasi")
            .setMessage(message)
            .setPositiveButton("Tutup") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
