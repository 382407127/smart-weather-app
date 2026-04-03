package com.example.smartweather.data.model

import com.google.gson.annotations.SerializedName

/**
 * 和风天气实时天气响应
 */
data class QWeatherNowResponse(
    val code: String,
    val now: WeatherNow?,
    val refer: Refer?
)

/**
 * 实时天气数据
 */
data class WeatherNow(
    @SerializedName("obsTime")
    val obsTime: String,          // 观测时间
    val temp: String,             // 温度
    val feelsLike: String,        // 体感温度
    val icon: String,             // 天气图标代码
    val text: String,             // 天气现象文字
    val wind360: String,          // 风向360角度
    val windDir: String,          // 风向
    val windScale: String,        // 风力等级
    val windSpeed: String,        // 风速 km/h
    val humidity: String,         // 湿度 %
    val precip: String,           // 降水量 mm
    val pressure: String,         // 气压 hPa
    val vis: String,              // 能见度 km
    val cloud: String?,           // 云量
    val dew: String?              // 露点温度
)

/**
 * 和风天气72小时预报响应
 */
data class QWeather72hResponse(
    val code: String,
    val hourly: List<HourlyWeather>?,
    val refer: Refer?
)

/**
 * 小时级天气数据
 */
data class HourlyWeather(
    @SerializedName("fxTime")
    val fxTime: String,           // 预报时间
    val temp: String,             // 温度
    val icon: String,             // 天气图标
    val text: String,             // 天气现象
    @SerializedName("wind360")
    val wind360: String,          // 风向角度
    @SerializedName("windDir")
    val windDir: String,          // 风向
    @SerializedName("windScale")
    val windScale: String,        // 风力等级
    @SerializedName("windSpeed")
    val windSpeed: String,        // 风速
    val humidity: String,         // 湿度
    val pop: String,              // 降水概率
    val precip: String,           // 降水量
    val pressure: String,         // 气压
    val cloud: String?,           // 云量
    val dew: String?              // 露点
)

/**
 * 引用来源
 */
data class Refer(
    val sources: List<String>?,
    val license: List<String>?
)
