package com.example.ui.components

import android.os.Build
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PermissionUiState
import com.example.ui.theme.StatusGranted
import com.example.ui.theme.StatusMissing
import com.example.ui.theme.StatusPending

@Composable
fun OemManufacturerSection(
  state: PermissionUiState,
  onOpenOemSettings: () -> Unit,
  onToggleAutoStartConfirmed: () -> Unit,
  onOpenBatteryOptimization: () -> Unit,
  modifier: Modifier = Modifier
) {
  val brand = state.brandInfo

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Brand detection banner
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
      )
    ) {
      Row(
        modifier = Modifier.padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.PhoneAndroid,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp)
          )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
          Text(
            text = "Fabricante Detectado",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = brand.brandName,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Dispositivo: ${Build.MANUFACTURER.uppercase()} ${Build.MODEL} • Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Auto-start Guide Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
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
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                  if (state.autoStartConfirmed) StatusGranted.copy(alpha = 0.15f) else StatusPending.copy(alpha = 0.15f)
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = null,
                tint = if (state.autoStartConfirmed) StatusGranted else StatusPending,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Inicio Automático (Autostart)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (brand.hasSpecialAutoStartManager) "Gestión personalizada del fabricante" else "Gestión estándar de AOSP",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (state.autoStartConfirmed) StatusGranted.copy(alpha = 0.15f) else StatusMissing.copy(alpha = 0.15f)
          ) {
            Text(
              text = if (state.autoStartConfirmed) "VERIFICADO" else "PENDIENTE",
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              ),
              color = if (state.autoStartConfirmed) StatusGranted else StatusMissing
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Muchos fabricantes incluyen administradores de energía agresivos que cierran servicios legítimos en segundo plano tras reiniciar el teléfono o tras unos minutos de inactividad.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
          lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Step by step box
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(14.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = "Instrucciones paso a paso para ${brand.brandName}:",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.primary
            )
            brand.stepsInstructions.forEach { step ->
              Text(
                text = step,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Button(
            onClick = onOpenOemSettings,
            modifier = Modifier.fillMaxWidth().testTag("open_oem_autostart_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = brand.targetSettingsLabel,
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
          }

          OutlinedButton(
            onClick = onToggleAutoStartConfirmed,
            modifier = Modifier.fillMaxWidth().testTag("confirm_autostart_toggle_button"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(
              imageVector = if (state.autoStartConfirmed) Icons.Default.Close else Icons.Default.Check,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = if (state.autoStartConfirmed) StatusMissing else StatusGranted
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (state.autoStartConfirmed) "Marcar como no configurado" else "Marcar como comprobado",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
          }
        }
      }
    }

    // Battery Optimization Guide Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.BatteryAlert,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Optimización de Batería Android (Doze)",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Restricciones de consumo en segundo plano",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Para que la aplicación pueda sincronizar y personalizar notificaciones en reposo, debes configurar el uso de batería como 'Sin restricciones' o excluirla de la optimización del sistema.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
          lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
          onClick = onOpenBatteryOptimization,
          modifier = Modifier.fillMaxWidth().testTag("open_battery_settings_button"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text(
            text = "Gestionar Optimización de Batería",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }
  }
}
