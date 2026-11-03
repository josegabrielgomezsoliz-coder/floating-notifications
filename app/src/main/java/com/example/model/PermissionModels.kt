package com.example.model

enum class PermissionId {
  NOTIFICATION_LISTENER,      // 1. Acceso a notificaciones (leer para personalizar)
  OVERLAY_DISPLAY,            // 2. Mostrar sobre otras aplicaciones (elementos flotantes)
  POST_NOTIFICATIONS,         // 3. Permiso para enviar notificaciones propias
  AUTO_START,                 // 4. Inicio automático (OEM del fabricante)
  BACKGROUND_RESTRICTION,     // 5. Ejecución en segundo plano / batería sin restricciones
  FULL_SCREEN_INTENT,         // 6. Acceso a pantalla completa (alarmas/llamadas)
  VIBRATION,                  // 7. Vibración háptica
  INSTALLED_APPS,             // 8. Acceso / visibilidad de aplicaciones instaladas
  EXACT_ALARMS,               // 9a. Alarmas exactas del sistema
  NOTIFICATION_POLICY         // 9b. Acceso a No Molestar / Política de notificaciones
}

enum class PermissionCategory(val label: String) {
  OBLIGATORIO("Obligatorio"),
  RECOMENDADO("Recomendado"),
  OPCIONAL("Opcional")
}

enum class OverallStatus(val title: String, val description: String) {
  COMPLETO("Configuración completa", "Todos los permisos necesarios y recomendados están activos. La aplicación funcionará al 100% de su capacidad."),
  FALTAN_NECESARIOS("Faltan permisos necesarios", "Existen permisos obligatorios desactivados. Algunas funciones fundamentales no podrán operar."),
  OPCIONALES_PENDIENTES("Permisos opcionales pendientes", "Los permisos obligatorios están listos, pero habilitar los opcionales mejorará la experiencia en segundo plano y elementos flotantes.")
}

data class PermissionInfo(
  val id: PermissionId,
  val name: String,
  val subtitle: String,
  val utility: String,
  val affectedFeatures: List<String>,
  val category: PermissionCategory,
  val isGranted: Boolean,
  val isAvailableOnThisDevice: Boolean = true,
  val settingType: String,
  val minSdk: Int = 24,
  val maxSdk: Int? = null,
  val apiRequirementNote: String? = null,
  val iconName: String = "shield"
)

data class CapturedNotification(
  val id: String,
  val packageName: String,
  val appName: String,
  val title: String?,
  val text: String?,
  val postTime: Long
)

data class InstalledAppItem(
  val packageName: String,
  val appName: String,
  val isSystemApp: Boolean
)
