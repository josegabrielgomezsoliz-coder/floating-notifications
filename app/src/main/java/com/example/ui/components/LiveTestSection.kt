package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NotificationAdd
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CapturedNotification
import com.example.model.InstalledAppItem
import com.example.ui.PermissionUiState
import com.example.ui.theme.StatusGranted
import com.example.ui.theme.StatusMissing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LiveTestSection(
  state: PermissionUiState,
  onSendNotification: () -> Unit,
  onTestVibration: (Int) -> Unit,
  onToggleOverlay: () -> Unit,
  onLoadApps: () -> Unit,
  onClearCapturedNotifications: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header Intro
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
      )
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "Banco de Pruebas de Permisos",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Comprueba en vivo y de forma segura el comportamiento de las APIs oficiales de Android tras activar cada permiso.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // 1. Test Send Notification
    TestCard(
      title = "1. Publicar Notificación Propia",
      subtitle = "Verifica el permiso POST_NOTIFICATIONS",
      icon = Icons.Default.NotificationAdd,
      isUnlocked = state.permissions.firstOrNull { it.id == com.example.model.PermissionId.POST_NOTIFICATIONS }?.isGranted == true
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Dispara una alerta prioritaria en la barra de notificaciones del dispositivo.",
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.weight(1f),
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = onSendNotification,
          modifier = Modifier.testTag("test_send_notification_button"),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Disparar", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        }
      }
    }

    // 2. Test Vibration
    TestCard(
      title = "2. Respuesta Háptica / Vibrador",
      subtitle = "Verifica el acceso de hardware VIBRATE",
      icon = Icons.Default.Vibration,
      isUnlocked = state.permissions.firstOrNull { it.id == com.example.model.PermissionId.VIBRATION }?.isGranted == true
    ) {
      Column {
        Text(
          text = "Prueba tres patrones de pulsación táctil independientes:",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = { onTestVibration(0) },
            modifier = Modifier.weight(1f).testTag("vibrate_pulse_btn"),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Pulso", style = MaterialTheme.typography.labelSmall)
          }
          OutlinedButton(
            onClick = { onTestVibration(1) },
            modifier = Modifier.weight(1f).testTag("vibrate_double_btn"),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Doble", style = MaterialTheme.typography.labelSmall)
          }
          OutlinedButton(
            onClick = { onTestVibration(2) },
            modifier = Modifier.weight(1f).testTag("vibrate_alert_btn"),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Alerta", style = MaterialTheme.typography.labelSmall)
          }
        }
      }
    }

    // 3. Test Overlay / SYSTEM_ALERT_WINDOW
    TestCard(
      title = "3. Elemento Flotante Superpuesto",
      subtitle = "Verifica el permiso SYSTEM_ALERT_WINDOW",
      icon = Icons.Default.Layers,
      isUnlocked = state.permissions.firstOrNull { it.id == com.example.model.PermissionId.OVERLAY_DISPLAY }?.isGranted == true
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (state.isOverlayRunning) "Cápsula flotante activa sobre la pantalla. ¡Arrástrala con el dedo!" else "Muestra una cápsula flotante arrastrable sobre cualquier aplicación.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = onToggleOverlay,
          modifier = Modifier.testTag("test_overlay_toggle_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (state.isOverlayRunning) StatusMissing else MaterialTheme.colorScheme.primary
          ),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(
            if (state.isOverlayRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            if (state.isOverlayRunning) "Ocultar" else "Mostrar",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }

    // 4. Notification Listener Feed
    TestCard(
      title = "4. Lector de Notificaciones Interceptadas",
      subtitle = "Servicio de escucha BIND_NOTIFICATION_LISTENER",
      icon = Icons.Default.NotificationsActive,
      isUnlocked = state.permissions.firstOrNull { it.id == com.example.model.PermissionId.NOTIFICATION_LISTENER }?.isGranted == true
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (state.isListenerConnected) StatusGranted else StatusMissing)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (state.isListenerConnected) "Servicio conectado y escuchando" else "Servicio no conectado por Android",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
              color = if (state.isListenerConnected) StatusGranted else StatusMissing
            )
          }

          if (state.recentCapturedNotifications.isNotEmpty()) {
            IconButton(
              onClick = onClearCapturedNotifications,
              modifier = Modifier.size(28.dp)
            ) {
              Icon(Icons.Default.DeleteSweep, contentDescription = "Limpiar historial", modifier = Modifier.size(18.dp))
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.recentCapturedNotifications.isEmpty()) {
          Text(
            text = "Ninguna notificación capturada aún. Envía una notificación de prueba o recibe un aviso para ver cómo el lector la detecta automáticamente.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.recentCapturedNotifications.take(4).forEach { notif ->
              CapturedNotificationItem(notif)
            }
          }
        }
      }
    }

    // 5. Installed Apps Visibility
    TestCard(
      title = "5. Visibilidad de Apps para Reglas Propias",
      subtitle = "Acceso a lista de paquetes del sistema",
      icon = Icons.Default.Apps,
      isUnlocked = state.permissions.firstOrNull { it.id == com.example.model.PermissionId.INSTALLED_APPS }?.isGranted == true
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Permite configurar qué aplicaciones pueden o no generar alertas personalizadas.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = onLoadApps,
            modifier = Modifier.testTag("load_installed_apps_button"),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Consultar", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
          }
        }

        if (state.isLoadingApps) {
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cargando catálogo de apps...", style = MaterialTheme.typography.bodySmall)
          }
        } else if (state.installedApps.isNotEmpty()) {
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Aplicaciones detectadas en el dispositivo (${state.installedApps.size}):",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(6.dp))
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            state.installedApps.take(5).forEach { app ->
              Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = app.appName,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = if (app.isSystemApp) "Sistema" else "Usuario",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
            if (state.installedApps.size > 5) {
              Text(
                text = "+ ${state.installedApps.size - 5} aplicaciones más disponibles para personalización.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TestCard(
  title: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isUnlocked: Boolean,
  content: @Composable () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = androidx.compose.foundation.BorderStroke(
      width = 1.dp,
      color = if (isUnlocked) StatusGranted.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    )
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(
                if (isUnlocked) StatusGranted.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = if (isUnlocked) StatusGranted else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = title,
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = subtitle,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = if (isUnlocked) StatusGranted.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
        ) {
          Text(
            text = if (isUnlocked) "DESBLOQUEADO" else "REQUIERE PERMISO",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 9.sp
            ),
            color = if (isUnlocked) StatusGranted else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      content()
    }
  }
}

@Composable
private fun CapturedNotificationItem(notif: CapturedNotification) {
  val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
  val timeStr = timeFormat.format(Date(notif.postTime))

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = notif.appName,
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary
        )
        Text(
          text = timeStr,
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      notif.title?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface
        )
      }
      notif.text?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
      }
    }
  }
}
