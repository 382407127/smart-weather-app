package com.example.smartweather.util

import com.example.smartweather.data.model.AdviceItem
import com.example.smartweather.data.model.HourlyWeather
import com.example.smartweather.data.model.WeatherNow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 本地穿衣建议生成器
 * 当DeepSeek API不可用时使用
 */
@Singleton
class LocalAdviceGenerator @Inject constructor() {
    
    /**
     * 根据天气数据生成本地建议
     */
    fun generateAdvice(
        weatherNow: WeatherNow?,
        hourlyData: List<HourlyWeather>?
    ): List<AdviceItem> {
        if (weatherNow == null) {
            return listOf(
                AdviceItem("暂无数据", "请刷新获取天气信息", "❓")
            )
        }
        
        val temp = weatherNow.temp.toIntOrNull() ?: 20
        val feelsLike = weatherNow.feelsLike.toIntOrNull() ?: temp
        val humidity = weatherNow.humidity.toIntOrNull() ?: 50
        val windSpeed = weatherNow.windSpeed.toIntOrNull() ?: 0
        val weatherText = weatherNow.text
        
        // 计算温差
        val temps = hourlyData?.mapNotNull { it.temp.toIntOrNull() } ?: emptyList()
        val tempRange = if (temps.isNotEmpty()) temps.max() - temps.min() else 0
        
        // 检查降水
        val hasPrecip = (weatherNow.precip.toDoubleOrNull() ?: 0.0) > 0
        val hasFuturePrecip = hourlyData?.any { 
            (it.precip.toDoubleOrNull() ?: 0.0) > 0 
        } ?: false
        
        // 生成建议
        val adviceList = mutableListOf<AdviceItem>()
        
        // 1. 今日天气解读
        adviceList.add(generateWeatherInterpretation(temp, feelsLike, humidity, windSpeed, weatherText, tempRange))
        
        // 2. 穿衣推荐
        adviceList.add(generateClothingAdvice(temp, feelsLike, windSpeed, hasPrecip || hasFuturePrecip, weatherText))
        
        // 3. 出行指南
        adviceList.add(generateTravelAdvice(temp, weatherText, hasPrecip, hasFuturePrecip, windSpeed))
        
        return adviceList
    }
    
    /**
     * 生成天气解读
     */
    private fun generateWeatherInterpretation(
        temp: Int, feelsLike: Int, humidity: Int, 
        windSpeed: Int, weatherText: String, tempRange: Int
    ): AdviceItem {
        val content = buildString {
            append("今日天气${weatherText}，")
            
            // 温度描述
            when {
                temp >= 35 -> append("气温高达${temp}℃，属于高温天气")
                temp >= 30 -> append("气温较高，达到${temp}℃")
                temp >= 20 -> append("气温适宜，约${temp}℃")
                temp >= 10 -> append("气温偏凉，约${temp}℃")
                temp >= 0 -> append("气温较低，仅${temp}℃")
                else -> append("气温严寒，仅${temp}℃")
            }
            
            // 体感差异
            if (kotlin.math.abs(feelsLike - temp) >= 3) {
                if (feelsLike > temp) {
                    append("，体感温度${feelsLike}℃，感觉更热")
                } else {
                    append("，体感温度${feelsLike}℃，感觉更冷")
                }
            }
            
            // 湿度
            if (humidity >= 80) {
                append("，湿度${humidity}%，空气潮湿")
            } else if (humidity <= 30) {
                append("，湿度${humidity}%，空气干燥")
            }
            
            // 温差
            if (tempRange >= 10) {
                append("。今日温差达${tempRange}℃，早晚注意增减衣物")
            }
            
            append("。")
        }
        
        return AdviceItem("今日天气解读", content, "🌤️")
    }
    
    /**
     * 生成穿衣建议
     */
    private fun generateClothingAdvice(
        temp: Int, feelsLike: Int, windSpeed: Int,
        hasPrecip: Boolean, weatherText: String
    ): AdviceItem {
        val content = buildString {
            // 基础衣物建议
            when {
                temp >= 35 -> {
                    append("建议穿着轻薄透气的短袖、短裤，选择浅色衣物反射阳光。")
                }
                temp >= 28 -> {
                    append("建议穿着短袖、薄长裤或裙子，材质选择棉麻等透气面料。")
                }
                temp >= 22 -> {
                    append("建议穿着长袖衬衫或薄外套，搭配长裤，早晚可加薄外套。")
                }
                temp >= 15 -> {
                    append("建议穿着薄毛衣或卫衣，搭配外套，注意保暖。")
                }
                temp >= 8 -> {
                    append("建议穿着毛衣、厚外套或薄棉服，注意保暖。")
                }
                temp >= 0 -> {
                    append("建议穿着厚毛衣、羽绒服或棉服，戴围巾手套。")
                }
                else -> {
                    append("建议穿着厚羽绒服、保暖内衣，务必戴帽子、围巾、手套。")
                }
            }
            
            // 风力影响
            if (windSpeed >= 30) {
                append("风力较大，建议选择防风外套。")
            }
            
            // 降水影响
            if (hasPrecip) {
                append("有降水，建议穿着防水外套或携带雨具。")
            }
            
            // 特殊天气
            if (weatherText.contains("雪")) {
                append("雪天路滑，建议穿防滑鞋。")
            }
        }
        
        return AdviceItem("穿衣推荐", content, "👕")
    }
    
    /**
     * 生成出行建议
     */
    private fun generateTravelAdvice(
        temp: Int, weatherText: String,
        hasPrecip: Boolean, hasFuturePrecip: Boolean, windSpeed: Int
    ): AdviceItem {
        val content = buildString {
            // 基础出行建议
            when {
                temp >= 35 -> {
                    append("高温天气，尽量避免中午外出，选择早晚出行。注意防晒，多补充水分。")
                }
                temp >= 28 -> {
                    append("天气较热，外出注意防晒，建议携带遮阳伞或帽子。")
                }
                temp >= 15 -> {
                    append("天气舒适，适合户外活动。")
                }
                temp >= 0 -> {
                    append("气温较低，外出注意保暖，可适当进行户外运动。")
                }
                else -> {
                    append("气温严寒，尽量减少外出，外出务必做好保暖措施。")
                }
            }
            
            // 降水影响
            if (hasPrecip) {
                append("当前有降水，外出请带伞，注意路滑。")
            } else if (hasFuturePrecip) {
                append("预报有降水，建议携带雨具。")
            }
            
            // 风力影响
            if (windSpeed >= 50) {
                append("风力很大，尽量避免外出，注意高空坠物。")
            } else if (windSpeed >= 30) {
                append("风力较大，外出注意安全。")
            }
            
            // 特殊天气
            if (weatherText.contains("雪")) {
                append("雪天出行注意安全，驾车减速慢行。")
            }
            
            if (weatherText.contains("雾") || weatherText.contains("霾")) {
                append("能见度较低，外出注意安全，建议佩戴口罩。")
            }
        }
        
        return AdviceItem("出行指南", content, "🚶")
    }
}
