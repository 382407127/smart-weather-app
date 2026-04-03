package com.example.smartweather.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartweather.data.model.*
import com.example.smartweather.ui.components.*
import com.example.smartweather.ui.theme.*
import com.example.smartweather.ui.viewmodel.WeatherViewModel
import com.example.smartweather.util.WeatherIconMapper

/**
 * 主天气界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val settingsState by viewModel.settingsState.collectAsState()
    val showSettings by viewModel.showSettings.collectAsState()
    val selectedDateOption by viewModel.selectedDateOption.collectAsState()
    val cityHistory by viewModel.cityHistory.collectAsState(initial = emptyList())
    
    val context = LocalContext.current
    
    // 位置权限请求
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val hasLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasLocation) {
            viewModel.getCurrentLocation()
        }
    }
    
    // 请求位置权限
    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        if (!hasPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    
    // 获取动态主题颜色
    val themeColors = getThemeColors(uiState.weatherTheme)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(themeColors.gradientStart, themeColors.gradientEnd),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 2000f)
                )
            )
    ) {
        // 主内容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // 顶部栏
            WeatherTopBar(
                cityName = uiState.cityName,
                lastUpdate = uiState.lastUpdate,
                onRefresh = { viewModel.refreshWeather() },
                onSettingsClick = { viewModel.toggleSettings(true) },
                onLocationClick = {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    
                    if (hasPermission) {
                        viewModel.getCurrentLocation()
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                isLoading = uiState.isLoading
            )
            
            // 历史城市
            if (cityHistory.isNotEmpty()) {
                CityHistoryRow(
                    cities = cityHistory,
                    onCityClick = { viewModel.selectCityFromHistory(it) }
                )
            }
            
            // 搜索框
            SearchBar(
                onSearch = { viewModel.searchCity(it) }
            )
            
            // 日期选择
            DateSelector(
                selectedOption = selectedDateOption,
                onOptionSelected = { viewModel.selectDateOption(it) }
            )
            
            // 内容区域
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // 加载状态
                if (uiState.isLoading && uiState.cityName.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else if (uiState.error != null && uiState.cityName.isEmpty()) {
                    // 错误状态
                    ErrorCard(
                        message = uiState.error ?: "未知错误",
                        onRetry = { viewModel.refreshWeather() }
                    )
                } else if (uiState.cityName.isNotEmpty()) {
                    // 天气内容
                    // 特别提醒
                    uiState.specialAlert?.let { alert ->
                        SpecialAlertBanner(alert = alert)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // 当前天气卡片
                    CurrentWeatherCard(
                        currentTemp = uiState.currentTemp,
                        feelsLike = uiState.feelsLike,
                        weatherText = uiState.weatherText,
                        weatherIcon = uiState.weatherIcon,
                        tempMin = uiState.tempMin,
                        tempMax = uiState.tempMax,
                        humidity = uiState.humidity,
                        windSpeed = uiState.windSpeed,
                        windDir = uiState.windDir,
                        pressure = uiState.pressure,
                        precip = uiState.precip,
                        longitude = uiState.longitude,
                        latitude = uiState.latitude
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 小时级图表
                    if (uiState.hourlyData.isNotEmpty()) {
                        HourlyChartsSection(
                            hourlyData = uiState.hourlyData,
                            themeColors = themeColors
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // AI建议
                    if (uiState.aiAdvice.isNotEmpty()) {
                        AiAdviceSection(
                            adviceList = uiState.aiAdvice
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
        
        // 设置页面
        if (showSettings) {
            SettingsDialog(
                settingsState = settingsState,
                onDismiss = { viewModel.toggleSettings(false) },
                onSave = { qweather, amap, deepseek ->
                    viewModel.saveSettings(qweather, amap, deepseek)
                }
            )
        }
    }
}

/**
 * 获取主题颜色
 */
@Composable
fun getThemeColors(theme: WeatherTheme): ThemeColors {
    return when (theme) {
        WeatherTheme.SUNNY -> ThemeColors(
            gradientStart = SunnyGradientStart,
            gradientEnd = SunnyGradientEnd,
            accent = Color(0xFFFFB74D),
            lineColor = Color(0xFFFF9800)
        )
        WeatherTheme.CLOUDY -> ThemeColors(
            gradientStart = CloudyGradientStart,
            gradientEnd = CloudyGradientEnd,
            accent = Color(0xFF90A4AE),
            lineColor = Color(0xFF607D8B)
        )
        WeatherTheme.OVERCAST -> ThemeColors(
            gradientStart = OvercastGradientStart,
            gradientEnd = OvercastGradientEnd,
            accent = Color(0xFF78909C),
            lineColor = Color(0xFF546E7A)
        )
        WeatherTheme.RAINY -> ThemeColors(
            gradientStart = RainyGradientStart,
            gradientEnd = RainyGradientEnd,
            accent = Color(0xFF64B5F6),
            lineColor = Color(0xFF2196F3)
        )
        WeatherTheme.SNOWY -> ThemeColors(
            gradientStart = SnowyGradientStart,
            gradientEnd = SnowyGradientEnd,
            accent = Color(0xFFE1F5FE),
            lineColor = Color(0xFF81D4FA)
        )
    }
}

/**
 * 主题颜色配置
 */
data class ThemeColors(
    val gradientStart: Color,
    val gradientEnd: Color,
    val accent: Color,
    val lineColor: Color
)

/**
 * 顶部栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherTopBar(
    cityName: String,
    lastUpdate: String,
    onRefresh: () -> Unit,
    onSettingsClick: () -> Unit,
    onLocationClick: () -> Unit,
    isLoading: Boolean
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = if (cityName.isNotEmpty()) cityName else "智能穿衣助手",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (lastUpdate.isNotEmpty()) {
                    Text(
                        text = "更新于 $lastUpdate",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        },
        actions = {
            // 定位按钮
            IconButton(onClick = onLocationClick) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "定位",
                    tint = Color.White
                )
            }
            // 刷新按钮
            IconButton(onClick = onRefresh) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新",
                        tint = Color.White
                    )
                }
            }
            // 设置按钮
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "设置",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

/**
 * 历史城市行
 */
@Composable
fun CityHistoryRow(
    cities: List<CityHistory>,
    onCityClick: (CityHistory) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(cities) { city ->
            FilterChip(
                selected = false,
                onClick = { onCityClick(city) },
                label = { Text(city.name) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = CardBackground,
                    labelColor = Color.White
                )
            )
        }
    }
}

/**
 * 搜索框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    onSearch: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    
    OutlinedTextField(
        value = searchText,
        onValueChange = { searchText = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("搜索城市...", color = TextHint) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "搜索",
                tint = Color.White
            )
        },
        trailingIcon = {
            if (searchText.isNotEmpty()) {
                IconButton(onClick = { 
                    onSearch(searchText)
                    searchText = ""
                }) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "确认",
                        tint = Color.White
                    )
                }
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = TextSecondary,
            cursorColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * 日期选择器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelector(
    selectedOption: DateOption,
    onOptionSelected: (DateOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = selectedOption.label,
            onValueChange = {},
            readOnly = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "日期",
                    tint = Color.White
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier.menuAnchor(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = TextSecondary,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DateOption.values().forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * 特别提醒横幅
 */
@Composable
fun SpecialAlertBanner(alert: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x33FF5722)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "警告",
                tint = Color(0xFFFFAB91),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = alert,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * 错误卡片
 */
@Composable
fun ErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x33FF5252)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "错误",
                tint = Color(0xFFFF8A80),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                )
            ) {
                Text("重试", color = Color.White)
            }
        }
    }
}
