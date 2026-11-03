package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.PermissionId
import com.example.ui.components.LiveTestSection
import com.example.ui.components.OemManufacturerSection
import com.example.ui.components.PermissionItemCard
import com.example.ui.components.StatusIndicatorCard
import com.example.ui.theme.StatusGranted
import com.example.ui.theme.StatusMissing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionManagerScreen(
  viewModel: PermissionViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(state.userMessage) {
    state.userMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      viewModel.dismissMessage()
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Permisos de la Aplicación",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Centro de Control y Accesos Especiales",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        actions = {
          IconButton(
            onClick = { viewModel.refreshPermissions() },
            modifier = Modifier.testTag("top_refresh_button")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Comprobar permisos",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { viewModel.refreshPermissions() },
        modifier = Modifier.testTag("fab_refresh"),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = CircleShape
      ) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = "Comprobar nuevamente todos los permisos"
        )
      }
    },
    snackbarHost = { SnackbarHost(snackbarHostState) },
    contentWindowInsets = WindowInsets(0, 0, 0, 0)
  ) { innerPadding ->
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(
          top = innerPadding.calculateTopPadding(),
          bottom = navBarPadding
        )
    ) {
      // Primary Tab Row
      PrimaryTabRow(
        selectedTabIndex = state.activeTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
      ) {
        Tab(
          selected = state.activeTab == 0,
          onClick = { viewModel.setActiveTab(0) },
          text = {
            Text(
              text = "Permisos (${state.totalCount})",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
          },
          icon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp)) },
          modifier = Modifier.testTag("tab_permissions")
        )
        Tab(
          selected = state.activeTab == 1,
          onClick = { viewModel.setActiveTab(1) },
          text = {
            Text(
              text = "Pruebas",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
          },
          icon = { Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp)) },
          modifier = Modifier.testTag("tab_tests")
        )
        Tab(
          selected = state.activeTab == 2,
          onClick = { viewModel.setActiveTab(2) },
          text = {
            Text(
              text = "Fabricante",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
          },
          icon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(18.dp)) },
          modifier = Modifier.testTag("tab_manufacturer")
        )
      }

      // Content for active tab
      when (state.activeTab) {
        0 -> {
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            item {
              Spacer(modifier = Modifier.height(10.dp))
              // General Health Indicator Card
              StatusIndicatorCard(
                state = state,
                onRefreshClick = { viewModel.refreshPermissions() }
              )
            }

            item {
              // Filter chips bar
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                PermissionFilter.values().forEach { filter ->
                  val count = when (filter) {
                    PermissionFilter.ALL -> state.totalCount
                    PermissionFilter.PENDING -> state.totalCount - state.grantedCount
                    PermissionFilter.REQUIRED -> state.requiredTotal
                    PermissionFilter.GRANTED -> state.grantedCount
                  }
                  FilterChip(
                    selected = state.activeFilter == filter,
                    onClick = { viewModel.setFilter(filter) },
                    label = {
                      Text(
                        text = "${filter.label} ($count)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                      )
                    },
                    modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                      selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                  )
                }
              }
            }

            items(
              items = state.filteredPermissions,
              key = { it.id.name }
            ) { perm ->
              PermissionItemCard(
                permission = perm,
                onConfigureClick = {
                  viewModel.openPermissionSettings(context, perm.id)
                },
                onDetailsClick = {
                  viewModel.selectPermission(perm)
                },
                onTestClick = when (perm.id) {
                  PermissionId.POST_NOTIFICATIONS -> {
                    { viewModel.sendTestNotification(context) }
                  }
                  PermissionId.VIBRATION -> {
                    { viewModel.testVibration(context, 1) }
                  }
                  PermissionId.OVERLAY_DISPLAY -> {
                    { viewModel.toggleOverlayDemo(context) }
                  }
                  PermissionId.INSTALLED_APPS -> {
                    { viewModel.setActiveTab(1) }
                  }
                  PermissionId.AUTO_START -> {
                    { viewModel.setActiveTab(2) }
                  }
                  else -> null
                }
              )
            }

            item {
              Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
            }
          }
        }
        1 -> {
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            item {
              Spacer(modifier = Modifier.height(10.dp))
              LiveTestSection(
                state = state,
                onSendNotification = { viewModel.sendTestNotification(context) },
                onTestVibration = { pattern -> viewModel.testVibration(context, pattern) },
                onToggleOverlay = { viewModel.toggleOverlayDemo(context) },
                onLoadApps = { viewModel.loadInstalledApps() },
                onClearCapturedNotifications = { viewModel.clearCapturedNotifications() }
              )
            }
            item {
              Spacer(modifier = Modifier.height(80.dp))
            }
          }
        }
        2 -> {
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            item {
              Spacer(modifier = Modifier.height(10.dp))
              OemManufacturerSection(
                state = state,
                onOpenOemSettings = {
                  viewModel.openPermissionSettings(context, PermissionId.AUTO_START)
                },
                onToggleAutoStartConfirmed = {
                  viewModel.toggleAutoStartConfirmed()
                },
                onOpenBatteryOptimization = {
                  viewModel.openPermissionSettings(context, PermissionId.BACKGROUND_RESTRICTION)
                }
              )
            }
            item {
              Spacer(modifier = Modifier.height(80.dp))
            }
          }
        }
      }
    }
  }
}
