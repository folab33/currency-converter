package com.afoo.currencyconverter.data

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Query

interface ConversionDataService {
    @GET("latest?")
    fun getLatestConversionData(
        @Query("access_key") accessKey: String, @Query("symbols") symbols: String
    ): Call<ConversionData>

//    @GET("latest?")
//    fun getHistoricalConversionData(
//        @Query("access_key") accessKey: String, @Query("symbols") symbols: String
//    ): Call<ConversionData>

    companion object {
        fun create(): ConversionDataService {

            val gson = GsonBuilder().registerTypeAdapter(
                object : TypeToken<Map<String, Double>>() {}.type,
                ConversionDataDeserializer()
            ).setDateFormat("yyyy-MM-dd")
                .create()
            val retrofit = Retrofit.Builder()
                .baseUrl("http://data.fixer.io")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            return retrofit.create(ConversionDataService::class.java)
        }
    }
}