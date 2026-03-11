package com.ergou.app.ui.english

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.rememberCoroutineScope
import com.ergou.app.data.remote.dto.NextEnglishScenario
import com.ergou.app.ui.components.MarkdownText
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private val categories = listOf(
    null to "全部",
    "英语" to "英语",
    "编程" to "编程",
    "职场" to "职场",
    "生活" to "生活",
    "其他" to "其他"
)

private val categoryColors = mapOf(
    "英语" to Color(0xFF1E88E5),
    "编程" to Color(0xFF43A047),
    "职场" to Color(0xFFFB8C00),
    "生活" to Color(0xFF8E24AA),
    "其他" to Color(0xFF78909C)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnglishScreen(
    onBack: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: EnglishViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var pendingDeleteIds by remember { mutableStateOf(setOf<String>()) }
    var confirmDeleteItem by remember { mutableStateOf<NextEnglishScenario?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // 详情页模式
    val selectedScenario = uiState.selectedScenario
    if (selectedScenario != null) {
        ScenarioDetailView(
            scenario = selectedScenario,
            onBack = { viewModel.selectScenario(null) },
            onArchive = {
                viewModel.archiveScenario(selectedScenario.id)
                viewModel.selectScenario(null)
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("学习场景") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setSearchActive(!uiState.isSearchActive) }) {
                        Icon(
                            if (uiState.isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (uiState.isSearchActive) "关闭搜索" else "搜索"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (uiState.isLoggedIn && !uiState.showArchived) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "添加场景")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (!uiState.isLoggedIn) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("请先登录 Next 账号", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onNavigateToSettings) {
                        Text("前往设置")
                    }
                }
            } else {
                // 活跃/已归档 TabRow
                val tabIndex = if (uiState.showArchived) 1 else 0
                TabRow(selectedTabIndex = tabIndex) {
                    Tab(
                        selected = tabIndex == 0,
                        onClick = { viewModel.switchTab(false) },
                        text = {
                            val count = uiState.scenarios.size
                            Text(if (count > 0) "场景 $count" else "场景")
                        }
                    )
                    Tab(
                        selected = tabIndex == 1,
                        onClick = { viewModel.switchTab(true) },
                        text = {
                            val count = uiState.archivedScenarios.size
                            Text(if (count > 0) "已归档 $count" else "已归档")
                        }
                    )
                }

                // Search bar
                AnimatedVisibility(visible = uiState.isSearchActive) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("搜索场景...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "清除", modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    )
                }

                // Category filter
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { (key, label) ->
                        FilterChip(
                            selected = uiState.selectedCategory == key,
                            onClick = { viewModel.filterByCategory(key) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                val baseList = if (uiState.showArchived) uiState.archivedScenarios else uiState.scenarios
                val displayList = viewModel.filteredScenarios(baseList).filter { it.id !in pendingDeleteIds }

                when {
                    uiState.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    displayList.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                if (uiState.showArchived) "没有已归档场景" else "还没有场景",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!uiState.showArchived) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("点击 + 或告诉二狗来创建", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    else -> {
                        val listState = androidx.compose.foundation.lazy.rememberLazyListState()

                        // Dismiss action bar on any scroll
                        LaunchedEffect(listState.isScrollInProgress) {
                            if (listState.isScrollInProgress && confirmDeleteItem != null) {
                                confirmDeleteItem = null
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .then(
                                    if (confirmDeleteItem != null) {
                                        Modifier.clickable(
                                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                            indication = null
                                        ) { confirmDeleteItem = null }
                                    } else Modifier
                                )
                        ) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item { Spacer(modifier = Modifier.height(4.dp)) }
                                itemsIndexed(displayList, key = { _, s -> s.id }) { index, scenario ->
                                    val isSelected = confirmDeleteItem?.id == scenario.id
                                    EnglishItem(
                                        scenario = scenario,
                                        index = index + 1,
                                        isGenerating = scenario.id in uiState.generatingIds,
                                        hasFailed = scenario.id in uiState.generateFailedIds,
                                        isSelected = isSelected,
                                        onClick = {
                                            if (confirmDeleteItem != null) {
                                                confirmDeleteItem = null
                                            } else {
                                                viewModel.selectScenario(scenario)
                                            }
                                        },
                                        onLongClick = { confirmDeleteItem = scenario },
                                        onGenerate = { viewModel.generateContent(scenario.id) },
                                        onArchive = { viewModel.archiveScenario(scenario.id) }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(80.dp)) }
                            }

                            // Bottom action bar on long press
                            if (confirmDeleteItem != null) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { showDeleteConfirmDialog = true },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("删除")
                                        }
                                        TextButton(onClick = { confirmDeleteItem = null }) {
                                            Text("取消")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmDialog && confirmDeleteItem != null) {
        val scenario = confirmDeleteItem!!
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                confirmDeleteItem = null
            },
            title = { Text("确认删除") },
            text = { Text("确定要删除「${scenario.title}」吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = scenario.id
                        showDeleteConfirmDialog = false
                        confirmDeleteItem = null
                        pendingDeleteIds = pendingDeleteIds + id
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "已删除",
                                actionLabel = "撤销",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                pendingDeleteIds = pendingDeleteIds - id
                            } else {
                                pendingDeleteIds = pendingDeleteIds - id
                                viewModel.deleteScenario(id)
                            }
                        }
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirmDialog = false
                    confirmDeleteItem = null
                }) {
                    Text("取消")
                }
            }
        )
    }

    if (showAddDialog) {
        AddScenarioDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, category, desc ->
                viewModel.addScenario(title, category, desc)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun CategoryTag(category: String) {
    val label = category.ifBlank { "英语" }
    val color = categoryColors[category] ?: categoryColors["英语"]!!

    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        modifier = Modifier
            .background(color, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EnglishItem(
    scenario: NextEnglishScenario,
    index: Int,
    isGenerating: Boolean,
    hasFailed: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onGenerate: () -> Unit,
    onArchive: () -> Unit
) {
    val statusLabel = when {
        isGenerating || scenario.status == "generating" -> "生成中"
        scenario.status == "ready" -> "已生成"
        scenario.status == "error" -> "生成失败"
        else -> "草稿"
    }

    val borderModifier = if (isSelected) {
        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
    } else {
        Modifier
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(borderModifier)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    // Sequence number
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$index",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (scenario.icon != null) {
                        Text(scenario.icon, style = MaterialTheme.typography.titleMedium)
                    }
                    Column {
                        Text(scenario.title, style = MaterialTheme.typography.bodyLarge)
                        if (scenario.titleEn != null) {
                            Text(scenario.titleEn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Row {
                    when {
                        isGenerating || scenario.status == "generating" -> {
                            PulsingIndicator()
                        }
                        hasFailed -> {
                            IconButton(onClick = onGenerate, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = "重试", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        scenario.status == "draft" || scenario.status == "error" -> {
                            IconButton(onClick = onGenerate, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "AI生成", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    if (!scenario.archived) {
                        IconButton(onClick = onArchive, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Archive, contentDescription = "归档", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryTag(scenario.category)
                Text(statusLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (scenario.description != null) {
                Text(
                    scenario.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (scenario.content != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    scenario.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PulsingIndicator() {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    CircularProgressIndicator(
        modifier = Modifier.size(20.dp).alpha(alpha),
        strokeWidth = 2.dp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScenarioDetailView(
    scenario: NextEnglishScenario,
    onBack: () -> Unit,
    onArchive: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(scenario.title, style = MaterialTheme.typography.titleMedium)
                        if (scenario.titleEn != null) {
                            Text(scenario.titleEn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (!scenario.archived) {
                        IconButton(onClick = onArchive) {
                            Icon(Icons.Default.Archive, contentDescription = "归档")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 类别 + 状态
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                CategoryTag(scenario.category)
                val statusText = when (scenario.status) {
                    "ready" -> "已生成"
                    "generating" -> "生成中"
                    "error" -> "生成失败"
                    else -> "草稿"
                }
                Text(statusText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (scenario.description != null) {
                Text(
                    scenario.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI 生成内容（Markdown 渲染）
            if (scenario.content != null) {
                MarkdownText(
                    text = scenario.content,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    "暂无内容",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun AddScenarioDialog(onDismiss: () -> Unit, onAdd: (String, String, String?) -> Unit) {
    var title by remember { mutableStateOf(TextFieldValue()) }
    var category by remember { mutableStateOf("英语") }
    var description by remember { mutableStateOf(TextFieldValue()) }

    val cats = listOf(
        "英语" to "英语",
        "编程" to "编程",
        "职场" to "职场",
        "生活" to "生活",
        "其他" to "其他"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加场景") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("场景标题") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("分类", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    cats.forEach { (key, label) ->
                        FilterChip(
                            selected = category == key,
                            onClick = { category = key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(title.text, category, description.text.ifBlank { null }) },
                enabled = title.text.isNotBlank()
            ) { Text("添加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
