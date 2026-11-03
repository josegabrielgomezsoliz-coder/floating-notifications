package com.example.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import com.example.model.CapturedNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppNotificationListenerService : NotificationListenerService() {

  companion object {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _recentNotifications = MutableStateFlow<List<CapturedNotification>>(emptyList())
    val recentNotifications: StateFlow<List<CapturedNotification>> = _recentNotifications.asStateFlow()

    fun isPermissionGranted(context: Context): Boolean {
      val enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(context)
      return enabledListeners.contains(context.packageName)
    }

    fun getComponentName(context: Context): ComponentName {
      return ComponentName(context, AppNotificationListenerService::class.java)
    }

    fun clearHistory() {
      _recentNotifications.value = emptyList()
    }
  }

  override fun onListenerConnected() {
    super.onListenerConnected()
    _isConnected.value = true
  }

  override fun onListenerDisconnected() {
    super.onListenerDisconnected()
    _isConnected.value = false
  }

  override fun onNotificationPosted(sbn: StatusBarNotification?) {
    super.onNotificationPosted(sbn)
    if (sbn == null) return

    val pkg = sbn.packageName ?: return
    val extras = sbn.notification?.extras
    val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
    val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()

    val pm = applicationContext.packageManager
    val appName = try {
      val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        pm.getApplicationInfo(pkg, PackageManager.ApplicationInfoFlags.of(0))
      } else {
        @Suppress("DEPRECATION")
        pm.getApplicationInfo(pkg, 0)
      }
      pm.getApplicationLabel(appInfo).toString()
    } catch (_: Exception) {
      pkg
    }

    val item = CapturedNotification(
      id = "${pkg}_${sbn.id}_${sbn.postTime}",
      packageName = pkg,
      appName = appName,
      title = title,
      text = text,
      postTime = sbn.postTime
    )

    val current = _recentNotifications.value.toMutableList()
    current.add(0, item)
    if (current.size > 20) {
      current.removeAt(current.lastIndex)
    }
    _recentNotifications.value = current
  }

  override fun onNotificationRemoved(sbn: StatusBarNotification?) {
    super.onNotificationRemoved(sbn)
  }
}
