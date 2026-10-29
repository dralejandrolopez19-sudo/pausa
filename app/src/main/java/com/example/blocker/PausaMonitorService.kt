package com.example.blocker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Foreground Guardian Service that monitors foreground social apps via UsageStatsManager
 * every second and launches PAUSA's ModoPausaScreen when a restricted social app is opened.
 */
class PausaMonitorService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitorJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        startGuardianForegroundNotification()
        startForegroundAppLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startGuardianForegroundNotification()
        if (monitorJob?.isActive != true) {
            startForegroundAppLoop()
        }
        return START_STICKY
    }

    private fun startGuardianForegroundNotification() {
        val channelId = "pausa_guardian_blocker_channel"
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager != null) {
            val channel = NotificationChannel(
                channelId,
                "Escudo de Bloqueo PAUSA",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitorea y bloquea el acceso impulsivo a redes sociales seleccionadas."
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            101,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Escudo PAUSA Activo")
            .setContentText("Protegiendo tu desconexión de redes sociales")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()

        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    1001,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(1001, notification)
            }
        }
    }

    private fun startForegroundAppLoop() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            while (isActive) {
                val fgPkg = PausaBlockerController.queryCurrentForegroundPackage(applicationContext)
                if (!fgPkg.isNullOrBlank()) {
                    PausaBlockerController.interceptIfNeeded(applicationContext, fgPkg)
                }
                delay(900L)
            }
        }
    }

    override fun onDestroy() {
        monitorJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
