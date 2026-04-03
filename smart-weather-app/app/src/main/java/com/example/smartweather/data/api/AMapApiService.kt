package com.example.smartweather.data.api

import com.example.smartweather.data.model.AMapGeocodeResponse
import com.example.smartweather.data.model.AMapRegeoResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 高德地图API接口
 * 文档：https://lbs.amap.com/api/webservice/summary
 */
interface AMapApiService {
    
    /**
     * 地理编码：地址转经纬度
     * @param address 城市名称
     * @param key API密钥
     */
    @GET("v3/geocode/geo")
    suspend fun geocode(
        @Query("address") address: String,
        @Query("key") key: String
    ): AMapGeocodeResponse
    
    /**
     * 逆地理编码：经纬度转地址
     * @param location 经纬度，格式：经度,纬度
     * @param key API密钥
     */
    @GET("v3/geocode/regeo")
    suspend fun regeo(
        @Query("location") location: String,
        @Query("key") key: String
    ): AMapRegeoResponse
}
