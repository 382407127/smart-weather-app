package com.example.smartweather.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.smartweather.data.model.HourlyUiModel
import com.example.smartweather.ui.theme.CardBackground
import com.example.smartweather.ui.theme.TextPrimary
import com.example.smartweather.ui.theme.TextSecondary
import com.example.smartweather.ui.theme.ThemeColors
import kotlin.math.*

/**
 * 小时级图表区域
 */
@Composable
fun HourlyChartsSection(
    hourlyData: List<HourlyUiModel>,
    themeColors: ThemeColors
) {
    Column {
        // 温度曲线图
        TemperatureChartCard(
            hourlyData = hourlyData,
            themeColors = themeColors
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 降水趋势图（仅当有降水时显示）
        val hasPrecip = hourlyData.any { it.precip > 0 }
        if (hasPrecip) {
            PrecipitationChartCard(
                hourlyData = hourlyData,
                themeColors = themeColors
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // 降水概率图（仅当有概率>0时显示）
        val hasPop = hourlyData.any { it.pop > 0 }
        if (hasPop) {
            PrecipitationProbabilityChartCard(
                hourlyData = hourlyData,
                themeColors = themeColors
            )
        }
    }
}

/**
 * 温度曲线图卡片
 */
@Composable
fun TemperatureChartCard(
    hourlyData: List<HourlyUiModel>,
    themeColors: ThemeColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🌡️ 温度趋势",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            TemperatureChart(
                data = hourlyData,
                lineColor = themeColors.lineColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

/**
 * 温度曲线图（带智能标签避让）
 */
@Composable
fun TemperatureChart(
    data: List<HourlyUiModel>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    
    // 计算温度范围
    val temps = data.map { it.temp }
    val minTemp = temps.minOrNull() ?: 0
    val maxTemp = temps.maxOrNull() ?: 0
    val tempRange = maxOf(maxTemp - minTemp, 1)
    
    // 标签配置
    val labelFontSize = 10.sp
    val labelPadding = 4.dp
    
    Canvas(
        modifier = modifier
    ) {
        val chartWidth = size.width
        val chartHeight = size.height
        val padding = 40.dp.toPx()
        
        // 绘制区域
        val drawWidth = chartWidth - padding * 2
        val drawHeight = chartHeight - padding * 2
        
        // 绘制网格线
        drawGrid(
            drawWidth = drawWidth,
            drawHeight = drawHeight,
            padding = padding,
            minTemp = minTemp,
            maxTemp = maxTemp
        )
        
        // 计算数据点位置
        val points = data.mapIndexed { index, hourly ->
            val x = padding + (index.toFloat() / (data.size - 1)) * drawWidth
            val y = padding + (1 - (hourly.temp - minTemp).toFloat() / tempRange) * drawHeight
            PointF(x, y)
        }
        
        // 绘制曲线
        drawCurve(
            points = points,
            lineColor = lineColor
        )
        
        // 绘制数据点和标签（带智能避让）
        drawDataPointsWithLabels(
            data = data,
            points = points,
            temps = temps,
            textMeasurer = textMeasurer,
            density = density,
            labelFontSize = labelFontSize,
            labelPadding = labelPadding.toPx(),
            lineColor = lineColor
        )
        
        // 绘制X轴标签（小时）
        drawXAxisLabels(
            data = data,
            padding = padding,
            drawWidth = drawWidth,
            chartHeight = chartHeight,
            textMeasurer = textMeasurer,
            density = density
        )
        
        // 绘制Y轴标签（温度）
        drawYAxisLabels(
            padding = padding,
            drawHeight = drawHeight,
            minTemp = minTemp,
            maxTemp = maxTemp,
            textMeasurer = textMeasurer,
            density = density
        )
    }
}

/**
 * 绘制网格线
 */
private fun DrawScope.drawGrid(
    drawWidth: Float,
    drawHeight: Float,
    padding: Float,
    minTemp: Int,
    maxTemp: Int
) {
    val gridColor = Color.White.copy(alpha = 0.1f)
    val gridPaint = Paint().apply {
        color = gridColor
        strokeWidth = 1f
    }
    
    // 水平网格线
    val hLines = 5
    for (i in 0..hLines) {
        val y = padding + (i.toFloat() / hLines) * drawHeight
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(padding + drawWidth, y),
            strokeWidth = 1f
        )
    }
    
    // 垂直网格线
    val vLines = 6
    for (i in 0..vLines) {
        val x = padding + (i.toFloat() / vLines) * drawWidth
        drawLine(
            color = gridColor,
            start = Offset(x, padding),
            end = Offset(x, padding + drawHeight),
            strokeWidth = 1f
        )
    }
}

/**
 * 绘制曲线
 */
private fun DrawScope.drawCurve(
    points: List<PointF>,
    lineColor: Color
) {
    if (points.size < 2) return
    
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        
        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val curr = points[i]
            
            // 贝塞尔曲线平滑
            val midX = (prev.x + curr.x) / 2
            quadraticBezierTo(
                prev.x, prev.y,
                midX, (prev.y + curr.y) / 2
            )
        }
        
        val last = points.last()
        lineTo(last.x, last.y)
    }
    
    // 绘制曲线阴影
    drawPath(
        path = path,
        color = lineColor.copy(alpha = 0.2f),
        style = Stroke(width = 8f)
    )
    
    // 绘制曲线
    drawPath(
        path = path,
        color = lineColor,
        style = Stroke(width = 3f, cap = StrokeCap.Round)
    )
}

/**
 * 绘制数据点和标签（智能避让算法）
 */
private fun DrawScope.drawDataPointsWithLabels(
    data: List<HourlyUiModel>,
    points: List<PointF>,
    temps: List<Int>,
    textMeasurer: TextMeasurer,
    density: Density,
    labelFontSize: TextUnit,
    labelPadding: Float,
    lineColor: Color
) {
    // 存储已绘制标签的位置，用于碰撞检测
    val labelRects = mutableListOf<Rect>()
    
    // 8个方向的偏移
    val directions = listOf(
        Offset(0f, -20f),   // 上
        Offset(0f, 20f),    // 下
        Offset(-20f, 0f),   // 左
        Offset(20f, 0f),    // 右
        Offset(-15f, -15f), // 左上
        Offset(15f, -15f),  // 右上
        Offset(-15f, 15f),  // 左下
        Offset(15f, 15f)    // 右下
    )
    
    points.forEachIndexed { index, point ->
        // 绘制数据点
        drawCircle(
            color = Color.White,
            radius = 5f,
            center = Offset(point.x, point.y)
        )
        drawCircle(
            color = lineColor,
            radius = 3f,
            center = Offset(point.x, point.y)
        )
        
        // 每隔几个点显示标签
        if (index % 3 == 0 || index == points.lastIndex) {
            val temp = temps[index]
            val labelText = "${temp}°"
            
            // 测量标签大小
            val labelLayout = textMeasurer.measure(
                text = labelText,
                style = TextStyle(
                    color = TextPrimary,
                    fontSize = labelFontSize,
                    fontWeight = FontWeight.Medium
                )
            )
            val labelWidth = labelLayout.size.width.toFloat()
            val labelHeight = labelLayout.size.height.toFloat()
            
            // 智能避让：尝试不同方向
            var bestOffset = Offset(0f, -20f)
            var foundPosition = false
            
            for (direction in directions) {
                val testX = point.x + direction.x - labelWidth / 2
                val testY = point.y + direction.y - labelHeight / 2
                
                val testRect = Rect(
                    left = testX - labelPadding,
                    top = testY - labelPadding,
                    right = testX + labelWidth + labelPadding,
                    bottom = testY + labelHeight + labelPadding
                )
                
                // 检查是否与已有标签重叠
                val overlaps = labelRects.any { existingRect ->
                    testRect.overlaps(existingRect)
                }
                
                // 检查是否与数据点重叠
                val overlapsPoint = points.any { p ->
                    val dx = p.x - (testX + labelWidth / 2)
                    val dy = p.y - (testY + labelHeight / 2)
                    sqrt(dx * dx + dy * dy) < 15f
                }
                
                if (!overlaps && !overlapsPoint) {
                    bestOffset = direction
                    foundPosition = true
                    break
                }
            }
            
            // 绘制标签
            val labelX = point.x + bestOffset.x - labelWidth / 2
            val labelY = point.y + bestOffset.y - labelHeight / 2
            
            // 标签背景
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.3f),
                topLeft = Offset(labelX - labelPadding, labelY - labelPadding),
                size = Size(labelWidth + labelPadding * 2, labelHeight + labelPadding * 2),
                cornerRadius = CornerRadius(4f)
            )
            
            // 标签文字
            drawText(
                textMeasurer = textMeasurer,
                text = labelText,
                topLeft = Offset(labelX, labelY),
                style = TextStyle(
                    color = TextPrimary,
                    fontSize = labelFontSize,
                    fontWeight = FontWeight.Medium
                )
            )
            
            // 记录标签位置
            labelRects.add(Rect(
                left = labelX - labelPadding,
                top = labelY - labelPadding,
                right = labelX + labelWidth + labelPadding,
                bottom = labelY + labelHeight + labelPadding
            ))
        }
    }
}

/**
 * 绘制X轴标签
 */
private fun DrawScope.drawXAxisLabels(
    data: List<HourlyUiModel>,
    padding: Float,
    drawWidth: Float,
    chartHeight: Float,
    textMeasurer: TextMeasurer,
    density: Density
) {
    val labelInterval = maxOf(1, data.size / 6)
    
    data.forEachIndexed { index, hourly ->
        if (index % labelInterval == 0 || index == data.lastIndex) {
            val x = padding + (index.toFloat() / (data.size - 1)) * drawWidth
            val label = "${hourly.hour}:00"
            
            drawText(
                textMeasurer = textMeasurer,
                text = label,
                topLeft = Offset(x - 15, chartHeight - 25),
                style = TextStyle(
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            )
        }
    }
}

/**
 * 绘制Y轴标签
 */
private fun DrawScope.drawYAxisLabels(
    padding: Float,
    drawHeight: Float,
    minTemp: Int,
    maxTemp: Int,
    textMeasurer: TextMeasurer,
    density: Density
) {
    val steps = 5
    for (i in 0..steps) {
        val temp = minTemp + ((maxTemp - minTemp) * i / steps)
        val y = padding + drawHeight - (i.toFloat() / steps) * drawHeight
        
        drawText(
            textMeasurer = textMeasurer,
            text = "${temp}°",
            topLeft = Offset(0f, y - 8),
            style = TextStyle(
                color = TextSecondary,
                fontSize = 10.sp
            )
        )
    }
}

/**
 * 降水趋势图卡片
 */
@Composable
fun PrecipitationChartCard(
    hourlyData: List<HourlyUiModel>,
    themeColors: ThemeColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🌧️ 降水趋势",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            PrecipitationChart(
                data = hourlyData,
                barColor = Color(0xFF42A5F5),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        }
    }
}

/**
 * 降水柱状图
 */
@Composable
fun PrecipitationChart(
    data: List<HourlyUiModel>,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    
    val textMeasurer = rememberTextMeasurer()
    
    val maxPrecip = data.maxOfOrNull { it.precip } ?: 0.0
    val safeMaxPrecip = maxOf(maxPrecip, 0.1)
    
    Canvas(modifier = modifier) {
        val chartWidth = size.width
        val chartHeight = size.height
        val padding = 40.dp.toPx()
        
        val drawWidth = chartWidth - padding * 2
        val drawHeight = chartHeight - padding * 2
        val barWidth = (drawWidth / data.size) * 0.6f
        
        data.forEachIndexed { index, hourly ->
            if (hourly.precip > 0) {
                val x = padding + (index.toFloat() / data.size) * drawWidth
                val barHeight = (hourly.precip.toFloat() / safeMaxPrecip.toFloat()) * drawHeight
                
                // 绘制柱状
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, padding + drawHeight - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4f)
                )
            }
        }
        
        // Y轴标签
        for (i in 0..4) {
            val precip = safeMaxPrecip * i / 4
            val y = padding + drawHeight - (i.toFloat() / 4) * drawHeight
            
            drawText(
                textMeasurer = textMeasurer,
                text = String.format("%.1f", precip),
                topLeft = Offset(0f, y - 8),
                style = TextStyle(
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            )
        }
    }
}

/**
 * 降水概率图卡片
 */
@Composable
fun PrecipitationProbabilityChartCard(
    hourlyData: List<HourlyUiModel>,
    themeColors: ThemeColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "☔ 降水概率",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            ProbabilityChart(
                data = hourlyData,
                lineColor = Color(0xFF64B5F6),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        }
    }
}

/**
 * 降水概率曲线图
 */
@Composable
fun ProbabilityChart(
    data: List<HourlyUiModel>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    
    val textMeasurer = rememberTextMeasurer()
    
    Canvas(modifier = modifier) {
        val chartWidth = size.width
        val chartHeight = size.height
        val padding = 40.dp.toPx()
        
        val drawWidth = chartWidth - padding * 2
        val drawHeight = chartHeight - padding * 2
        
        // 计算点位置
        val points = data.mapIndexed { index, hourly ->
            val x = padding + (index.toFloat() / (data.size - 1)) * drawWidth
            val y = padding + (1 - hourly.pop / 100f) * drawHeight
            PointF(x, y)
        }
        
        // 绘制填充区域
        val fillPath = Path().apply {
            moveTo(points[0].x, padding + drawHeight)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, padding + drawHeight)
            close()
        }
        
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.3f),
                    lineColor.copy(alpha = 0.1f)
                )
            )
        )
        
        // 绘制曲线
        val strokePath = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }
        
        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(width = 2f)
        )
        
        // Y轴标签
        for (i in 0..4) {
            val prob = i * 25
            val y = padding + drawHeight - (i.toFloat() / 4) * drawHeight
            
            drawText(
                textMeasurer = textMeasurer,
                text = "${prob}%",
                topLeft = Offset(0f, y - 8),
                style = TextStyle(
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            )
        }
    }
}

/**
 * 点坐标
 */
data class PointF(val x: Float, val y: Float)
