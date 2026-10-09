package com.example

import com.example.data.huggingface.HuggingFaceRepository
import com.example.data.model.ExplanationRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun `presets contain all 5 levels with increasing resource consumption`() {
    val presets = ExplanationRepository.PRESETS
    assertEquals(5, presets.size)

    for (i in 0 until presets.size - 1) {
      assertTrue(
        "Level ${presets[i+1].level} battery drain should be >= Level ${presets[i].level}",
        presets[i+1].batteryDrainPct >= presets[i].batteryDrainPct
      )
    }
  }

  @Test
  fun `explanations repository returns real world analogies and outcome previews`() {
    val loraRank = ExplanationRepository.getExplanation("LORA_RANK")
    assertNotNull(loraRank)
    assertTrue(loraRank.analogy.contains("sticky notes", ignoreCase = true))
    assertTrue(loraRank.outcomeIfIncreased.isNotBlank())
    assertTrue(loraRank.outcomeIfDecreased.isNotBlank())
  }

  @Test
  fun `hugging face repository returns curated mobile models`() = runBlocking {
    val repo = HuggingFaceRepository()
    val models = repo.getModels("bitnet")
    assertTrue(models.isNotEmpty())
    assertTrue(models.any { it.repoId == "1bitLLM/bitnet_b1_58-1B" })
    assertTrue(models.any { it.isSweetSpot })
  }

  @Test
  fun `hugging face repository returns curated datasets and tracks token budget`() = runBlocking {
    val repo = HuggingFaceRepository()
    val datasets = repo.getDatasets()
    assertTrue(datasets.isNotEmpty())
    val alpaca = datasets.firstOrNull { it.repoId.contains("alpaca", ignoreCase = true) }
    assertNotNull(alpaca)
    assertTrue(alpaca!!.isUnder50kCap)
    assertTrue(alpaca.tokenCount <= 50000)
  }

  @Test
  fun `hugging face token storage updates properly`() {
    val repo = HuggingFaceRepository()
    assertEquals(null, repo.userApiToken.value)
    repo.setUserToken("hf_test123456789")
    assertEquals("hf_test123456789", repo.userApiToken.value)
    repo.setUserToken("  ")
    assertEquals(null, repo.userApiToken.value)
  }
}
