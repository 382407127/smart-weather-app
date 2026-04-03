package com.example.smartweather.di

import com.example.smartweather.data.api.AMapApiService
import com.example.smartweather.data.api.DeepSeekApiService
import com.example.smartweather.data.api.QWeatherApiService
import com.example.smartweather.util.LocalAdviceGenerator
import com.example.smartweather.data.repository.WeatherRepository
import com.example.smartweather.data.preferences.UserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    // ==================== 网络层 ====================
    
    /**
     * 提供OkHttpClient
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * 提供和风天气API服务
     */
    @Provides
    @Singleton
    fun provideQWeatherApiService(okHttpClient: OkHttpClient): QWeatherApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.qweather.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QWeatherApiService::class.java)
    }
    
    /**
     * 提供高德地图API服务
     */
    @Provides
    @Singleton
    fun provideAMapApiService(okHttpClient: OkHttpClient): AMapApiService {
        return Retrofit.Builder()
            .baseUrl("https://restapi.amap.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AMapApiService::class.java)
    }
    
    /**
     * 提供DeepSeek API服务
     */
    @Provides
    @Singleton
    fun provideDeepSeekApiService(okHttpClient: OkHttpClient): DeepSeekApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.deepseek.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeepSeekApiService::class.java)
    }
    
    // ==================== 数据层 ====================
    
    /**
     * 提供天气仓库
     */
    @Provides
    @Singleton
    fun provideWeatherRepository(
        qWeatherApi: QWeatherApiService,
        aMapApi: AMapApiService,
        deepSeekApi: DeepSeekApiService,
        preferences: UserPreferences,
        localAdviceGenerator: LocalAdviceGenerator
    ): WeatherRepository {
        return WeatherRepository(
            qWeatherApi = qWeatherApi,
            aMapApi = aMapApi,
            deepSeekApi = deepSeekApi,
            preferences = preferences,
            localAdviceGenerator = localAdviceGenerator
        )
    }
    
    /**
     * 提供本地建议生成器
     */
    @Provides
    @Singleton
    fun provideLocalAdviceGenerator(): LocalAdviceGenerator {
        return LocalAdviceGenerator()
    }
}
