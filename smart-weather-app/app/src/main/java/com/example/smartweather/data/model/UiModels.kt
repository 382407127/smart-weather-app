package com.example.smartweather.data.model

/**
 * UI展示用的天气数据模型
 */
data class WeatherUiModel(
    // 城市信息
    val cityName: String = "",
    val longitude: Double = 0.0,
    val latitude: Double = 0.0,
    val lastUpdate: String = "",
    
    // 实时天气
    val currentTemp: Int = 0,
    val feelsLike: Int = 0,
    val weatherText: String = "",
    val weatherIcon: String = "100",
    
    // 今日概况
    val tempMin: Int = 0,
    val tempMax: Int = 0,
    val humidity: Int = 0,
    val windSpeed: String = "",
    val windDir: String = "",
    val pressure: Int = 0,
    val precip: Double = 0.0,
    
    // 小时级数据
    val hourlyData: List<HourlyUiModel> = emptyList(),
    
    // AI建议
    val aiAdvice: List<AdviceItem> = emptyList(),
    
    // 特别提醒
    val specialAlert: String? = null,
    
    // 加载状态
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 小时级天气UI模型
 */
data class HourlyUiModel(
    val hour: Int,                // 小时 (0-23)
    val temp: Int,                // 温度
    val precip: Double,           // 降水量 mm
    val pop: Int,                 // 降水概率 %
    val icon: String,             // 天气图标
    val text: String,             // 天气现象
    val windSpeed: String,        // 风速
    val windDir: String,          // 风向
    val humidity: Int             // 湿度
)

/**
 * AI建议项
 */
data class AdviceItem(
    val title: String,
    val content: String,
    val icon: String = "💡"
)

/**
 * 日期选择选项
 */
enum class DateOption(val label: String, val dayOffset: Int) {
    TODAY("今天", 0),
    TOMORROW("明天", 1),
    AFTER_TOMORROW("后天", 2),
    THREE_DAYS_LATER("+3天", 3)
}

/**
 * 天气主题类型
 */
enum class WeatherTheme {
    SUNNY,      // 晴天
    CLOUDY,     // 多云
    OVERCAST,   // 阴天
    RAINY,      // 雨天
    SNOWY       // 雪天
}

/**
 * 历史城市记录
 */
data class CityHistory(
    val name: String,
    val longitude: Double,
    val latitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)
