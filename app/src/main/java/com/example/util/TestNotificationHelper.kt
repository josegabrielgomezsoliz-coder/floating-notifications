package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

object TestNotificationHelper {

  private const val CHANNEL_ID = "channel_permissions_demo"
  private const val CHANNEL_NAME = "Pruebas de Notificaciones"

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        CHANNEL_NAME,
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Canal para comprobar el envío y lectura de notificaciones"
        enableLights(true)
        enableVibration(true)
      }
      val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      nm?.createNotificationChannel(channel)
    }
  }

  fun sendTestNotification(context: Context, testCount: Int): Boolean {
    createNotificationChannel(context)

    if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
      return false
    }

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      0,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_dialog_info)
      .setContentTitle("¡Permiso Verificado Exitosamente! #$testCount")
      .setContentText("El sistema de notificaciones está funcionando correctamente en segundo plano.")
      .setStyle(
        NotificationCompat.BigTextStyle().bigText(
          "Esta notificación de prueba confirma que la aplicación tiene autorización para publicar avisos y que el lector de notificaciones puede interceptarla."
        )
      )
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .build()

    try {
      NotificationManagerCompat.from(context).notify(1001 + (testCount % 5), notification)
      return true
    } catch (_: SecurityException) {
      return false
    }
  }

  @Suppress("DEPRECATION")
  fun triggerVibrationPattern(context: Context, patternIndex: Int): Boolean {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      vm?.defaultVibrator
    } else {
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    } ?: return false

    if (!vibrator.hasVibrator()) return false

    try {
      when (patternIndex) {
        0 -> { // Pulso corto
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
          } else {
            vibrator.vibrate(100)
          }
        }
        1 -> { // Doble pulso
          val pattern = longArrayOf(0, 120, 80, 140)
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
          } else {
            vibrator.vibrate(pattern, -1)
          }
        }
        2 -> { // Alerta rítmica
          val pattern = longArrayOf(0, 200, 100, 200, 100, 350)
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
          } else {
            vibrator.vibrate(pattern, -1)
          }
        }
      }
      return true
    } catch (_: Exception) {
      return false
    }
  }
}
