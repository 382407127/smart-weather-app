package com.example.smartweather.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * 导航图
 */
@Composable
fun SmartWeatherNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "weather"
    ) {
        composable("weather") {
            WeatherScreen()
        }
    }
}
