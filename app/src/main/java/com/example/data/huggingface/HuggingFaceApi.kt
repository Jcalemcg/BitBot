package com.example.data.huggingface

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface HuggingFaceApi {

    @GET("models")
    suspend fun searchModels(
        @Query("search") query: String? = null,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "downloads",
        @Query("direction") direction: String? = "-1",
        @Query("limit") limit: Int = 20,
        @Header("Authorization") authHeader: String? = null
    ): List<HFModelApiItem>

    @GET("datasets")
    suspend fun searchDatasets(
        @Query("search") query: String? = null,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "downloads",
        @Query("direction") direction: String? = "-1",
        @Query("limit") limit: Int = 20,
        @Header("Authorization") authHeader: String? = null
    ): List<HFDatasetApiItem>
}
