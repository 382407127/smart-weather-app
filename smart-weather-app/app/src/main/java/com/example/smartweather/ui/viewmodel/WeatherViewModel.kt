package com.example.smartweather.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartweather.data.model.*
import com.example.smartweather.data.preferences.UserPreferences
import com.example.smartweather.data.repository.WeatherRepository
import com.example.smartweather.util.WeatherIconMapper
import com.google.android.gms.location.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * 天气ViewModel
 * 管理UI状态和业务逻辑
 */
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val preferences: UserPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    // 位置客户端
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    // UI状态
    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()
    
    // 设置状态
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()
    
    // 当前选中的日期
    private val _selectedDateOption = MutableStateFlow(DateOption.TODAY)
    val selectedDateOption: StateFlow<DateOption> = _selectedDateOption.asStateFlow()
    
    // 历史城市
    val cityHistory: Flow<List<CityHistory>> = preferences.cityHistory
    
    // 搜索输入
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // 是否显示设置页面
    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()
    
    init {
        // 初始化位置客户端
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        
        // 加载设置
        loadSettings()
        
        // 检查是否已配置API密钥
        viewModelScope.launch {
            val hasKeys = repository.hasRequiredKeys()
            if (hasKeys) {
                // 加载默认城市或定位
                loadDefaultCity()
            } else {
                _showSettings.value = true
            }
        }
    }
    
    /**
     * 加载设置
     */
    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                preferences.qweatherKey,
                preferences.amapKey,
                preferences.deepseekKey
            ) { qweather, amap, deepseek ->
                SettingsState(
                    qweatherKey = qweather ?: "",
                    amapKey = amap ?: "",
                    deepseekKey = deepseek ?: ""
                )
            }.collect { state ->
                _settingsState.value = state
            }
        }
    }
    
    /**
     * 加载默认城市
     */
    private fun loadDefaultCity() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // 尝试获取定位
            getCurrentLocation()
        }
    }
    
    /**
     * 获取当前位置
     */
    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val locationResult = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).await()
                
                if (locationResult != null) {
                    val lon = locationResult.longitude
                    val lat = locationResult.latitude
                    
                    // 获取城市名
                    val cityName = repository.getCityName(lon, lat) ?: "当前位置"
                    
                    // 保存到历史
                    preferences.addCityToHistory(CityHistory(cityName, lon, lat))
                    preferences.setDefaultCity(cityName, lon, lat)
                    
                    // 加载天气
                    loadWeatherData(cityName, lon, lat)
                } else {
                    // 定位失败，使用默认城市
                    loadDefaultBeijing()
                }
            } catch (e: Exception) {
                // 定位失败，使用默认城市
                loadDefaultBeijing()
            }
        }
    }
    
    /**
     * 加载默认城市（北京）
     */
    private fun loadDefaultBeijing() {
        viewModelScope.launch {
            loadWeatherData("北京", 116.41, 39.90)
        }
    }
    
    /**
     * 搜索城市
     */
    fun searchCity(query: String) {
        _searchQuery.value = query
        
        if (query.isBlank()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val location = repository.searchCity(query)
            if (location != null) {
                val (lon, lat) = location
                
                // 获取城市名
                val cityName = repository.getCityName(lon, lat) ?: query
                
                // 保存到历史
                preferences.addCityToHistory(CityHistory(cityName, lon, lat))
                preferences.setDefaultCity(cityName, lon, lat)
                
                // 加载天气
                loadWeatherData(cityName, lon, lat)
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "未找到城市：$query"
                    ) 
                }
            }
        }
    }
    
    /**
     * 从历史记录选择城市
     */
    fun selectCityFromHistory(city: CityHistory) {
        viewModelScope.launch {
            loadWeatherData(city.name, city.longitude, city.latitude)
        }
    }
    
    /**
     * 加载天气数据
     */
    private suspend fun loadWeatherData(cityName: String, lon: Double, lat: Double) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        try {
            // 获取实时天气
            val weatherNow = repository.getWeatherNow(lon, lat)
            
            // 获取72小时预报
            val hourlyData = repository.getWeather72h(lon, lat)
            
            // 转换小时数据
            val hourlyUiModels = repository.convertHourlyToUiModel(hourlyData, _selectedDateOption.value.dayOffset)
            
            // 计算今日温度范围
            val todayHourly = repository.convertHourlyToUiModel(hourlyData, 0)
            val temps = todayHourly.map { it.temp }
            val tempMin = temps.minOrNull() ?: 0
            val tempMax = temps.maxOrNull() ?: 0
            
            // 获取AI建议
            val aiAdvice = repository.getAiAdvice(weatherNow, hourlyData, cityName)
            
            // 获取特别提醒
            val specialAlert = repository.getSpecialAlert(weatherNow, hourlyData)
            
            // 获取天气主题
            val theme = weatherNow?.let { 
                WeatherIconMapper.getWeatherTheme(it.icon) 
            } ?: WeatherTheme.SUNNY
            
            // 更新UI状态
            _uiState.update { state ->
                state.copy(
                    cityName = cityName,
                    longitude = lon,
                    latitude = lat,
                    lastUpdate = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                        .format(Date()),
                    currentTemp = weatherNow?.temp?.toIntOrNull() ?: 0,
                    feelsLike = weatherNow?.feelsLike?.toIntOrNull() ?: 0,
                    weatherText = weatherNow?.text ?: "",
                    weatherIcon = weatherNow?.icon ?: "100",
                    tempMin = tempMin,
                    tempMax = tempMax,
                    humidity = weatherNow?.humidity?.toIntOrNull() ?: 0,
                    windSpeed = weatherNow?.windSpeed ?: "0",
                    windDir = weatherNow?.windDir ?: "",
                    pressure = weatherNow?.pressure?.toIntOrNull() ?: 0,
                    precip = weatherNow?.precip?.toDoubleOrNull() ?: 0.0,
                    hourlyData = hourlyUiModels,
                    aiAdvice = aiAdvice,
                    specialAlert = specialAlert,
                    weatherTheme = theme,
                    isLoading = false,
                    error = null
                )
            }
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(
                    isLoading = false, 
                    error = "加载失败：${e.message}"
                ) 
            }
        }
    }
    
    /**
     * 刷新天气
     */
    fun refreshWeather() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.cityName.isNotEmpty()) {
                loadWeatherData(state.cityName, state.longitude, state.latitude)
            }
        }
    }
    
    /**
     * 选择日期
     */
    fun selectDateOption(option: DateOption) {
        _selectedDateOption.value = option
        
        viewModelScope.launch {
            val state = _uiState.value
            if (state.cityName.isNotEmpty()) {
                // 重新加载天气数据以更新小时数据
                val hourlyData = repository.getWeather72h(state.longitude, state.latitude)
                val hourlyUiModels = repository.convertHourlyToUiModel(hourlyData, option.dayOffset)
                
                // 如果选择的是未来日期，计算平均温度
                if (option != DateOption.TODAY && hourlyUiModels.isNotEmpty()) {
                    val avgTemp = hourlyUiModels.map { it.temp }.average().toInt()
                    val middleWeather = hourlyUiModels.getOrNull(hourlyUiModels.size / 2)
                    
                    _uiState.update { 
                        it.copy(
                            hourlyData = hourlyUiModels,
                            currentTemp = avgTemp,
                            weatherText = middleWeather?.text ?: "",
                            weatherIcon = middleWeather?.icon ?: "100"
                        ) 
                    }
                } else {
                    _uiState.update { it.copy(hourlyData = hourlyUiModels) }
                }
            }
        }
    }
    
    /**
     * 保存设置
     */
    fun saveSettings(qweatherKey: String, amapKey: String, deepseekKey: String) {
        viewModelScope.launch {
            preferences.saveQWeatherKey(qweatherKey)
            preferences.saveAmapKey(amapKey)
            if (deepseekKey.isNotBlank()) {
                preferences.saveDeepSeekKey(deepseekKey)
            }
            
            _settingsState.update { 
                it.copy(
                    qweatherKey = qweatherKey,
                    amapKey = amapKey,
                    deepseekKey = deepseekKey
                ) 
            }
            
            _showSettings.value = false
            
            // 如果之前没有数据，加载默认城市
            if (_uiState.value.cityName.isEmpty()) {
                loadDefaultCity()
            }
        }
    }
    
    /**
     * 显示/隐藏设置页面
     */
    fun toggleSettings(show: Boolean) {
        _showSettings.value = show
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * 天气UI状态
 */
data class WeatherUiState(
    val cityName: String = "",
    val longitude: Double = 0.0,
    val latitude: Double = 0.0,
    val lastUpdate: String = "",
    
    val currentTemp: Int = 0,
    val feelsLike: Int = 0,
    val weatherText: String = "",
    val weatherIcon: String = "100",
    
    val tempMin: Int = 0,
    val tempMax: Int = 0,
    val humidity: Int = 0,
    val windSpeed: String = "0",
    val windDir: String = "",
    val pressure: Int = 0,
    val precip: Double = 0.0,
    
    val hourlyData: List<HourlyUiModel> = emptyList(),
    val aiAdvice: List<AdviceItem> = emptyList(),
    val specialAlert: String? = null,
    val weatherTheme: WeatherTheme = WeatherTheme.SUNNY,
    
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 设置状态
 */
data class SettingsState(
    val qweatherKey: String = "",
    val amapKey: String = "",
    val deepseekKey: String = ""
)
