package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NotificationAdd
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PermissionCategory
import com.example.model.PermissionId
import com.example.model.PermissionInfo
import com.example.ui.theme.StatusGranted
import com.example.ui.theme.StatusMissing
import com.example.ui.theme.StatusPending

@Composable
fun PermissionItemCard(
  permission: PermissionInfo,
  onConfigureClick: () -> Unit,
  onDetailsClick: () -> Unit,
  onTestClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var isExpanded by remember { mutableStateOf(false) }

  val icon = getIconForPermission(permission.iconName)
  val isGranted = permission.isGranted

  val (statusText, statusBg, statusColor, statusBorder) = if (isGranted) {
    Quadruple(
      "ACTIVADO",
      Color(0xFF064E3B).copy(alpha = 0.2f),
      StatusGranted,
      StatusGranted.copy(alpha = 0.5f)
    )
  } else {
    Quadruple(
      "DESACTIVADO",
      Color(0xFF7F1D1D).copy(alpha = 0.2f),
      StatusMissing,
      StatusMissing.copy(alpha = 0.5f)
    )
  }

  val categoryColor = when (permission.category) {
    PermissionCategory.OBLIGATORIO -> if (isGranted) StatusGranted else StatusMissing
    PermissionCategory.RECOMENDADO -> MaterialTheme.colorScheme.primary
    PermissionCategory.OPCIONAL -> MaterialTheme.colorScheme.onSurfaceVariant
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("permission_card_${permission.id.name.lowercase()}"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = androidx.compose.foundation.BorderStroke(
      width = if (!isGranted && permission.category == PermissionCategory.OBLIGATORIO) 1.5.dp else 1.dp,
      color = if (!isGranted && permission.category == PermissionCategory.OBLIGATORIO) {
        StatusMissing.copy(alpha = 0.4f)
      } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
      }
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // Header row with Icon, Titles & Status Pill
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(
                if (isGranted) {
                  StatusGranted.copy(alpha = 0.12f)
                } else {
                  MaterialTheme.colorScheme.surfaceVariant
                }
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = icon,
              contentDescription = permission.name,
              tint = if (isGranted) StatusGranted else MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = permission.name,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Text(
              text = permission.subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Status badge: ACTIVADO / DESACTIVADO
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = statusBg,
          border = androidx.compose.foundation.BorderStroke(1.dp, statusBorder),
          modifier = Modifier.testTag("status_badge_${permission.id.name.lowercase()}")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Close,
              contentDescription = statusText,
              tint = statusColor,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = statusText,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp
              ),
              color = statusColor
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Category and Setting Type chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = categoryColor.copy(alpha = 0.12f)
        ) {
          Text(
            text = permission.category.label.uppercase(),
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp
            ),
            color = categoryColor
          )
        }

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = MaterialTheme.colorScheme.surfaceVariant
        ) {
          Text(
            text = permission.settingType,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Utility description
      Text(
        text = permission.utility,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
        lineHeight = 18.sp
      )

      // Warning about affected features if not granted
      if (!isGranted && permission.affectedFeatures.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF7F1D1D).copy(alpha = 0.08f))
            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(10.dp)
        ) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = StatusMissing,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Funcionalidades no disponibles:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = StatusMissing
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            permission.affectedFeatures.forEach { feature ->
              Text(
                text = "• $feature",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
              )
            }
          }
        }
      }

      // Expandable details section
      AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
        ) {
          permission.apiRequirementNote?.let { note ->
            Text(
              text = "Compatibilidad: $note",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Action row: Configure button + Details / Test buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Toggle expand
        Row(
          modifier = Modifier
            .clickable { isExpanded = !isExpanded }
            .padding(vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isExpanded) "Menos info" else "Más info",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
          )
          Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          // If granted and has test
          if (isGranted && onTestClick != null) {
            OutlinedButton(
              onClick = onTestClick,
              modifier = Modifier.testTag("test_action_${permission.id.name.lowercase()}"),
              shape = RoundedCornerShape(10.dp),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = "Probar",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
              )
            }
          }

          // Button to open corresponding Android settings screen
          Button(
            onClick = onConfigureClick,
            modifier = Modifier.testTag("configure_button_${permission.id.name.lowercase()}"),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isGranted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
              contentColor = if (isGranted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text(
              text = if (isGranted) "Ajustes de Android" else "Activar Permiso",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }
    }
  }
}

private fun getIconForPermission(iconName: String): ImageVector {
  return when (iconName) {
    "notifications_active" -> Icons.Default.NotificationsActive
    "layers" -> Icons.Default.Layers
    "notification_add" -> Icons.Default.NotificationAdd
    "power_settings_new" -> Icons.Default.PowerSettingsNew
    "battery_charging_full" -> Icons.Default.BatteryChargingFull
    "fullscreen" -> Icons.Default.Fullscreen
    "vibration" -> Icons.Default.Vibration
    "apps" -> Icons.Default.Apps
    "alarm" -> Icons.Default.Alarm
    "do_not_disturb_on" -> Icons.Default.DoNotDisturbOn
    else -> Icons.Default.Security
  }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
