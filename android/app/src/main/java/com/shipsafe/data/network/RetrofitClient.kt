package com.shipsafe.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var baseUrl = "http://10.0.2.2:3000/" // Android Emulator localhost bridge
    var authToken: String? = null
    var currentRole: String = "RELEASE_MANAGER"

    fun setServerUrl(url: String) {
        baseUrl = if (url.endsWith("/")) url else "$url/"
        retrofitInstance = null
    }

    fun getServerUrl(): String = baseUrl

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()

        authToken?.let {
            builder.header("Authorization", "Bearer $it")
        }
        builder.header("x-demo-role", currentRole)
        builder.header("x-device-model", "iQOO 12 Pro (Android 14)")

        chain.proceed(builder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private var retrofitInstance: Retrofit? = null

    val api: ShipSafeApiService
        get() {
            if (retrofitInstance == null) {
                retrofitInstance = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofitInstance!!.create(ShipSafeApiService::class.java)
        }
}
