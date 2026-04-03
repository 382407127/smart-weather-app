package com.example.smartweather.data.api

import com.example.smartweather.data.model.QWeatherNowResponse
import com.example.smartweather.data.model.QWeather72hResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 和风天气API接口
 * 文档：https://dev.qweather.com/docs/api/
 */
interface QWeatherApiService {
    
    /**
     * 获取实时天气
     * @param location 经纬度，格式：经度,纬度
     * @param key API密钥
     */
    @GET("v7/weather/now")
    suspend fun getWeatherNow(
        @Query("location") location: String,
        @Query("key") key: String
    ): QWeatherNowResponse
    
    /**
     * 获取72小时天气预报
     * @param location 经纬度，格式：经度,纬度
     * @param key API密钥
     */
    @GET("v7/weather/72h")
    suspend fun getWeather72h(
        @Query("location") location: String,
        @Query("key") key: String
    ): QWeather72hResponse
}
