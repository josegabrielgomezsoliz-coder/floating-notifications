package com.example.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings

data class OemBrandInfo(
  val brandName: String,
  val hasSpecialAutoStartManager: Boolean,
  val stepsInstructions: List<String>,
  val targetSettingsLabel: String
)

object OemAutoStartHelper {

  fun getBrandInfo(): OemBrandInfo {
    val manufacturer = Build.MANUFACTURER.lowercase()
    return when {
      manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") -> {
        OemBrandInfo(
          brandName = "Xiaomi / Redmi / POCO (MIUI / HyperOS)",
          hasSpecialAutoStartManager = true,
          stepsInstructions = listOf(
            "1. Ve a Seguridad > Permisos > Inicio automático (Autostart).",
            "2. Localiza 'Permisos de la App' y activa el interruptor.",
            "3. En Ajustes > Batería > Ahorro de batería en apps, selecciona 'Sin restricciones'."
          ),
          targetSettingsLabel = "Abrir Inicio Automático de Xiaomi"
        )
      }
      manufacturer.contains("samsung") -> {
        OemBrandInfo(
          brandName = "Samsung (One UI)",
          hasSpecialAutoStartManager = true,
          stepsInstructions = listOf(
            "1. Ve a Ajustes > Cuidado del dispositivo / Batería.",
            "2. Entra en 'Límites de uso en segundo plano'.",
            "3. Añade 'Permisos de la App' a la lista de 'Aplicaciones que nunca se suspenden'."
          ),
          targetSettingsLabel = "Abrir Batería y Mantenimiento Samsung"
        )
      }
      manufacturer.contains("huawei") || manufacturer.contains("honor") -> {
        OemBrandInfo(
          brandName = "Huawei / Honor (EMUI / Magic UI)",
          hasSpecialAutoStartManager = true,
          stepsInstructions = listOf(
            "1. Ve a Ajustes > Batería > Inicio de aplicaciones.",
            "2. Desactiva la gestión automática para 'Permisos de la App'.",
            "3. En la ventana emergente, activa 'Inicio automático', 'Inicio secundario' y 'Ejecutar en segundo plano'."
          ),
          targetSettingsLabel = "Abrir Gestor de Inicio Huawei"
        )
      }
      manufacturer.contains("oppo") || manufacturer.contains("realme") -> {
        OemBrandInfo(
          brandName = "OPPO / Realme (ColorOS / Realme UI)",
          hasSpecialAutoStartManager = true,
          stepsInstructions = listOf(
            "1. Ve a Ajustes > Batería > Más ajustes > Inicio automático de aplicaciones.",
            "2. Activa el permiso de arranque para 'Permisos de la App'.",
            "3. En 'Optimizar batería', selecciona 'No optimizar'."
          ),
          targetSettingsLabel = "Abrir Inicio Automático ColorOS"
        )
      }
      manufacturer.contains("vivo") || manufacturer.contains("iqoo") -> {
        OemBrandInfo(
          brandName = "Vivo / iQOO (Funtouch OS / OriginOS)",
          hasSpecialAutoStartManager = true,
          stepsInstructions = listOf(
            "1. Ve a iManager > Gestión de permisos > Gestión de inicio automático.",
            "2. Permite el inicio para 'Permisos de la App'.",
            "3. En Ajustes de batería, selecciona 'Consumo de energía alto en segundo plano'."
          ),
          targetSettingsLabel = "Abrir Gestor de Inicio Vivo"
        )
      }
      manufacturer.contains("oneplus") -> {
        OemBrandInfo(
          brandName = "OnePlus (OxygenOS)",
          hasSpecialAutoStartManager = true,
          stepsInstructions = listOf(
            "1. Ve a Ajustes > Batería > Optimización de batería.",
            "2. Selecciona 'Permisos de la App' y elige 'No optimizar'.",
            "3. Verifica 'Gestión de inicio automático' en Permisos de aplicaciones."
          ),
          targetSettingsLabel = "Abrir Optimización OnePlus"
        )
      }
      manufacturer.contains("asus") -> {
        OemBrandInfo(
          brandName = "ASUS (ZenUI)",
          hasSpecialAutoStartManager = true,
          stepsInstructions = listOf(
            "1. Ve a Mobile Manager > PowerMaster > Auto-start Manager.",
            "2. Permite el inicio para 'Permisos de la App'."
          ),
          targetSettingsLabel = "Abrir Auto-start ASUS"
        )
      }
      else -> {
        OemBrandInfo(
          brandName = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} (Android Estándar)",
          hasSpecialAutoStartManager = false,
          stepsInstructions = listOf(
            "1. Tu fabricante utiliza la gestión estándar de Android.",
            "2. En Información de la aplicación, desactiva las restricciones de batería.",
            "3. Permite la ejecución en segundo plano sin restricciones."
          ),
          targetSettingsLabel = "Abrir Ajustes de la Aplicación"
        )
      }
    }
  }

  fun getAutoStartIntent(context: Context): Intent {
    val pm = context.packageManager
    val candidateIntents = listOf(
      // Xiaomi / Redmi
      Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
      Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.powercenter.PowerSettings")),
      // Huawei / Honor
      Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
      Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.bootstart.BootStartActivity")),
      Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
      // Samsung
      Intent().setComponent(ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
      Intent().setComponent(ComponentName("com.samsung.android.sm", "com.samsung.android.sm.ui.battery.BatteryActivity")),
      // Oppo / Realme
      Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
      Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
      Intent().setComponent(ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
      // Vivo
      Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
      Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.PurviewTabActivity")),
      Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.MainGuideActivity")),
      // OnePlus
      Intent().setComponent(ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity")),
      // Asus
      Intent().setComponent(ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.autostart.AutoStartActivity")),
      Intent().setComponent(ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.entry.FunctionActivity"))
    )

    for (intent in candidateIntents) {
      if (pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isNotEmpty()) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
      }
    }

    // Fallback: standard battery or application details
    return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
      data = Uri.parse("package:${context.packageName}")
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
  }
}
