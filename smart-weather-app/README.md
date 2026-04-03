# 智能穿衣助手 - Android App

一个功能完整的Android原生天气应用，使用Kotlin + Jetpack Compose构建。

## 📱 在线编译 APK

### 方法一：使用 GitPod（推荐）

1. 点击下方按钮：

[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/你的用户名/smart-weather-app)

2. 等待环境启动（约2分钟）
3. 在终端执行：`./gradlew assembleDebug`
4. 下载 `app/build/outputs/apk/debug/app-debug.apk`

### 方法二：使用 GitHub Actions

1. Fork 本仓库
2. 进入 Actions 页面
3. 点击 "Android CI" → "Run workflow"
4. 等待完成后下载 Artifacts

## 功能特性

- 🌤️ 实时天气查询
- 📊 小时级趋势图表（智能标签避让）
- 🤖 AI 穿衣建议（DeepSeek API）
- 📍 GPS 定位
- 🎨 动态主题背景

## API 配置

首次运行需要配置：
- 和风天气 API Key: https://dev.qweather.com
- 高德地图 API Key: https://lbs.amap.com
- DeepSeek API Key（可选）: https://platform.deepseek.com

## 技术栈

- Kotlin + Jetpack Compose
- MVVM 架构
- Hilt 依赖注入
- Retrofit + OkHttp
- DataStore
