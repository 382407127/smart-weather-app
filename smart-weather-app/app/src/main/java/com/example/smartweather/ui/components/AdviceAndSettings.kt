package com.example.smartweather.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartweather.data.model.AdviceItem
import com.example.smartweather.ui.theme.CardBackground
import com.example.smartweather.ui.theme.TextPrimary
import com.example.smartweather.ui.theme.TextSecondary
import com.example.smartweather.ui.viewmodel.SettingsState

/**
 * AI建议区域
 */
@Composable
fun AiAdviceSection(
    adviceList: List<AdviceItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 标题和徽章
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🤖",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI 穿衣建议",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                // AI智感徽章
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF7C4DFF).copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "AI智感",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = Color(0xFFB388FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 建议列表
            adviceList.forEachIndexed { index, advice ->
                AdviceItemCard(
                    advice = advice,
                    index = index + 1
                )
                
                if (index < adviceList.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

/**
 * 单条建议卡片
 */
@Composable
fun AdviceItemCard(
    advice: AdviceItem,
    index: Int
) {
    val iconMap = mapOf(
        "今日天气解读" to "🌤️",
        "穿衣推荐" to "👕",
        "出行指南" to "🚶"
    )
    
    val colorMap = mapOf(
        "今日天气解读" to Color(0xFF4FC3F7),
        "穿衣推荐" to Color(0xFFFFB74D),
        "出行指南" to Color(0xFF81C784)
    )
    
    val icon = iconMap[advice.title] ?: advice.icon
    val accentColor = colorMap[advice.title] ?: Color(0xFF78909C)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.15f),
                        accentColor.copy(alpha = 0.05f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        // 图标
        Text(
            text = icon,
            fontSize = 28.sp
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // 内容
        Column {
            Text(
                text = advice.title,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = advice.content,
                color = TextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

/**
 * 设置对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    settingsState: SettingsState,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var qweatherKey by remember { mutableStateOf(settingsState.qweatherKey) }
    var amapKey by remember { mutableStateOf(settingsState.amapKey) }
    var deepseekKey by remember { mutableStateOf(settingsState.deepseekKey) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E1E)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // 标题
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "设置",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "API 密钥配置",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 和风天气 API Key
                OutlinedTextField(
                    value = qweatherKey,
                    onValueChange = { qweatherKey = it },
                    label = { Text("和风天气 API Key *") },
                    placeholder = { Text("请输入和风天气API密钥") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4FC3F7),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF4FC3F7),
                        unfocusedLabelColor = Color.Gray
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = null,
                            tint = Color(0xFF4FC3F7)
                        )
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 高德地图 API Key
                OutlinedTextField(
                    value = amapKey,
                    onValueChange = { amapKey = it },
                    label = { Text("高德地图 API Key *") },
                    placeholder = { Text("请输入高德地图API密钥") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF4CAF50),
                        unfocusedLabelColor = Color.Gray
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50)
                        )
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // DeepSeek API Key
                OutlinedTextField(
                    value = deepseekKey,
                    onValueChange = { deepseekKey = it },
                    label = { Text("DeepSeek API Key (可选)") },
                    placeholder = { Text("用于AI穿衣建议") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C4DFF),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF7C4DFF),
                        unfocusedLabelColor = Color.Gray
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Color(0xFF7C4DFF)
                        )
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 提示
                Text(
                    text = "* 为必填项，DeepSeek Key 可选，未配置时使用本地规则",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 取消按钮
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text("取消")
                    }
                    
                    // 保存按钮
                    Button(
                        onClick = {
                            if (qweatherKey.isNotBlank() && amapKey.isNotBlank()) {
                                onSave(qweatherKey, amapKey, deepseekKey)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = qweatherKey.isNotBlank() && amapKey.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4FC3F7)
                        )
                    ) {
                        Text("保存")
                    }
                }
                
                // 获取密钥提示
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "获取API密钥：",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• 和风天气: dev.qweather.com",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
                Text(
                    text = "• 高德地图: lbs.amap.com",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
                Text(
                    text = "• DeepSeek: platform.deepseek.com",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}
