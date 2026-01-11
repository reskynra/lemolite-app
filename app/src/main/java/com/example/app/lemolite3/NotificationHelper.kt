package com.example.app.lemolite3

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlin.random.Random

class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "lemolite_channel_id"
        private const val CHANNEL_NAME = "Lemolite Notifications"
        private const val CHANNEL_DESC = "Notifikasi aktivitas dari aplikasi Lemolite"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
            }

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            Random.nextInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE
                    else 0
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context)
            .notify(Random.nextInt(), notification)
    }

    fun notifyNewPost() =
        sendNotification(
            "Postingan Terbit!",
            "Hore! Postingan baru Anda berhasil dipublikasikan."
        )

    fun notifyEditPost() =
        sendNotification(
            "Perubahan Disimpan",
            "Postingan Anda telah berhasil diperbarui."
        )

    fun notifyDeletePost() =
        sendNotification(
            "Postingan Dihapus",
            "Postingan telah berhasil dihapus dari sistem."
        )

    fun notifyEditProfile() =
        sendNotification(
            "Profil Diperbarui",
            "Data profil Anda telah berhasil diubah."
        )

    fun notifyLikePost() =
        sendNotification(
            "Postingan Disukai ❤️",
            "Seseorang menyukai postingan Anda."
        )

    fun notifyNewComment() =
        sendNotification(
            "Komentar Baru 💬",
            "Ada komentar baru pada postingan Anda."
        )
}