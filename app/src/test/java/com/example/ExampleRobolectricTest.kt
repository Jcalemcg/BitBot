package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.engine.BackendSelectorEngine
import com.example.data.engine.DatasetValidator
import com.example.data.engine.OomRisk
import com.example.data.model.ModelScale
import com.example.data.model.QuantFormat
import com.example.data.model.TargetChipset
import com.example.data.model.TrainingPathId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
  fun `backend selector evaluates sweet spot for 1B model on Galaxy S25`() {
    val rec = BackendSelectorEngine.evaluate(
      modelScale = ModelScale.SCALE_1B,
      quantFormat = QuantFormat.TQ2_0,
      chipset = TargetChipset.SAMSUNG_S25_ADRENO830
    )
    assertEquals(TrainingPathId.PATH_A, rec.recommendedPath)
    assertEquals("1h 18m", rec.timePerEpochDisplay)
    assertTrue(rec.isSweetSpot)
    assertEquals(OomRisk.SAFE, rec.oomRisk)
  }

  @Test
  fun `backend selector detects OOM for 7B on Galaxy S25`() {
    val rec = BackendSelectorEngine.evaluate(
      modelScale = ModelScale.SCALE_7B,
      quantFormat = QuantFormat.TQ2_0,
      chipset = TargetChipset.SAMSUNG_S25_ADRENO830
    )
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
