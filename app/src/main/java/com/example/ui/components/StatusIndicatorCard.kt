package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OverallStatus
import com.example.ui.PermissionUiState
import com.example.ui.theme.StatusGranted
import com.example.ui.theme.StatusMissing
import com.example.ui.theme.StatusPending

@Composable
fun StatusIndicatorCard(
  state: PermissionUiState,
  onRefreshClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val animatedProgress by animateFloatAsState(
    targetValue = state.completionPercentage / 100f,
    animationSpec = tween(600),
    label = "progress_anim"
  )

  val (statusColor, statusBg, statusIcon) = when (state.overallStatus) {
    OverallStatus.COMPLETO -> Triple(
      StatusGranted,
      Color(0xFF064E3B).copy(alpha = 0.25f),
      Icons.Default.CheckCircle
    )
    OverallStatus.FALTAN_NECESARIOS -> Triple(
      StatusMissing,
      Color(0xFF7F1D1D).copy(alpha = 0.25f),
      Icons.Default.Error
    )
    OverallStatus.OPCIONALES_PENDIENTES -> Triple(
      StatusPending,
      Color(0xFF78350F).copy(alpha = 0.25f),
      Icons.Default.Info
    )
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("status_indicator_card"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              statusColor.copy(alpha = 0.12f),
              Color.Transparent
            )
          )
        )
        .padding(20.dp)
    ) {
      // Top status row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(statusBg)
              .border(1.5.dp, statusColor.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = statusIcon,
              contentDescription = "Estado de permisos",
              tint = statusColor,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = "Estado General",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = state.overallStatus.title,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = statusColor
            )
          }
        }

        // Percentage badge
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = statusColor.copy(alpha = 0.15f),
          border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
        ) {
          Text(
            text = "${state.completionPercentage}%",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              fontSize = 15.sp
            ),
            color = statusColor
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = state.overallStatus.description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
        lineHeight = 18.sp
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Progress bar
      LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(RoundedCornerShape(4.dp)),
        color = statusColor,
        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Breakdown counts and refresh button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
          CountBadge(
            label = "Obligatorios",
            current = state.requiredGranted,
            total = state.requiredTotal,
            color = if (state.requiredGranted == state.requiredTotal) StatusGranted else StatusMissing
          )
          CountBadge(
            label = "Opcionales",
            current = state.optionalGranted,
            total = state.optionalTotal,
            color = if (state.optionalGranted == state.optionalTotal) StatusGranted else StatusPending
          )
        }

        Button(
          onClick = onRefreshClick,
          modifier = Modifier.testTag("recheck_permissions_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
          ),
          shape = RoundedCornerShape(12.dp),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Comprobar nuevamente",
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Comprobar",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
          )
        }
      }
    }
  }
}

@Composable
private fun CountBadge(
  label: String,
  current: Int,
  total: Int,
  color: Color
) {
  Column {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(color)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "$current / $total",
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}
