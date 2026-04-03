package com.example.smartweather

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用入口类
 * 使用Hilt进行依赖注入
 */
@HiltAndroidApp
class SmartWeatherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // 应用初始化
    }
}
