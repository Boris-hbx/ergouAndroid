package com.ergou.app.ui.soul

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ergou.app.data.local.entity.SoulEvolutionLogEntity
import com.ergou.app.data.local.entity.SoulStateEntity
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoulScreen(
    onBack: () -> Unit,
    viewModel: SoulViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("人格状态") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = uiState.selectedTab) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.onTabSelected(0) },
                    text = { Text("参数面板") }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.onTabSelected(1) },
                    text = { Text("进化日志") }
                )
            }

            when (uiState.selectedTab) {
                0 -> ParametersTab(
                    state = uiState.soulState,
                    onResetAll = { viewModel.onShowResetDialog() }
                )
                1 -> EvolutionLogTab(logs = uiState.recentLogs)
            }
        }
    }

    if (uiState.showResetDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissResetDialog() },
            title = { Text("恢复出厂设置") },
            text = { Text("确定要将所有人格参数重置为默认值吗？互动次数和关系阶段不会被重置。") },
            confirmButton = {
                TextButton(onClick = { viewModel.onResetAll() }) {
                    Text("确定重置", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissResetDialog() }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun ParametersTab(
    state: SoulStateEntity,
    onResetAll: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 关系阶段 + 互动次数
        item {
            Spacer(modifier = Modifier.height(8.dp))
            RelationshipHeader(state)
        }

        // 5 个参数卡片
        item {
            ParameterCard(
                name = "文白比例",
                value = state.classicalRatio,
                description = describeClassicalRatio(state.classicalRatio)
            )
        }
        item {
            ParameterCard(
                name = "温度",
                value = state.warmthLevel,
                description = describeWarmth(state.warmthLevel)
            )
        }
        item {
            ParameterCard(
                name = "话痨程度",
                value = state.verbosityLevel,
                description = describeVerbosity(state.verbosityLevel)
            )
        }
        item {
            ParameterCard(
                name = "主动性",
                value = state.proactivityLevel,
                maxValue = 0.8f,
                description = describeProactivity(state.proactivityLevel)
            )
        }
        item {
            ParameterCard(
                name = "信任度",
                value = state.trustLevel,
                description = describeTrust(state.trustLevel)
            )
        }

        // 重置按钮
        item {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onResetAll,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("恢复出厂设置", color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RelationshipHeader(state: SoulStateEntity) {
    val stageLabel = when (state.relationshipStage) {
        "stranger" -> "初识"
        "acquaintance" -> "相识"
        "familiar" -> "熟悉"
        "close" -> "亲近"
        "intimate" -> "至交"
        else -> state.relationshipStage
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "关系阶段",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = stageLabel,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "互动次数",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = "${state.totalInteractions}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun ParameterCard(
    name: String,
    value: Float,
    maxValue: Float = 1.0f,
    description: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${(value * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { value / maxValue },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EvolutionLogTab(logs: List<SoulEvolutionLogEntity>) {
    if (logs.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "暂无进化记录",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "随着对话增多，二狗的人格会自然演变",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        items(items = logs, key = { it.id }) { log ->
            EvolutionLogItem(log)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun EvolutionLogItem(log: SoulEvolutionLogEntity) {
    val paramLabel = when (log.parameterName) {
        "classicalRatio" -> "文白比例"
        "warmthLevel" -> "温度"
        "verbosityLevel" -> "话痨程度"
        "proactivityLevel" -> "主动性"
        "trustLevel" -> "信任度"
        "relationshipStage" -> "关系阶段"
        else -> log.parameterName
    }

    val isIncrease = log.newValue > log.oldValue
    val changeColor = if (isIncrease) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    val changeText = if (log.parameterName == "relationshipStage") {
        val stages = listOf("初识", "相识", "熟悉", "亲近", "至交")
        val oldStage = stages.getOrElse(log.oldValue.toInt()) { "?" }
        val newStage = stages.getOrElse(log.newValue.toInt()) { "?" }
        "$oldStage → $newStage"
    } else {
        val oldPct = (log.oldValue * 100).toInt()
        val newPct = (log.newValue * 100).toInt()
        "${oldPct}% → ${newPct}%"
    }

    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    val timeStr = dateFormat.format(Date(log.createdAt))

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = paramLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = changeText,
                style = MaterialTheme.typography.bodyMedium,
                color = changeColor,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = log.reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = timeStr,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun describeClassicalRatio(value: Float): String {
    val pct = (value * 100).toInt()
    return when {
        value >= 0.9f -> "九成文言，一成白话。古文为母语。"
        value >= 0.75f -> "约${pct}%文言，${100 - pct}%白话。文言为主，偶尔白话。"
        else -> "约${pct}%文言，${100 - pct}%白话。文白混用。"
    }
}

private fun describeWarmth(value: Float): String {
    return when {
        value < 0.2f -> "冷静克制，就事论事。"
        value < 0.5f -> "偶有温度，关键时刻点到为止。"
        value < 0.8f -> "有温度，关心主人状态。"
        else -> "温暖可靠，像一个真正的老友。"
    }
}

private fun describeVerbosity(value: Float): String {
    return when {
        value < 0.3f -> "言简意赅，惜字如金。"
        value < 0.6f -> "适度展开，不啰嗦。"
        else -> "详细说明，主动提供背景信息。"
    }
}

private fun describeProactivity(value: Float): String {
    return when {
        value < 0.2f -> "被动响应，问什么答什么。"
        value < 0.5f -> "适度主动，发现风险会提醒。"
        else -> "主动关怀，注意到相关事项会提醒。"
    }
}

private fun describeTrust(value: Float): String {
    return when {
        value < 0.3f -> "初步建立信任中。"
        value < 0.5f -> "信任逐渐积累。"
        value < 0.8f -> "信任深厚，说话更自然放松。"
        else -> "心意相通，无需多言。"
    }
}
