package com.example.data.huggingface

import com.example.data.model.ModelScale
import com.example.data.model.QuantFormat
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HFModelApiItem(
    @Json(name = "id") val id: String,
    @Json(name = "author") val author: String? = null,
    @Json(name = "downloads") val downloads: Int? = 0,
    @Json(name = "likes") val likes: Int? = 0,
    @Json(name = "tags") val tags: List<String>? = emptyList(),
    @Json(name = "pipeline_tag") val pipelineTag: String? = null
)

@JsonClass(generateAdapter = true)
data class HFDatasetApiItem(
    @Json(name = "id") val id: String,
    @Json(name = "author") val author: String? = null,
    @Json(name = "downloads") val downloads: Int? = 0,
    @Json(name = "likes") val likes: Int? = 0,
    @Json(name = "tags") val tags: List<String>? = emptyList(),
    @Json(name = "description") val description: String? = null
)

data class HFModelEntry(
    val repoId: String,
    val author: String,
    val modelName: String,
    val description: String,
    val downloads: Int,
    val likes: Int,
    val tags: List<String>,
    val scale: ModelScale,
    val quantFormat: QuantFormat,
    val format: String = "GGUF",
    val isBitNet: Boolean = true,
    val isSweetSpot: Boolean = false
)

data class HFDatasetEntry(
    val repoId: String,
    val author: String,
    val datasetName: String,
    val description: String,
    val downloads: Int,
    val likes: Int,
    val tags: List<String>,
    val docCount: Int,
    val tokenCount: Int,
    val isUnder50kCap: Boolean,
    val isInstructionTuning: Boolean = true
)
