package com.example.callrecorder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CallRecorderService : Service() {

    private var recorder: MediaRecorder? = null
    private val channelId = "recorder_channel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, buildNotification("Waiting for calls..."))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startRecording()
        return START_STICKY
    }

    private fun startRecording() {
        val outputDir = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "recordings")
        if (!outputDir.exists()) outputDir.mkdirs()

        val fileName = "Call_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.mp3"
        val outputFile = File(outputDir, fileName)

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile.absolutePath)
            prepare()
            start()
        }

        val notification = buildNotification("Recording call: $fileName")
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Call Recorder", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
        } else {
            Notification.Builder(this)
        }
        return builder.setContentTitle("Auto Call Recorder")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
