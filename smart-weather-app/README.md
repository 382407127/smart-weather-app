# 智能穿衣助手 Android App

一个功能完整的Android原生天气应用，使用Kotlin + Jetpack Compose构建。

## 功能特性

### 🌤️ 核心功能
- **城市搜索与管理**：支持高德地图API地理编码搜索城市
- **GPS定位**：自动获取当前位置并显示天气
- **历史城市**：保存最近查询的10个城市，快速切换
- **日期选择**：查看今天、明天、后天、+3天的天气预报

### 📊 天气展示
- **实时天气**：当前温度、体感温度、天气现象
- **详细数据**：湿度、风速、风向、气压、降水量
- **小时级图表**：
  - 温度曲线图（带智能标签避让）
  - 降水趋势图
  - 降水概率图

### 🤖 AI穿衣建议
- **DeepSeek API**：智能生成穿衣建议
- **本地规则引擎**：API不可用时自动降级
- **三条建议**：今日天气解读、穿衣推荐、出行指南

### 🎨 动态主题
- 根据天气自动切换背景渐变色
- 5种主题：晴天、多云、阴天、雨天、雪天

## 技术栈

- **语言**：Kotlin
- **UI框架**：Jetpack Compose
- **架构**：MVVM (ViewModel + StateFlow)
- **网络**：Retrofit + OkHttp + Gson
- **异步**：Coroutines + Flow
- **依赖注入**：Hilt
- **本地存储**：DataStore (Preferences)
- **图表**：自定义Canvas绘制

## API配置

### 必需API密钥
1. **和风天气 API Key**
   - 获取地址：https://dev.qweather.com
   - 用于获取天气数据

2. **高德地图 API Key**
   - 获取地址：https://lbs.amap.com
   - 用于城市搜索和逆地理编码

### 可选API密钥
3. **DeepSeek API Key**
   - 获取地址：https://platform.deepseek.com
   - 用于AI穿衣建议（未配置时使用本地规则）

## 项目结构

```
app/src/main/java/com/example/smartweather/
├── SmartWeatherApp.kt          # Application类
├── MainActivity.kt             # 主Activity
├── data/
│   ├── api/                    # API接口定义
│   │   ├── QWeatherApiService.kt
│   │   ├── AMapApiService.kt
│   │   └── DeepSeekApiService.kt
│   ├── model/                  # 数据模型
│   │   ├── QWeatherModels.kt
│   │   ├── AMapModels.kt
│   │   ├── DeepSeekModels.kt
│   │   └── UiModels.kt
│   ├── preferences/            # 偏好设置
│   │   └── UserPreferences.kt
│   └── repository/             # 数据仓库
│       └── WeatherRepository.kt
├── di/                         # 依赖注入
│   └── AppModule.kt
├── ui/
│   ├── Navigation.kt           # 导航
│   ├── WeatherScreen.kt        # 主界面
│   ├── theme/                  # 主题
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   ├── components/             # UI组件
│   │   ├── WeatherCards.kt
│   │   ├── Charts.kt
│   │   └── AdviceAndSettings.kt
│   └── viewmodel/              # ViewModel
│       └── WeatherViewModel.kt
└── util/                       # 工具类
    ├── WeatherIconMapper.kt
    └── LocalAdviceGenerator.kt
```

## 构建运行

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd smart-weather-app
   ```

2. **Android Studio导入**
   - 打开Android Studio
   - 选择 "Open an Existing Project"
   - 选择项目根目录

3. **配置API密钥**
   - 运行应用后，点击右上角设置图标
   - 输入和风天气和高德地图API密钥
   - （可选）输入DeepSeek API密钥

4. **运行应用**
   - 连接Android设备或启动模拟器
   - 点击运行按钮

## 最低要求

- **最低SDK**：API 24 (Android 7.0)
- **目标SDK**：API 34 (Android 14)
- **JDK**：17

## 权限

- `INTERNET` - 网络访问
- `ACCESS_NETWORK_STATE` - 网络状态
- `ACCESS_FINE_LOCATION` - 精确定位
- `ACCESS_COARSE_LOCATION` - 粗略定位

## 特色功能

### 智能标签避让算法
图表中的温度标签会自动检测重叠，并尝试8个不同方向的偏移，确保标签清晰可读。

### 动态主题切换
根据当前天气现象自动切换应用主题：
- ☀️ 晴天：蓝色渐变
- ⛅ 多云：灰蓝渐变
- ☁️ 阴天：深灰渐变
- 🌧️ 雨天：深蓝渐变
- ❄️ 雪天：浅蓝渐变

### 本地规则引擎
当DeepSeek API不可用时，基于以下因素生成穿衣建议：
- 温度和体感温度
- 日温差
- 降水情况
- 风力等级
- 特殊天气（雪、雾、霾等）

## 许可证

MIT License
