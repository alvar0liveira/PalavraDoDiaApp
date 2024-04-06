package com.alvaroliveira.wordaday.worker

import android.Manifest
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.alvaroliveira.wordaday.R
import com.alvaroliveira.wordaday.presentation.MainActivity
import kotlin.random.Random


class WordNotificationWorker(
    private val appContext: Context,
    parameters: WorkerParameters
): Worker(appContext, parameters) {
    override fun doWork(): Result {

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(appContext, 0, intent, FLAG_IMMUTABLE)
        val builder = NotificationCompat
            .Builder(appContext, "WordChannel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("World A Day")
            .setContentText("Time for a new word")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(appContext)){
            if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
                notify(Random.nextInt(), builder.build())
            }
        }
        return Result.success()
    }
}