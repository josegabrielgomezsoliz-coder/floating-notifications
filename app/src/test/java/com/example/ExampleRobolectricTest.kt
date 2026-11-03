package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.manager.PermissionChecker
import com.example.model.OverallStatus
import com.example.model.PermissionCategory
import com.example.model.PermissionId
import com.example.model.PermissionInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Permisos de la App", appName)
  }

  @Test
  fun `verify all required permissions are enumerated`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val permissions = PermissionChecker.getAllPermissions(context)

    // Verify all 10 managed permissions exist
    val ids = permissions.map { it.id }
    assertTrue(ids.contains(PermissionId.NOTIFICATION_LISTENER))
    assertTrue(ids.contains(PermissionId.OVERLAY_DISPLAY))
    assertTrue(ids.contains(PermissionId.POST_NOTIFICATIONS))
    assertTrue(ids.contains(PermissionId.AUTO_START))
    assertTrue(ids.contains(PermissionId.BACKGROUND_RESTRICTION))
    assertTrue(ids.contains(PermissionId.FULL_SCREEN_INTENT))
    assertTrue(ids.contains(PermissionId.VIBRATION))
    assertTrue(ids.contains(PermissionId.INSTALLED_APPS))
    assertTrue(ids.contains(PermissionId.EXACT_ALARMS))
    assertTrue(ids.contains(PermissionId.NOTIFICATION_POLICY))
  }

  @Test
  fun `verify overall status calculation logic`() {
    // 1. Missing required -> FALTAN_NECESARIOS
    val testList1 = listOf(
      PermissionInfo(
        id = PermissionId.NOTIFICATION_LISTENER,
        name = "Test",
        subtitle = "Sub",
        utility = "Util",
        affectedFeatures = emptyList(),
        category = PermissionCategory.OBLIGATORIO,
        isGranted = false,
        settingType = "Special"
      )
    )
    assertEquals(OverallStatus.FALTAN_NECESARIOS, PermissionChecker.calculateOverallStatus(testList1))

    // 2. Required granted, optional missing -> OPCIONALES_PENDIENTES
    val testList2 = listOf(
      PermissionInfo(
        id = PermissionId.NOTIFICATION_LISTENER,
        name = "Req",
        subtitle = "Sub",
        utility = "Util",
        affectedFeatures = emptyList(),
        category = PermissionCategory.OBLIGATORIO,
        isGranted = true,
        settingType = "Special"
      ),
      PermissionInfo(
        id = PermissionId.OVERLAY_DISPLAY,
        name = "Opt",
        subtitle = "Sub",
        utility = "Util",
        affectedFeatures = emptyList(),
        category = PermissionCategory.RECOMENDADO,
        isGranted = false,
        settingType = "Special"
      )
    )
    assertEquals(OverallStatus.OPCIONALES_PENDIENTES, PermissionChecker.calculateOverallStatus(testList2))

    // 3. All granted -> COMPLETO
    val testList3 = listOf(
      PermissionInfo(
        id = PermissionId.NOTIFICATION_LISTENER,
        name = "Req",
        subtitle = "Sub",
        utility = "Util",
        affectedFeatures = emptyList(),
        category = PermissionCategory.OBLIGATORIO,
        isGranted = true,
        settingType = "Special"
      ),
      PermissionInfo(
        id = PermissionId.OVERLAY_DISPLAY,
        name = "Opt",
        subtitle = "Sub",
        utility = "Util",
        affectedFeatures = emptyList(),
        category = PermissionCategory.RECOMENDADO,
        isGranted = true,
        settingType = "Special"
      )
    )
    assertEquals(OverallStatus.COMPLETO, PermissionChecker.calculateOverallStatus(testList3))
  }
}
