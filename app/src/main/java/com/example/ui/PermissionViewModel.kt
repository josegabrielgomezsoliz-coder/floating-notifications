package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.manager.PermissionChecker
import com.example.model.CapturedNotification
import com.example.model.InstalledAppItem
import com.example.model.OverallStatus
import com.example.model.PermissionCategory
import com.example.model.PermissionId
import com.example.model.PermissionInfo
import com.example.service.AppNotificationListenerService
import com.example.service.OverlayDemoService
import com.example.util.OemAutoStartHelper
import com.example.util.OemBrandInfo
import com.example.util.TestNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PermissionFilter(val label: String) {
  ALL("Todos"),
  PENDING("Pendientes"),
  REQUIRED("Obligatorios"),
  GRANTED("Activados")
}

data class PermissionUiState(
  val permissions: List<PermissionInfo> = emptyList(),
  val overallStatus: OverallStatus = OverallStatus.FALTAN_NECESARIOS,
  val activeFilter: PermissionFilter = PermissionFilter.ALL,
  val activeTab: Int = 0, // 0: Permisos, 1: Pruebas en Vivo, 2: Fabricante & Batería
  val selectedPermission: PermissionInfo? = null,
  val isRefreshing: Boolean = false,
  val autoStartConfirmed: Boolean = false,
  val brandInfo: OemBrandInfo = OemAutoStartHelper.getBrandInfo(),
  val installedApps: List<InstalledAppItem> = emptyList(),
  val isLoadingApps: Boolean = false,
  val userMessage: String? = null,
  val testNotificationCount: Int = 0,
  val isOverlayRunning: Boolean = false,
  val isListenerConnected: Boolean = false,
  val recentCapturedNotifications: List<CapturedNotification> = emptyList()
) {
  val totalCount: Int get() = permissions.size
  val grantedCount: Int get() = permissions.count { it.isGranted }
  val requiredTotal: Int get() = permissions.count { it.category == PermissionCategory.OBLIGATORIO }
  val requiredGranted: Int get() = permissions.count { it.category == PermissionCategory.OBLIGATORIO && it.isGranted }
  val optionalTotal: Int get() = permissions.count { it.category != PermissionCategory.OBLIGATORIO }
  val optionalGranted: Int get() = permissions.count { it.category != PermissionCategory.OBLIGATORIO && it.isGranted }
  val completionPercentage: Int get() = if (totalCount > 0) (grantedCount * 100) / totalCount else 0

  val filteredPermissions: List<PermissionInfo>
    get() = when (activeFilter) {
      PermissionFilter.ALL -> permissions
      PermissionFilter.PENDING -> permissions.filter { !it.isGranted }
      PermissionFilter.REQUIRED -> permissions.filter { it.category == PermissionCategory.OBLIGATORIO }
      PermissionFilter.GRANTED -> permissions.filter { it.isGranted }
    }
}

class PermissionViewModel(application: Application) : AndroidViewModel(application) {

  private val _uiState = MutableStateFlow(PermissionUiState())
  val uiState: StateFlow<PermissionUiState> = _uiState.asStateFlow()

  init {
    refreshPermissions()

    // Observe overlay status
    viewModelScope.launch {
      OverlayDemoService.isShowingOverlay.collect { isShowing ->
        _uiState.update { it.copy(isOverlayRunning = isShowing) }
      }
    }

    // Observe notification listener connection & logs
    viewModelScope.launch {
      AppNotificationListenerService.isConnected.collect { connected ->
        _uiState.update { it.copy(isListenerConnected = connected) }
      }
    }

    viewModelScope.launch {
      AppNotificationListenerService.recentNotifications.collect { notifications ->
        _uiState.update { it.copy(recentCapturedNotifications = notifications) }
      }
    }
  }

  fun refreshPermissions() {
    viewModelScope.launch(Dispatchers.Default) {
      _uiState.update { it.copy(isRefreshing = true) }
      val context = getApplication<Application>()
      val list = PermissionChecker.getAllPermissions(context, _uiState.value.autoStartConfirmed)
      val overall = PermissionChecker.calculateOverallStatus(list)

      _uiState.update {
        it.copy(
          permissions = list,
          overallStatus = overall,
          isRefreshing = false
        )
      }
    }
  }

  fun setFilter(filter: PermissionFilter) {
    _uiState.update { it.copy(activeFilter = filter) }
  }

  fun setActiveTab(tab: Int) {
    _uiState.update { it.copy(activeTab = tab) }
    if (tab == 1 && _uiState.value.installedApps.isEmpty()) {
      loadInstalledApps()
    }
  }

  fun selectPermission(permission: PermissionInfo?) {
    _uiState.update { it.copy(selectedPermission = permission) }
  }

  fun openPermissionSettings(context: Context, id: PermissionId) {
    val success = PermissionChecker.openPermissionSettings(context, id)
    if (!success) {
      _uiState.update { it.copy(userMessage = "No se pudo abrir directamente. Ajustes abiertos.") }
    }
  }

  fun toggleAutoStartConfirmed() {
    val newConfirmed = !_uiState.value.autoStartConfirmed
    _uiState.update { it.copy(autoStartConfirmed = newConfirmed) }
    refreshPermissions()
    val msg = if (newConfirmed) {
      "Inicio automático marcado como verificado por el usuario."
    } else {
      "Inicio automático marcado como pendiente."
    }
    _uiState.update { it.copy(userMessage = msg) }
  }

  fun sendTestNotification(context: Context) {
    val currentCount = _uiState.value.testNotificationCount + 1
    val success = TestNotificationHelper.sendTestNotification(context, currentCount)
    if (success) {
      _uiState.update {
        it.copy(
          testNotificationCount = currentCount,
          userMessage = "¡Notificación #$currentCount enviada a la barra de estado!"
        )
      }
    } else {
      _uiState.update {
        it.copy(userMessage = "Error: El permiso de notificaciones no está concedido en el sistema.")
      }
    }
  }

  fun testVibration(context: Context, patternIndex: Int) {
    val success = TestNotificationHelper.triggerVibrationPattern(context, patternIndex)
    val patternName = when (patternIndex) {
      0 -> "Pulso simple (100ms)"
      1 -> "Doble toque háptico"
      else -> "Alerta rítmica de seguridad"
    }
    if (success) {
      _uiState.update { it.copy(userMessage = "Vibración reproducida: $patternName") }
    } else {
      _uiState.update { it.copy(userMessage = "El dispositivo no cuenta con hardware de vibración o permiso.") }
    }
  }

  fun toggleOverlayDemo(context: Context) {
    if (_uiState.value.isOverlayRunning) {
      OverlayDemoService.stop(context)
      _uiState.update { it.copy(isOverlayRunning = false, userMessage = "Widget flotante ocultado.") }
    } else {
      if (PermissionChecker.checkPermissionStatus(context, PermissionId.OVERLAY_DISPLAY)) {
        OverlayDemoService.start(context)
        _uiState.update { it.copy(isOverlayRunning = true, userMessage = "¡Widget flotante activado en pantalla!") }
      } else {
        _uiState.update { it.copy(userMessage = "Primero debes activar el permiso 'Mostrar sobre otras aplicaciones'.") }
        PermissionChecker.openPermissionSettings(context, PermissionId.OVERLAY_DISPLAY)
      }
    }
  }

  fun loadInstalledApps() {
    viewModelScope.launch(Dispatchers.IO) {
      _uiState.update { it.copy(isLoadingApps = true) }
      val apps = PermissionChecker.getInstalledApps(getApplication())
      _uiState.update {
        it.copy(
          installedApps = apps,
          isLoadingApps = false
        )
      }
    }
  }

  fun clearCapturedNotifications() {
    AppNotificationListenerService.clearHistory()
    _uiState.update { it.copy(userMessage = "Historial de notificaciones vaciado.") }
  }

  fun dismissMessage() {
    _uiState.update { it.copy(userMessage = null) }
  }
}
