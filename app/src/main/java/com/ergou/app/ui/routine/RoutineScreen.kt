package com.ergou.app.ui.routine

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ergou.app.data.remote.dto.NextReview
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineScreen(
    onBack: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: RoutineViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var confirmDeleteItem by remember { mutableStateOf<NextReview?>(null) }
    var pendingDeleteIds by remember { mutableStateOf(setOf<String>()) }
    var progressDialogReview by remember { mutableStateOf<NextReview?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Intercept system back when in detail view
    BackHandler(enabled = uiState.selectedReview != null) {
        viewModel.selectReview(null)
    }

    // Detail view
    if (uiState.selectedReview != null) {
        val reviewId = uiState.selectedReview!!.id
        ReviewDetailScreen(
            review = uiState.selectedReview!!,
            progress = uiState.progressMap[reviewId] ?: 0f,
            onBack = { viewModel.selectReview(null) },
            onProgressChange = { viewModel.updateProgress(reviewId, it) },
            onComplete = {
                viewModel.completeReview(reviewId)
                viewModel.selectReview(null)
                scope.launch { snackbarHostState.showSnackbar("已完成") }
            },
            onDelete = { confirmDeleteItem = uiState.selectedReview }
        )
    } else {
        // List view
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("例行") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                if (uiState.isLoggedIn) {
                    FloatingActionButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "添加")
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
                        Button(onClick = onNavigateToSettings) { Text("前往设置") }
                    }
                } else {
                    // Frequency tabs
                    val tabs = FrequencyTab.entries
                    val selectedIndex = tabs.indexOf(uiState.selectedTab)
                    TabRow(selectedTabIndex = selectedIndex) {
                        tabs.forEachIndexed { index, tab ->
                            Tab(
                                selected = selectedIndex == index,
                                onClick = { viewModel.selectTab(tab) },
                                text = { Text(tab.label) }
                            )
                        }
                    }

                    when {
                        uiState.isLoading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        else -> {
                            val reviews = viewModel.filteredReviews()
                                .filter { it.id !in pendingDeleteIds }
                            ReviewListContent(
                                reviews = reviews,
                                progressMap = uiState.progressMap,
                                onClick = { viewModel.selectReview(it) },
                                onCircleClick = { progressDialogReview = it },
                                onSwipeDelete = { confirmDeleteItem = it }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add dialog
    if (showAddDialog) {
        AddReviewDialog(
            defaultFrequency = uiState.selectedTab.apiValue,
            onDismiss = { showAddDialog = false },
            onAdd = { text, frequency, category ->
                viewModel.addReview(text, frequency, category)
                showAddDialog = false
            }
        )
    }

    // Progress dialog (from circle click on list)
    progressDialogReview?.let { review ->
        ProgressSliderDialog(
            reviewText = review.text,
            currentProgress = uiState.progressMap[review.id] ?: 0f,
            onProgressChange = { viewModel.updateProgress(review.id, it) },
            onDismiss = { progressDialogReview = null },
            onComplete = {
                progressDialogReview = null
                viewModel.completeReview(review.id)
                scope.launch { snackbarHostState.showSnackbar("已完成") }
            }
        )
    }

    // Delete confirmation dialog (shared by swipe and detail page)
    confirmDeleteItem?.let { item ->
        AlertDialog(
            onDismissRequest = { confirmDeleteItem = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除「${item.text}」吗？") },
            confirmButton = {
                TextButton(onClick = {
                    val deleteId = item.id
                    confirmDeleteItem = null
                    // If in detail view, go back to list first
                    if (uiState.selectedReview?.id == deleteId) {
                        viewModel.selectReview(null)
                    }
                    pendingDeleteIds = pendingDeleteIds + deleteId
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "已删除",
                            actionLabel = "撤销",
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            pendingDeleteIds = pendingDeleteIds - deleteId
                        } else {
                            viewModel.deleteReview(deleteId)
                            pendingDeleteIds = pendingDeleteIds - deleteId
                        }
                    }
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteItem = null }) { Text("取消") }
            }
        )
    }
}

// ── Review List Content ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewListContent(
    reviews: List<NextReview>,
    progressMap: Map<String, Float>,
    onClick: (NextReview) -> Unit,
    onCircleClick: (NextReview) -> Unit,
    onSwipeDelete: (NextReview) -> Unit
) {
    if (reviews.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("还没有例行项", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text("点击 + 或告诉二狗来添加", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }
        items(reviews, key = { it.id }) { review ->
            SwipeToDeleteItem(
                review = review,
                progress = progressMap[review.id] ?: 0f,
                onClick = { onClick(review) },
                onCircleClick = { onCircleClick(review) },
                onSwipeDelete = { onSwipeDelete(review) }
            )
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

// ── Swipe To Delete Wrapper ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteItem(
    review: NextReview,
    progress: Float,
    onClick: () -> Unit,
    onCircleClick: () -> Unit,
    onSwipeDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onSwipeDelete()
                false // Don't complete dismiss, wait for confirmation
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.error, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        ReviewItemCard(review = review, progress = progress, onClick = onClick, onCircleClick = onCircleClick)
    }
}

// ── Review Item Card ──

@Composable
private fun ReviewItemCard(
    review: NextReview,
    progress: Float,
    onClick: () -> Unit,
    onCircleClick: () -> Unit
) {
    val dueColor = when (review.dueStatus) {
        "overdue" -> MaterialTheme.colorScheme.error
        "due_today" -> MaterialTheme.colorScheme.error
        "due_soon" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val freqLabel = when (review.frequency) {
        "daily" -> "每日"
        "weekly" -> "每周"
        "quarterly" -> "每季度"
        "yearly" -> "每年"
        else -> "每月"
    }

    val progressPercent = (progress * 100).toInt()

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle progress button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onCircleClick),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(32.dp),
                    color = when {
                        progressPercent >= 100 -> MaterialTheme.colorScheme.primary
                        progressPercent > 0 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 3.dp
                )
                if (progressPercent > 0) {
                    Text(
                        "$progressPercent",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(review.text, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(freqLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    review.category?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    review.dueLabel?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = dueColor)
                    }
                }
            }
        }
    }
}

// ── Review Detail Screen ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewDetailScreen(
    review: NextReview,
    progress: Float,
    onBack: () -> Unit,
    onProgressChange: (Float) -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val freqLabel = when (review.frequency) {
        "daily" -> "每日"
        "weekly" -> "每周"
        "quarterly" -> "每季度"
        "yearly" -> "每年"
        else -> "每月"
    }

    val progressPercent = (progress * 100).toInt()
    var showCompleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("例行详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Main card
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(review.text, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(freqLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        review.category?.let {
                            Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    review.dueLabel?.let {
                        val dueColor = when (review.dueStatus) {
                            "overdue" -> MaterialTheme.colorScheme.error
                            "due_today" -> MaterialTheme.colorScheme.error
                            "due_soon" -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        InfoRow(label = "到期状态", value = it, valueColor = dueColor)
                    }
                    review.lastCompleted?.let { InfoRow(label = "上次完成", value = formatDate(it)) }
                    review.notes?.let {
                        if (it.isNotBlank()) InfoRow(label = "备注", value = it)
                    }
                    review.createdAt?.let { InfoRow(label = "创建时间", value = formatDate(it)) }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress slider
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(durationMillis = 300),
                label = "progress"
            )
            val progressColor = when {
                progressPercent >= 100 -> MaterialTheme.colorScheme.primary
                progressPercent >= 70 -> MaterialTheme.colorScheme.tertiary
                progressPercent > 0 -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            }

            Text(
                "执行进度  $progressPercent%",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = progressColor
            )
            Spacer(modifier = Modifier.height(12.dp))
            Slider(
                value = progress,
                onValueChange = { newValue ->
                    onProgressChange(newValue)
                    if (newValue >= 1f && !showCompleteDialog) {
                        showCompleteDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = progressColor,
                    activeTrackColor = progressColor,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }

    // Complete confirmation dialog
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showCompleteDialog = false
                onProgressChange(0.95f) // snap back slightly
            },
            title = { Text("确认完成") },
            text = { Text("确定已完成「${review.text}」？") },
            confirmButton = {
                TextButton(onClick = {
                    showCompleteDialog = false
                    onComplete()
                }) { Text("完成") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCompleteDialog = false
                    onProgressChange(0.95f)
                }) { Text("取消") }
            }
        )
    }
}

// ── Progress Slider Dialog ──

@Composable
private fun ProgressSliderDialog(
    reviewText: String,
    currentProgress: Float,
    onProgressChange: (Float) -> Unit,
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    var localProgress by remember(currentProgress) { mutableStateOf(currentProgress) }
    val progressPercent = (localProgress * 100).toInt()
    var showCompleteConfirm by remember { mutableStateOf(false) }

    val progressColor = when {
        progressPercent >= 100 -> MaterialTheme.colorScheme.primary
        progressPercent >= 70 -> MaterialTheme.colorScheme.tertiary
        progressPercent > 0 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    }

    if (showCompleteConfirm) {
        AlertDialog(
            onDismissRequest = {
                showCompleteConfirm = false
                localProgress = 0.95f
                onProgressChange(0.95f)
            },
            title = { Text("确认完成") },
            text = { Text("确定已完成「$reviewText」？") },
            confirmButton = {
                TextButton(onClick = {
                    showCompleteConfirm = false
                    onComplete()
                }) { Text("完成") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCompleteConfirm = false
                    localProgress = 0.95f
                    onProgressChange(0.95f)
                }) { Text("取消") }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = {
                onProgressChange(localProgress)
                onDismiss()
            },
            title = null,
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Circular progress display
                    Box(
                        modifier = Modifier.size(96.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { localProgress },
                            modifier = Modifier.size(96.dp),
                            color = progressColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeWidth = 6.dp
                        )
                        Text(
                            "$progressPercent%",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = progressColor
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        reviewText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Slider(
                        value = localProgress,
                        onValueChange = { newValue ->
                            localProgress = newValue
                            if (newValue >= 1f) {
                                onProgressChange(1f)
                                showCompleteConfirm = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = progressColor,
                            activeTrackColor = progressColor,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onProgressChange(localProgress)
                    onDismiss()
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("关闭") }
            }
        )
    }
}

// ── Info Row ──

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (valueColor != Color.Unspecified) valueColor else Color.Unspecified
        )
    }
}

private fun formatDate(dateStr: String): String {
    return runCatching {
        val date = if (dateStr.contains("T")) {
            LocalDate.parse(dateStr.substringBefore("T"), DateTimeFormatter.ISO_LOCAL_DATE)
        } else {
            LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
        }
        date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }.getOrDefault(dateStr)
}

// ── Add Review Dialog ──

@Composable
private fun AddReviewDialog(
    defaultFrequency: String = "daily",
    onDismiss: () -> Unit,
    onAdd: (String, String, String?) -> Unit
) {
    var text by remember { mutableStateOf(TextFieldValue()) }
    var frequency by remember { mutableStateOf(defaultFrequency) }
    var category by remember { mutableStateOf(TextFieldValue()) }

    val frequencies = listOf(
        "daily" to "每日",
        "weekly" to "每周",
        "monthly" to "每月",
        "yearly" to "每年"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加例行") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("例行内容") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("频率", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    frequencies.forEach { (key, label) ->
                        FilterChip(
                            selected = frequency == key,
                            onClick = { frequency = key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("分类（可选）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(text.text, frequency, category.text.ifBlank { null }) },
                enabled = text.text.isNotBlank()
            ) { Text("添加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
