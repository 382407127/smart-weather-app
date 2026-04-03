package com.example.smartweather.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartweather.ui.theme.CardBackground
import com.example.smartweather.ui.theme.TextPrimary
import com.example.smartweather.ui.theme.TextSecondary
import com.example.smartweather.util.WeatherIconMapper

/**
 * 当前天气卡片
 */
@Composable
fun CurrentWeatherCard(
    currentTemp: Int,
    feelsLike: Int,
    weatherText: String,
    weatherIcon: String,
    tempMin: Int,
    tempMax: Int,
    humidity: Int,
    windSpeed: String,
    windDir: String,
    pressure: Int,
    precip: Double,
    longitude: Double,
    latitude: Double
) {
    // 毛玻璃效果卡片
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // 主要天气信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 天气图标
                Text(
                    text = WeatherIconMapper.getWeatherEmoji(weatherIcon),
                    fontSize = 64.sp
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    // 当前温度
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = currentTemp.toString(),
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Light,
                            color = TextPrimary
                        )
                        Text(
                            text = "°C",
                            fontSize = 24.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    
                    // 天气描述
                    Text(
                        text = weatherText,
                        fontSize = 20.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 温度范围和体感
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherInfoItem(
                    icon = Icons.Default.Thermostat,
                    label = "体感",
                    value = "${feelsLike}°C"
                )
                WeatherInfoItem(
                    icon = Icons.Default.ArrowUpward,
                    label = "最高",
                    value = "${tempMax}°C"
                )
                WeatherInfoItem(
                    icon = Icons.Default.ArrowDownward,
                    label = "最低",
                    value = "${tempMin}°C"
                )
            }
            
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                color = Color.White.copy(alpha = 0.2f)
            )
            
            // 详细信息网格
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherDetailItem(
                    icon = Icons.Default.WaterDrop,
                    label = "湿度",
                    value = "$humidity%"
                )
                WeatherDetailItem(
                    icon = Icons.Default.Air,
                    label = "风速",
                    value = "${windSpeed}km/h"
                )
                WeatherDetailItem(
                    icon = Icons.Default.Explore,
                    label = "风向",
                    value = windDir
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherDetailItem(
                    icon = Icons.Default.Speed,
                    label = "气压",
                    value = "${pressure}hPa"
                )
                WeatherDetailItem(
                    icon = Icons.Default.Grain,
                    label = "降水",
                    value = "${precip}mm"
                )
                WeatherDetailItem(
                    icon = Icons.Default.LocationOn,
                    label = "经纬度",
                    value = "${String.format("%.2f", longitude)}, ${String.format("%.2f", latitude)}"
                )
            }
        }
    }
}

/**
 * 天气信息项
 */
@Composable
fun WeatherInfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextSecondary
            )
            Text(
                text = value,
                fontSize = 16.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 天气详细信息项
 */
@Composable
fun WeatherDetailItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 降水情况卡片
 */
@Composable
fun PrecipitationCard(
    precip: Double,
    hasPrecip: Boolean
) {
    val borderColor = if (hasPrecip) {
        Color(0xFF42A5F5) // 蓝色边框表示有雨
    } else {
        Color.Transparent
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (hasPrecip) Icons.Default.Umbrella else Icons.Default.WbSunny,
                contentDescription = "降水",
                tint = if (hasPrecip) Color(0xFF64B5F6) else Color(0xFFFFB74D),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (hasPrecip) "当前有降水" else "暂无降水",
                    fontSize = 16.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "降水量: ${precip}mm",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
