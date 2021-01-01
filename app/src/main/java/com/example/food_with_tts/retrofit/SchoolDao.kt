package com.example.food_with_tts.retrofit

import com.example.food_with_tts.data.SchoolBase
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SchoolDao {
    @GET("schoolInfo")
    fun getSchoolInfo(
        @Query("KEY", encoded = true) KEY : String,
        @Query("Type", encoded = true) Type : String,
        @Query("pIndex", encoded = true) pIndex : String,
        @Query("pSize", encoded = true) pSize: String,
        @Query("SCHUL_NM", encoded = true) SCHUL_NM: String
    ) : Call<SchoolBase>
}