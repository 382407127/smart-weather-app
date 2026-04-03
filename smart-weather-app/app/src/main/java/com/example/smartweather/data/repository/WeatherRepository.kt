package com.example.smartweather.data.repository

import com.example.smartweather.data.api.AMapApiService
import com.example.smartweather.data.api.DeepSeekApiService
import com.example.smartweather.data.api.QWeatherApiService
import com.example.smartweather.data.model.*
import com.example.smartweather.data.preferences.UserPreferences
import com.example.smartweather.util.LocalAdviceGenerator
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 天气数据仓库
 * 统一管理所有数据源
 */
@Singleton
class WeatherRepository @Inject constructor(
    private val qWeatherApi: QWeatherApiService,
    private val aMapApi: AMapApiService,
    private val deepSeekApi: DeepSeekApiService,
    private val preferences: UserPreferences,
    private val localAdviceGenerator: LocalAdviceGenerator
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val hourFormat = SimpleDateFormat("HH", Locale.getDefault())
    
    /**
     * 搜索城市
     * @param cityName 城市名称
     * @return 经纬度或null
     */
    suspend fun searchCity(cityName: String): Pair<Double, Double>? {
        val amapKey = preferences.amapKey.first() ?: return null
        
        return try {
            val response = aMapApi.geocode(cityName, amapKey)
            if (response.status == "1" && !response.geocodes.isNullOrEmpty()) {
                val location = response.geocodes[0].location
                val parts = location.split(",")
                if (parts.size == 2) {
                    Pair(parts[0].toDouble(), parts[1].toDouble())
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 逆地理编码获取城市名
     * @param lon 经度
     * @param lat 纬度
     * @return 城市名或null
     */
    suspend fun getCityName(lon: Double, lat: Double): String? {
        val amapKey = preferences.amapKey.first() ?: return null
        
        return try {
            val response = aMapApi.regeo("$lon,$lat", amapKey)
            if (response.status == "1" && response.regeocode != null) {
                val regeo = response.regeocode
                val addressComponent = regeo.addressComponent
                
                // 优先返回城市名，直辖市返回省份
                val city = addressComponent.city
                val province = addressComponent.province
                
                when {
                    !city.isNullOrEmpty() -> city
                    !province.isNullOrEmpty() -> province
                    else -> regeo.formattedAddress
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 获取实时天气
     */
    suspend fun getWeatherNow(lon: Double, lat: Double): WeatherNow? {
        val key = preferences.qweatherKey.first() ?: return null
        
        return try {
            val response = qWeatherApi.getWeatherNow("$lon,$lat", key)
            if (response.code == "200") response.now else null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 获取72小时预报
     */
    suspend fun getWeather72h(lon: Double, lat: Double): List<HourlyWeather>? {
        val key = preferences.qweatherKey.first() ?: return null
        
        return try {
            val response = qWeatherApi.getWeather72h("$lon,$lat", key)
            if (response.code == "200") response.hourly else null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 获取AI穿衣建议
     * 优先使用DeepSeek，失败则降级到本地规则
     */
    suspend fun getAiAdvice(
        weatherNow: WeatherNow?,
        hourlyData: List<HourlyWeather>?,
        cityName: String
    ): List<AdviceItem> {
        val deepseekKey = preferences.deepseekKey.first()
        
        // 如果有DeepSeek Key，尝试调用API
        if (!deepseekKey.isNullOrBlank() && weatherNow != null) {
            try {
                val advice = callDeepSeek(weatherNow, hourlyData, cityName, deepseekKey)
                if (advice != null) return advice
            } catch (e: Exception) {
                // 调用失败，降级到本地规则
            }
        }
        
        // 本地规则引擎
        return localAdviceGenerator.generateAdvice(weatherNow, hourlyData)
    }
    
    /**
     * 调用DeepSeek API获取建议
     */
    private suspend fun callDeepSeek(
        weatherNow: WeatherNow,
        hourlyData: List<HourlyWeather>?,
        cityName: String,
        apiKey: String
    ): List<AdviceItem>? {
        val prompt = buildDeepSeekPrompt(weatherNow, hourlyData, cityName)
        
        val request = DeepSeekRequest(
            model = "deepseek-chat",
            messages = listOf(
                Message(
                    role = "system",
                    content = "你是一个专业的穿衣顾问，请根据天气情况给出实用的穿衣建议。请严格按照JSON数组格式返回，每个建议包含title和content两个字段。"
                ),
                Message(
                    role = "user",
                    content = prompt
                )
            )
        )
        
        return try {
            val response = deepSeekApi.chatCompletions("Bearer $apiKey", request)
            val content = response.choices?.firstOrNull()?.message?.content ?: return null
            parseDeepSeekResponse(content)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 构建DeepSeek提示词
     */
    private fun buildDeepSeekPrompt(
        weatherNow: WeatherNow,
        hourlyData: List<HourlyWeather>?,
        cityName: String
    ): String {
        val sb = StringBuilder()
        sb.append("城市：$cityName\n")
        sb.append("当前温度：${weatherNow.temp}℃\n")
        sb.append("体感温度：${weatherNow.feelsLike}℃\n")
        sb.append("天气：${weatherNow.text}\n")
        sb.append("湿度：${weatherNow.humidity}%\n")
        sb.append("风速：${weatherNow.windSpeed}km/h\n")
        sb.append("风向：${weatherNow.windDir}\n")
        
        if (!hourlyData.isNullOrEmpty()) {
            val temps = hourlyData.mapNotNull { it.temp.toIntOrNull() }
            if (temps.isNotEmpty()) {
                sb.append("今日温度范围：${temps.min()}~${temps.max()}℃\n")
            }
        }
        
        sb.append("\n请给出三条建议：1.今日天气解读 2.穿衣推荐 3.出行指南\n")
        sb.append("请以JSON数组格式返回，格式如下：\n")
        sb.append("[{\"title\":\"今日天气解读\",\"content\":\"...\"},{\"title\":\"穿衣推荐\",\"content\":\"...\"},{\"title\":\"出行指南\",\"content\":\"...\"}]")
        
        return sb.toString()
    }
    
    /**
     * 解析DeepSeek响应
     */
    private fun parseDeepSeekResponse(content: String): List<AdviceItem>? {
        return try {
            // 尝试提取JSON数组
            val jsonStart = content.indexOf('[')
            val jsonEnd = content.lastIndexOf(']')
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val json = content.substring(jsonStart, jsonEnd + 1)
                val items = com.google.gson.Gson().fromJson<List<Map<String, String>>>(
                    json,
                    object : com.google.gson.reflect.TypeToken<List<Map<String, String>>>() {}.type
                )
                items.map { map ->
                    AdviceItem(
                        title = map["title"] ?: "",
                        content = map["content"] ?: ""
                    )
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 转换小时级数据为UI模型
     * @param dayOffset 天数偏移（0=今天，1=明天，2=后天，3=+3天）
     */
    fun convertHourlyToUiModel(
        hourlyData: List<HourlyWeather>?,
        dayOffset: Int
    ): List<HourlyUiModel> {
        if (hourlyData.isNullOrEmpty()) return emptyList()
        
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, dayOffset)
        val targetDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        
        return hourlyData
            .filter { it.fxTime.startsWith(targetDate) }
            .map { hourly ->
                val hour = try {
                    val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
                        .parse(hourly.fxTime)
                    if (date != null) {
                        Calendar.getInstance().apply { time = date }.get(Calendar.HOUR_OF_DAY)
                    } else 0
                } catch (e: Exception) { 0 }
                
                HourlyUiModel(
                    hour = hour,
                    temp = hourly.temp.toIntOrNull() ?: 0,
                    precip = hourly.precip.toDoubleOrNull() ?: 0.0,
                    pop = hourly.pop.toIntOrNull() ?: 0,
                    icon = hourly.icon,
                    text = hourly.text,
                    windSpeed = hourly.windSpeed,
                    windDir = hourly.windDir,
                    humidity = hourly.humidity.toIntOrNull() ?: 0
                )
            }
    }
    
    /**
     * 检查API密钥是否已配置
     */
    suspend fun hasRequiredKeys(): Boolean {
        val qweatherKey = preferences.qweatherKey.first()
        val amapKey = preferences.amapKey.first()
        return !qweatherKey.isNullOrBlank() && !amapKey.isNullOrBlank()
    }
    
    /**
     * 获取特别提醒
     */
    fun getSpecialAlert(weatherNow: WeatherNow?, hourlyData: List<HourlyWeather>?): String? {
        if (weatherNow == null) return null
        
        val alerts = mutableListOf<String>()
        
        // 高温预警
        val temp = weatherNow.temp.toIntOrNull() ?: 0
        if (temp >= 35) {
            alerts.add("⚠️ 高温预警：今日气温较高，请注意防暑降温")
        } else if (temp >= 30) {
            alerts.add("🌡️ 温度较高，注意补充水分")
        }
        
        // 低温预警
        if (temp <= 0) {
            alerts.add("❄️ 低温预警：请注意保暖，防止冻伤")
        } else if (temp <= 5) {
            alerts.add("🥶 温度较低，建议多穿衣物")
        }
        
        // 降水预警
        val precip = weatherNow.precip.toDoubleOrNull() ?: 0.0
        if (precip > 0) {
            alerts.add("🌧️ 当前有降水，出门请带伞")
        }
        
        // 检查未来降水
        val hasFutureRain = hourlyData?.any { 
            (it.precip.toDoubleOrNull() ?: 0.0) > 0 || (it.pop.toIntOrNull() ?: 0) > 50
        } ?: false
        if (hasFutureRain && precip <= 0) {
            alerts.add("☔ 未来可能有降水，建议携带雨具")
        }
        
        // 大风预警
        val windSpeed = weatherNow.windSpeed.toIntOrNull() ?: 0
        if (windSpeed >= 50) {
            alerts.add("💨 大风预警：风力较大，注意安全")
        }
        
        return if (alerts.isNotEmpty()) alerts.joinToString("\n") else null
    }
}
