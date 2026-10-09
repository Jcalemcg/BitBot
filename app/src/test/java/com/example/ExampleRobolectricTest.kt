package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.engine.BackendSelectorEngine
import com.example.data.engine.DatasetValidator
import com.example.data.engine.OomRisk
import com.example.data.model.ModelScale
import com.example.data.model.QuantFormat
import com.example.data.model.TrainingPathId
import com.example.data.scanner.DeviceHardwareProfile
import com.example.data.scanner.DeviceHardwareScanner
import com.example.data.scanner.DeviceTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    assertEquals("OnDeviceML", appName)
  }

  @Test
  fun `device hardware scanner executes and detects memory`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val profile = DeviceHardwareScanner.scan(context)
    assertNotNull(profile)
    assertTrue(profile.totalRamGb > 0f)
    assertTrue(profile.cpuCores >= 1)
    assertNotNull(profile.tier)
    assertEquals(4, profile.checkBadges.size)
  }

  @Test
  fun `backend selector evaluates sweet spot on flagship device profile`() {
    val flagshipProfile = DeviceHardwareProfile(
      deviceModel = "Test Flagship",
      manufacturer = "TestBrand",
      hardwareName = "flagship_chip",
      totalRamGb = 12.0f,
      availableRamGb = 8.5f,
      cpuCores = 8,
      hasVulkanSupport = true,
      batteryPct = 90,
      isCharging = false,
      tier = DeviceTier.TIER_S,
      checkBadges = emptyList()
    )

    val rec = BackendSelectorEngine.evaluate(
      modelScale = ModelScale.SCALE_1B,
      quantFormat = QuantFormat.TQ2_0,
      deviceProfile = flagshipProfile
    )
    assertEquals(TrainingPathId.PATH_A, rec.recommendedPath)
    assertFalse(rec.isLocked)
    assertTrue(rec.isSweetSpot)
    assertEquals(OomRisk.SAFE, rec.oomRisk)
  }

  @Test
  fun `backend selector locks 7B model due to memory shortfall on constrained device`() {
    val constrainedProfile = DeviceHardwareProfile(
      deviceModel = "Constrained Device",
      manufacturer = "Budget",
      hardwareName = "low_end_chip",
      totalRamGb = 3.8f,
      availableRamGb = 1.2f,
      cpuCores = 4,
      hasVulkanSupport = false,
      batteryPct = 50,
      isCharging = false,
      tier = DeviceTier.TIER_C,
      checkBadges = emptyList()
    )

    val rec = BackendSelectorEngine.evaluate(
      modelScale = ModelScale.SCALE_7B,
      quantFormat = QuantFormat.TQ2_0,
      deviceProfile = constrainedProfile
    )
    assertTrue(rec.isLocked)
    assertNotNull(rec.lockReason)
    assertEquals(OomRisk.FATAL_OOM, rec.oomRisk)
    assertEquals(TrainingPathId.PATH_D, rec.recommendedPath)
    assertEquals(0, rec.feasibilityScorePct)
  }

  @Test
  fun `dataset validator rejects dataset over 50k tokens cap`() {
    val inspection = DatasetValidator.validateDataset(
      docCount = 1000,
      totalTokens = 65000
    )
    assertFalse(inspection.isUnderCap)
    assertFalse(inspection.isClean)
    assertTrue(inspection.warnings.isNotEmpty())
  }
}
