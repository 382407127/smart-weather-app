package com.example.smartweather.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.smartweather.data.model.CityHistory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "weather_preferences")

/**
 * 用户偏好设置存储
 * 使用DataStore存储API密钥和历史城市
 */
@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    
    companion object {
        // API密钥
        private val QWEATHER_KEY = stringPreferencesKey("qweather_key")
        private val AMAP_KEY = stringPreferencesKey("amap_key")
        private val DEEPSEEK_KEY = stringPreferencesKey("deepseek_key")
        
        // 历史城市列表（JSON格式）
        private val CITY_HISTORY = stringPreferencesKey("city_history")
        
        // 默认城市
        private val DEFAULT_CITY = stringPreferencesKey("default_city")
        private val DEFAULT_LON = stringPreferencesKey("default_lon")
        private val DEFAULT_LAT = stringPreferencesKey("default_lat")
    }
    
    // 获取和风天气API Key
    val qweatherKey: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[QWEATHER_KEY]
    }
    
    // 获取高德地图API Key
    val amapKey: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[AMAP_KEY]
    }
    
    // 获取DeepSeek API Key
    val deepseekKey: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[DEEPSEEK_KEY]
    }
    
    // 获取历史城市列表
    val cityHistory: Flow<List<CityHistory>> = context.dataStore.data.map { preferences ->
        val json = preferences[CITY_HISTORY] ?: "[]"
        try {
            gson.fromJson(json, object : TypeToken<List<CityHistory>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // 获取默认城市
    val defaultCity: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_CITY]
    }
    
    // 保存和风天气API Key
    suspend fun saveQWeatherKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[QWEATHER_KEY] = key
        }
    }
    
    // 保存高德地图API Key
    suspend fun saveAmapKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[AMAP_KEY] = key
        }
    }
    
    // 保存DeepSeek API Key
    suspend fun saveDeepSeekKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[DEEPSEEK_KEY] = key
        }
    }
    
    // 清空所有API密钥
    suspend fun clearAllKeys() {
        context.dataStore.edit { preferences ->
            preferences.remove(QWEATHER_KEY)
            preferences.remove(AMAP_KEY)
            preferences.remove(DEEPSEEK_KEY)
        }
    }
    
    // 添加城市到历史记录
    suspend fun addCityToHistory(city: CityHistory) {
        context.dataStore.edit { preferences ->
            val currentList = try {
                val json = preferences[CITY_HISTORY] ?: "[]"
                gson.fromJson<MutableList<CityHistory>>(
                    json, 
                    object : TypeToken<MutableList<CityHistory>>() {}.type
                )
            } catch (e: Exception) {
                mutableListOf()
            }
            
            // 移除已存在的相同城市
            currentList.removeAll { it.name == city.name }
            
            // 添加到列表开头
            currentList.add(0, city)
            
            // 最多保留10条
            val trimmedList = currentList.take(10)
            
            preferences[CITY_HISTORY] = gson.toJson(trimmedList)
        }
    }
    
    // 清空历史城市
    suspend fun clearCityHistory() {
        context.dataStore.edit { preferences ->
            preferences[CITY_HISTORY] = "[]"
        }
    }
    
    // 设置默认城市
    suspend fun setDefaultCity(name: String, lon: Double, lat: Double) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_CITY] = name
            preferences[DEFAULT_LON] = lon.toString()
            preferences[DEFAULT_LAT] = lat.toString()
        }
    }
    
    // 获取默认城市经纬度
    suspend fun getDefaultLocation(): Pair<Double, Double>? {
        val preferences = context.dataStore.data.map { it }.first()
        val lon = preferences[DEFAULT_LON]?.toDoubleOrNull()
        val lat = preferences[DEFAULT_LAT]?.toDoubleOrNull()
        return if (lon != null && lat != null) Pair(lon, lat) else null
    }
}
