package com.example.manager

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.Vibrator
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.model.InstalledAppItem
import com.example.model.OverallStatus
import com.example.model.PermissionCategory
import com.example.model.PermissionId
import com.example.model.PermissionInfo
import com.example.service.AppNotificationListenerService
import com.example.util.OemAutoStartHelper

object PermissionChecker {

  fun checkPermissionStatus(context: Context, id: PermissionId, autoStartManuallyConfirmed: Boolean = false): Boolean {
    return when (id) {
      PermissionId.NOTIFICATION_LISTENER -> {
        AppNotificationListenerService.isPermissionGranted(context)
      }
      PermissionId.OVERLAY_DISPLAY -> {
        Settings.canDrawOverlays(context)
      }
      PermissionId.POST_NOTIFICATIONS -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
              PackageManager.PERMISSION_GRANTED &&
              NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
          NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
      }
      PermissionId.AUTO_START -> {
        // En Android estándar no hay API pública para leer el toggle de OEM privado,
        // pero verificamos si la exención de batería está activa o si el usuario confirmó el ajuste.
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isIgnoringBattery = pm?.isIgnoringBatteryOptimizations(context.packageName) == true
        isIgnoringBattery || autoStartManuallyConfirmed
      }
      PermissionId.BACKGROUND_RESTRICTION -> {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        pm?.isIgnoringBatteryOptimizations(context.packageName) == true
      }
      PermissionId.FULL_SCREEN_INTENT -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
          val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
          nm?.canUseFullScreenIntent() ?: true
        } else {
          true // En Android 13 o inferior viene concedido por defecto en manifest
        }
      }
      PermissionId.VIBRATION -> {
        val hasVibratePermission = ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.VIBRATE
        ) == PackageManager.PERMISSION_GRANTED
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        hasVibratePermission && (vibrator?.hasVibrator() == true)
      }
      PermissionId.INSTALLED_APPS -> {
        // Comprobar si podemos listar paquetes instalados con lanzador
        try {
          val pm = context.packageManager
          val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
          val resolveInfos = pm.queryIntentActivities(launcherIntent, 0)
          resolveInfos.isNotEmpty()
        } catch (_: Exception) {
          false
        }
      }
      PermissionId.EXACT_ALARMS -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
          am?.canScheduleExactAlarms() ?: true
        } else {
          true
        }
      }
      PermissionId.NOTIFICATION_POLICY -> {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.isNotificationPolicyAccessGranted == true
      }
    }
  }

  fun getAllPermissions(context: Context, autoStartConfirmed: Boolean = false): List<PermissionInfo> {
    val list = mutableListOf<PermissionInfo>()

    // 1. Acceso a notificaciones
    list.add(
      PermissionInfo(
        id = PermissionId.NOTIFICATION_LISTENER,
        name = "Acceso a notificaciones",
        subtitle = "Lectura e interceptación segura de avisos",
        utility = "Permite leer las notificaciones recibidas en el dispositivo para personalizar su presentación, agruparlas y aplicar reglas inteligentes.",
        affectedFeatures = listOf(
          "No se podrán leer ni personalizar las notificaciones entrantes.",
          "El historial en vivo de avisos estará deshabilitado.",
          "Las reglas automáticas por aplicación no se ejecutarán."
        ),
        category = PermissionCategory.OBLIGATORIO,
        isGranted = checkPermissionStatus(context, PermissionId.NOTIFICATION_LISTENER, autoStartConfirmed),
        settingType = "Acceso especial del sistema",
        iconName = "notifications_active"
      )
    )

    // 2. Mostrar sobre otras aplicaciones
    list.add(
      PermissionInfo(
        id = PermissionId.OVERLAY_DISPLAY,
        name = "Mostrar sobre otras aplicaciones",
        subtitle = "Superposición de elementos flotantes",
        utility = "Permite mostrar burbujas, ventanas emergentes y widgets flotantes interactivos por encima de cualquier otra pantalla.",
        affectedFeatures = listOf(
          "Los paneles flotantes y burbujas de estado no aparecerán sobre otras apps.",
          "No se podrán mostrar avisos prioritarios inmediatos mientras juegas o lees."
        ),
        category = PermissionCategory.RECOMENDADO,
        isGranted = checkPermissionStatus(context, PermissionId.OVERLAY_DISPLAY, autoStartConfirmed),
        settingType = "Permiso especial de superposición",
        iconName = "layers"
      )
    )

    // 3. Permiso para enviar notificaciones
    val isTiramisu = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    list.add(
      PermissionInfo(
        id = PermissionId.POST_NOTIFICATIONS,
        name = "Enviar notificaciones",
        subtitle = "Publicación de alertas propias en la barra de estado",
        utility = "Permite publicar alertas, recordatorios de estado del sistema y avisos críticos en la barra de notificaciones.",
        affectedFeatures = listOf(
          "La aplicación no podrá mostrar ninguna alerta en la barra superior.",
          "Los estados de fondo y avisos de seguridad pasarán inadvertidos."
        ),
        category = PermissionCategory.OBLIGATORIO,
        isGranted = checkPermissionStatus(context, PermissionId.POST_NOTIFICATIONS, autoStartConfirmed),
        settingType = if (isTiramisu) "Permiso de tiempo de ejecución (Android 13+)" else "Ajustes de notificaciones de la app",
        minSdk = 24,
        apiRequirementNote = if (isTiramisu) "Requerido a partir de Android 13 (API 33)" else null,
        iconName = "notification_add"
      )
    )

    // 4. Inicio automático (OEM)
    val brandInfo = OemAutoStartHelper.getBrandInfo()
    list.add(
      PermissionInfo(
        id = PermissionId.AUTO_START,
        name = "Inicio automático del fabricante",
        subtitle = "Gestión de arranque para ${brandInfo.brandName}",
        utility = "Evita que las capas de personalización (MIUI, One UI, EMUI, ColorOS) bloqueen los servicios de la app al reiniciar el teléfono o limpiar la memoria.",
        affectedFeatures = listOf(
          "El sistema del fabricante podría cerrar la aplicación en segundo plano tras un tiempo.",
          "Los servicios de escucha no arrancarán tras reiniciar el dispositivo."
        ),
        category = if (brandInfo.hasSpecialAutoStartManager) PermissionCategory.RECOMENDADO else PermissionCategory.OPCIONAL,
        isGranted = checkPermissionStatus(context, PermissionId.AUTO_START, autoStartConfirmed),
        settingType = "Ajuste específico del fabricante",
        iconName = "power_settings_new"
      )
    )

    // 5. Ejecución en segundo plano / Batería
    list.add(
      PermissionInfo(
        id = PermissionId.BACKGROUND_RESTRICTION,
        name = "Ejecución en segundo plano",
        subtitle = "Exención de optimización de batería Doze",
        utility = "Configura la aplicación como 'Sin restricciones' de batería para que el sistema Doze de Android no congele sus procesos de supervisión.",
        affectedFeatures = listOf(
          "Android detendrá la monitorización periódica cuando la pantalla esté apagada.",
          "Retraso o congelación de respuestas a eventos de notificaciones."
        ),
        category = PermissionCategory.OBLIGATORIO,
        isGranted = checkPermissionStatus(context, PermissionId.BACKGROUND_RESTRICTION, autoStartConfirmed),
        settingType = "Ajuste de ahorro de batería",
        iconName = "battery_charging_full"
      )
    )

    // 6. Acceso a pantalla completa
    val isUpsideDownCake = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    list.add(
      PermissionInfo(
        id = PermissionId.FULL_SCREEN_INTENT,
        name = "Acceso a pantalla completa",
        subtitle = "Alertas a pantalla completa con dispositivo bloqueado",
        utility = "Permite presentar alarmas y eventos críticos ocupando toda la pantalla incluso cuando el teléfono esté bloqueado.",
        affectedFeatures = listOf(
          "No se podrán mostrar llamadas o avisos de emergencia a pantalla completa cuando el teléfono esté bloqueado."
        ),
        category = PermissionCategory.OPCIONAL,
        isGranted = checkPermissionStatus(context, PermissionId.FULL_SCREEN_INTENT, autoStartConfirmed),
        settingType = if (isUpsideDownCake) "Permiso especial (Android 14+)" else "Concedido por manifiesto",
        minSdk = 24,
        apiRequirementNote = if (isUpsideDownCake) "Ajustable por el usuario en Android 14+ (API 34)" else "Habilitado por defecto en versiones anteriores",
        iconName = "fullscreen"
      )
    )

    // 7. Vibración
    list.add(
      PermissionInfo(
        id = PermissionId.VIBRATION,
        name = "Vibración y respuesta háptica",
        subtitle = "Motor háptico para alertas personalizadas",
        utility = "Permite reproducir patrones de vibración específicos para distinguir alertas importantes sin encender la pantalla.",
        affectedFeatures = listOf(
          "Las notificaciones no podrán emitir patrones táctiles personalizados.",
          "La retroalimentación táctil de la aplicación quedará desactivada."
        ),
        category = PermissionCategory.RECOMENDADO,
        isGranted = checkPermissionStatus(context, PermissionId.VIBRATION, autoStartConfirmed),
        settingType = "Permiso estándar de hardware",
        iconName = "vibration"
      )
    )

    // 8. Visibilidad de aplicaciones instaladas
    list.add(
      PermissionInfo(
        id = PermissionId.INSTALLED_APPS,
        name = "Acceso a aplicaciones instaladas",
        subtitle = "Visibilidad para preferencias individuales",
        utility = "Permite consultar la lista de aplicaciones instaladas en el dispositivo para configurar filtros, exclusiones y tratamientos personalizados por cada una.",
        affectedFeatures = listOf(
          "No podrás seleccionar qué aplicaciones supervisar o ignorar en la lista.",
          "La personalización selectiva no estará disponible."
        ),
        category = PermissionCategory.RECOMENDADO,
        isGranted = checkPermissionStatus(context, PermissionId.INSTALLED_APPS, autoStartConfirmed),
        settingType = "Visibilidad de paquetes (Android Queries)",
        iconName = "apps"
      )
    )

    // 9. Alarmas exactas
    val isSnowCone = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    list.add(
      PermissionInfo(
        id = PermissionId.EXACT_ALARMS,
        name = "Alarmas y recordatorios exactos",
        subtitle = "Programación precisa en segundo plano",
        utility = "Garantiza que los temporizadores y tareas programadas se disparen en el segundo exacto, sin retrasos provocados por el sistema operativo.",
        affectedFeatures = listOf(
          "Las comprobaciones programadas podrían retrasarse varios minutos.",
          "Los recordatorios horarios no sonarán a tiempo exacto."
        ),
        category = PermissionCategory.OPCIONAL,
        isGranted = checkPermissionStatus(context, PermissionId.EXACT_ALARMS, autoStartConfirmed),
        settingType = if (isSnowCone) "Acceso especial de alarmas (Android 12+)" else "Estándar",
        minSdk = 24,
        apiRequirementNote = if (isSnowCone) "Gestionable en Android 12+ (API 31)" else null,
        iconName = "alarm"
      )
    )

    // 10. Acceso a No Molestar
    list.add(
      PermissionInfo(
        id = PermissionId.NOTIFICATION_POLICY,
        name = "Acceso a No Molestar (DND)",
        subtitle = "Política de prioridad y silencio",
        utility = "Permite omitir temporalmente el modo No Molestar únicamente para alertas de máxima emergencia configuradas por el usuario.",
        affectedFeatures = listOf(
          "Las alertas críticas no sonarán si el usuario tiene el modo No Molestar activado."
        ),
        category = PermissionCategory.OPCIONAL,
        isGranted = checkPermissionStatus(context, PermissionId.NOTIFICATION_POLICY, autoStartConfirmed),
        settingType = "Acceso especial a política de notificaciones",
        iconName = "do_not_disturb_on"
      )
    )

    return list
  }

  fun calculateOverallStatus(permissions: List<PermissionInfo>): OverallStatus {
    val missingRequired = permissions.any { it.category == PermissionCategory.OBLIGATORIO && !it.isGranted }
    if (missingRequired) return OverallStatus.FALTAN_NECESARIOS

    val missingOptional = permissions.any {
      (it.category == PermissionCategory.RECOMENDADO || it.category == PermissionCategory.OPCIONAL) && !it.isGranted
    }
    if (missingOptional) return OverallStatus.OPCIONALES_PENDIENTES

    return OverallStatus.COMPLETO
  }

  fun openPermissionSettings(context: Context, id: PermissionId): Boolean {
    return try {
      val intent = when (id) {
        PermissionId.NOTIFICATION_LISTENER -> {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).apply {
              putExtra(
                Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                AppNotificationListenerService.getComponentName(context).flattenToString()
              )
            }
          } else {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
          }
        }
        PermissionId.OVERLAY_DISPLAY -> {
          Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
          )
        }
        PermissionId.POST_NOTIFICATIONS -> {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
              putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
          } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
              data = Uri.parse("package:${context.packageName}")
            }
          }
        }
        PermissionId.AUTO_START -> {
          OemAutoStartHelper.getAutoStartIntent(context)
        }
        PermissionId.BACKGROUND_RESTRICTION -> {
          Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:${context.packageName}")
          )
        }
        PermissionId.FULL_SCREEN_INTENT -> {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Intent(
              Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
              Uri.parse("package:${context.packageName}")
            )
          } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
              data = Uri.parse("package:${context.packageName}")
            }
          }
        }
        PermissionId.VIBRATION -> {
          Intent(Settings.ACTION_SOUND_SETTINGS)
        }
        PermissionId.INSTALLED_APPS -> {
          Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
          }
        }
        PermissionId.EXACT_ALARMS -> {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(
              Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
              Uri.parse("package:${context.packageName}")
            )
          } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
              data = Uri.parse("package:${context.packageName}")
            }
          }
        }
        PermissionId.NOTIFICATION_POLICY -> {
          Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        }
      }

      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(intent)
      true
    } catch (_: Exception) {
      // Fallback seguro a los detalles de la app en caso de ROMs personalizadas
      try {
        val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
          data = Uri.parse("package:${context.packageName}")
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(fallbackIntent)
        true
      } catch (_: Exception) {
        false
      }
    }
  }

  fun getInstalledApps(context: Context): List<InstalledAppItem> {
    return try {
      val pm = context.packageManager
      val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
      val resolveInfos = pm.queryIntentActivities(launcherIntent, 0)
      resolveInfos.mapNotNull { resolveInfo ->
        val appInfo = resolveInfo.activityInfo?.applicationInfo ?: return@mapNotNull null
        val appName = pm.getApplicationLabel(appInfo).toString()
        val pkg = appInfo.packageName
        val isSystem = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
        InstalledAppItem(
          packageName = pkg,
          appName = appName,
          isSystemApp = isSystem
        )
      }.sortedBy { it.appName.lowercase() }
    } catch (_: Exception) {
      emptyList()
    }
  }
}
