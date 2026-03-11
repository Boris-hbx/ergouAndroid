package com.ergou.app.ui.expense

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ergou.app.data.remote.dto.NextExpenseEntry
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

// Next 后端 9 大分类对应的 emoji，用于标签和卡片展示
private val TAG_EMOJI_MAP = mapOf(
    // 食品杂货类
    "超市" to "\uD83C\uDFEA", "杂货" to "\uD83C\uDFEA", "生鲜" to "\uD83E\uDD6C",
    "肉类" to "\uD83E\uDD69", "蔬菜" to "\uD83E\uDD6C", "水果" to "\uD83C\uDF4E", "海鲜" to "\uD83E\uDD90",
    // 餐饮类
    "餐饮" to "\uD83C\uDF5C", "外卖" to "\uD83D\uDEF5", "咖啡" to "\u2615",
    // 交通类
    "交通" to "\uD83D\uDE97", "加油" to "\u26FD", "停车" to "\uD83C\uDD7F\uFE0F",
    // 购物类
    "购物" to "\uD83D\uDED2", "日用" to "\uD83E\uDDF4",
    // 住房类
    "住房" to "\uD83C\uDFE0", "房租" to "\uD83C\uDFE0",
    // 娱乐类
    "娱乐" to "\uD83C\uDFAC", "旅游" to "\u2708\uFE0F",
    // 医疗类
    "医疗" to "\uD83C\uDFE5", "药品" to "\uD83D\uDC8A",
    // 教育类
    "教育" to "\uD83D\uDCDA", "书籍" to "\uD83D\uDCD6"
)

private fun getTagEmoji(tags: List<String>?): String {
    if (tags.isNullOrEmpty()) return "\uD83D\uDCB0"
    for (tag in tags) {
        TAG_EMOJI_MAP[tag]?.let { return it }
    }
    return "\uD83D\uDCB0"
}

fun formatCurrencyAmount(amount: Double, currency: String): String {
    return try {
        val format = NumberFormat.getCurrencyInstance(
            when (currency) {
                "CNY" -> Locale.CHINA
                "USD" -> Locale.US
                "CAD" -> Locale.CANADA
                else -> Locale.getDefault()
            }
        )
        format.currency = Currency.getInstance(currency)
        format.format(amount)
    } catch (_: Exception) {
        "${"%.2f".format(amount)} $currency"
    }
}

/** Display amount: negative for refund entries (tagged "退款"), positive otherwise */
fun NextExpenseEntry.displayAmount(): Double =
    if (tags?.contains("退款") == true) -amount else amount

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExpenseScreen(
    onBack: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToScan: () -> Unit = {},
    refreshTrigger: Boolean = false,
    sessionToken: String = "",
    viewModel: ExpenseViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var actionSheetExpense by remember { mutableStateOf<NextExpenseEntry?>(null) }
    var confirmDeleteExpense by remember { mutableStateOf<NextExpenseEntry?>(null) }
    var showRecycleBin by remember { mutableStateOf(false) }

    // Quick camera: FAB → camera → new dialog with photo
    var quickCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val quickCameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            quickCameraUri?.let { uri ->
                viewModel.openNewDialog()
                viewModel.addDialogPhotos(listOf(uri))
            }
        }
    }
    fun prepareQuickCameraUri(): android.net.Uri {
        val photoFile = java.io.File(
            context.cacheDir.resolve("photos").also { it.mkdirs() },
            "quick_${System.currentTimeMillis()}.jpg"
        )
        return androidx.core.content.FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", photoFile
        )
    }
    val quickCameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = prepareQuickCameraUri()
            quickCameraUri = uri
            quickCameraLauncher.launch(uri)
        }
    }

    // Refresh data when returning from detail/scan page
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger) {
            viewModel.refreshExpenses()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记账", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (uiState.isLoggedIn) {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            ExpensePeriod.entries.forEachIndexed { index, period ->
                                SegmentedButton(
                                    selected = uiState.selectedPeriod == period,
                                    onClick = { viewModel.selectPeriod(period) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index,
                                        ExpensePeriod.entries.size
                                    )
                                ) {
                                    Text(period.label, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        IconButton(onClick = { /* 图表预留 */ }) {
                            Icon(
                                Icons.Default.BarChart,
                                contentDescription = "统计",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (uiState.isLoggedIn) {
                Column(horizontalAlignment = Alignment.End) {
                    SmallFloatingActionButton(
                        onClick = {
                            if (context.checkSelfPermission(android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                val uri = prepareQuickCameraUri()
                                quickCameraUri = uri
                                quickCameraLauncher.launch(uri)
                            } else {
                                quickCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "拍照记账")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    FloatingActionButton(onClick = { viewModel.openNewDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = "手动记一笔")
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
        Column(modifier = Modifier.fillMaxSize()) {
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
            } else if (uiState.showBatchScan) {
                BatchScanSection(viewModel = viewModel, uiState = uiState, context = context)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Period navigator (◀ label ▶)
                    item(key = "period_nav") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.navigatePeriod(-1) }) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "上一个时段")
                            }
                            Text(
                                uiState.periodLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { viewModel.navigatePeriod(1) }) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "下一个时段")
                            }
                        }
                    }

                    // Total summary (single line)
                    item(key = "total_summary") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "总支出",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                formatCurrencyAmount(uiState.filteredTotal, uiState.dominantCurrency),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Tag filter chips
                    item(key = "tag_chips") {
                        val tags = uiState.sortedTags
                        if (tags.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                item {
                                    FilterChip(
                                        selected = uiState.selectedTag == null,
                                        onClick = { viewModel.filterByTag(null) },
                                        label = { Text("全部", style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                                items(tags) { tag ->
                                    FilterChip(
                                        selected = uiState.selectedTag == tag,
                                        onClick = { viewModel.filterByTag(tag) },
                                        label = { Text("${TAG_EMOJI_MAP[tag] ?: ""} $tag", style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }
                    }

                    // Loading / Empty / Day groups
                    if (uiState.isLoading) {
                        item(key = "loading") {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    } else if (uiState.filteredExpenses.isEmpty()) {
                        item(key = "empty") {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("\uD83D\uDCB0", style = MaterialTheme.typography.displayMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("还没有记账记录", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("点击 + 记一笔", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        uiState.dayGroups.forEachIndexed { index, group ->
                            if (index > 0) {
                                item(key = "divider_${group.date}") {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                            item(key = "header_${group.date}") {
                                DayGroupHeader(
                                    group = group,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                            items(group.entries, key = { it.id }) { expense ->
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    ExpenseItem(
                                        expense = expense,
                                        onClick = { viewModel.openEditDialog(expense.id) },
                                        onLongClick = { actionSheetExpense = expense }
                                    )
                                }
                            }
                        }

                        item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
        // Recycle bin FAB — bottom-left, light blue badge
        if (uiState.deletedItems.isNotEmpty() && uiState.isLoggedIn && !uiState.showBatchScan) {
            SmallFloatingActionButton(
                onClick = { showRecycleBin = true },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "回收站",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .size(14.dp)
                            .background(Color(0xFF64B5F6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${uiState.deletedItems.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
        }
    }

    // Unified edit dialog (new + edit)
    if (uiState.editingExpenseId != null) {
        val isNew = uiState.editingExpenseId == "new"
        ExpenseEditDialog(
            isNew = isNew,
            entry = uiState.editingEntry,
            existingPhotos = uiState.editingPhotos,
            sessionToken = sessionToken,
            preview = uiState.dialogPreview,
            previewVersion = uiState.dialogPreviewVersion,
            dialogPhotos = uiState.dialogPhotos,
            isAnalyzing = uiState.isDialogAnalyzing,
            isSaving = if (isNew) uiState.isSaving else uiState.isEditSaving,
            isDeleting = uiState.isEditDeleting,
            isUploadingPhoto = uiState.isEditUploadingPhoto,
            isLoading = uiState.isEditLoading,
            dialogError = uiState.dialogError,
            onDismiss = { viewModel.clearDialogState() },
            onSave = { amount, notes, tags, currency, date, time, description, items ->
                // Build notes: title\n---\ntime:HH:mm\ndescription
                val metaParts = mutableListOf<String>()
                if (!time.isNullOrBlank()) metaParts.add("time:$time")
                if (!description.isNullOrBlank()) metaParts.add(description)
                val fullNotes = if (metaParts.isNotEmpty()) "$notes\n---\n${metaParts.joinToString("\n")}" else notes
                if (isNew) {
                    if (uiState.dialogPhotos.isNotEmpty() || items != null) {
                        viewModel.addExpenseWithPhotos(amount, fullNotes, tags, currency, date, items, uiState.dialogPhotos)
                    } else {
                        viewModel.addExpense(amount, fullNotes, tags, currency, date)
                        viewModel.clearDialogState()
                    }
                } else {
                    viewModel.saveEditChanges(amount, fullNotes, tags, currency, date, time, description, items)
                }
            },
            onDelete = { viewModel.deleteEditExpense() },
            onAnalyze = { memo -> viewModel.analyzeDialogPhotos(memo) },
            onAddDialogPhotos = { uris -> viewModel.addDialogPhotos(uris) },
            onRemoveDialogPhoto = { index -> viewModel.removeDialogPhoto(index) },
            onUploadPhoto = { uri -> viewModel.uploadEditPhoto(uri) },
            onDeletePhoto = { id -> viewModel.deleteEditPhoto(id) },
            onSwitchToBatch = {
                viewModel.startBatchFromDialog()
            }
        )
    }


    // Long-press action bottom sheet
    actionSheetExpense?.let { expense ->
        val title = expense.notes?.substringBefore("\n---\n")?.take(20) ?: "消费"
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { actionSheetExpense = null },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                // Header
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                // Edit
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            actionSheetExpense = null
                            viewModel.openEditDialog(expense.id)
                        }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("编辑", style = MaterialTheme.typography.bodyLarge)
                }
                // Share (coming soon)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* TODO: 待开发 */ }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "分享",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "待开发",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                // Delete
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            confirmDeleteExpense = expense
                            actionSheetExpense = null
                        }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("删除", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    // Delete confirmation dialog
    confirmDeleteExpense?.let { expense ->
        val title = expense.notes?.substringBefore("\n---\n")?.take(20) ?: "消费"
        AlertDialog(
            onDismissRequest = { confirmDeleteExpense = null },
            title = { Text("删除记录") },
            text = { Text("确定删除「$title」？\n删除后可在回收站恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteExpense(expense.id)
                    confirmDeleteExpense = null
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteExpense = null }) { Text("取消") }
            }
        )
    }

    // Recycle bin dialog
    var previewingDeleted by remember { mutableStateOf<DeletedExpenseItem?>(null) }

    if (showRecycleBin && previewingDeleted == null) {
        AlertDialog(
            onDismissRequest = { showRecycleBin = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("回收站")
                    if (uiState.deletedItems.isNotEmpty()) {
                        TextButton(onClick = {
                            viewModel.clearRecycleBin()
                            showRecycleBin = false
                        }) {
                            Text("清空", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            },
            text = {
                if (uiState.deletedItems.isEmpty()) {
                    Text("回收站是空的", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "最近删除的 ${uiState.deletedItems.size} 条记录（最多保留10条）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        uiState.deletedItems.forEach { deleted ->
                            val entry = deleted.entry
                            val title = entry.notes?.substringBefore("\n---\n")?.take(20) ?: "未命名"
                            val amountText = formatCurrencyAmount(entry.displayAmount(), entry.currency)
                            val dateText = entry.date ?: ""
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { previewingDeleted = deleted },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                amountText,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            if (dateText.isNotEmpty()) {
                                                Text(
                                                    dateText,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                    TextButton(
                                        onClick = { viewModel.restoreExpense(deleted) },
                                        enabled = !uiState.isRestoring
                                    ) {
                                        if (uiState.isRestoring) {
                                            CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
                                        } else {
                                            Text("恢复")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRecycleBin = false }) { Text("关闭") }
            }
        )
    }

    // Deleted expense preview dialog (read-only)
    previewingDeleted?.let { deleted ->
        val entry = deleted.entry
        val previewTitle = entry.notes?.substringBefore("\n---\n") ?: "未命名"
        val previewDesc = entry.notes?.let {
            if (it.contains("\n---\n")) it.substringAfter("\n---\n") else null
        }
        Dialog(
            onDismissRequest = { previewingDeleted = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 32.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "已删除的账单",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        IconButton(onClick = { previewingDeleted = null }) {
                            Icon(Icons.Default.Close, contentDescription = "关闭")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title
                    Text("标题", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(previewTitle, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Amount + Currency
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("金额", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            val isRefundEntry = entry.tags?.contains("退款") == true
                            Text(
                                (if (isRefundEntry) "↩ " else "") + formatCurrencyAmount(entry.displayAmount(), entry.currency),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isRefundEntry) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                            )
                        }
                        Column {
                            Text("货币", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(entry.currency, style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Date
                    if (!entry.date.isNullOrBlank()) {
                        Text("日期", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(entry.date, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Tags
                    if (!entry.tags.isNullOrEmpty()) {
                        Text("标签", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            entry.tags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.secondaryContainer,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "${getTagEmoji(listOf(tag))} $tag",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Items
                    if (!entry.items.isNullOrEmpty()) {
                        Text("明细", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                entry.items.forEachIndexed { index, item ->
                                    if (index > 0) HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.name, style = MaterialTheme.typography.bodySmall)
                                            if (!item.specs.isNullOrBlank()) {
                                                Text(
                                                    item.specs,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Text(
                                            "%.2f".format(item.amount),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Description
                    if (!previewDesc.isNullOrBlank()) {
                        Text("描述", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            previewDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Cached photos
                    if (deleted.cachedPhotoPaths.isNotEmpty()) {
                        Text("票据照片", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(deleted.cachedPhotoPaths.size) { index ->
                                val file = java.io.File(deleted.cachedPhotoPaths[index])
                                AsyncImage(
                                    model = file,
                                    contentDescription = "照片 ${index + 1}",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { previewingDeleted = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("返回")
                        }
                        Button(
                            onClick = {
                                viewModel.restoreExpense(deleted)
                                previewingDeleted = null
                            },
                            enabled = !uiState.isRestoring,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (uiState.isRestoring) {
                                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text("恢复")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayGroupHeader(group: ExpenseDayGroup, modifier: Modifier = Modifier) {
    val currency = remember(group.entries) {
        group.entries.groupBy { it.currency }
            .maxByOrNull { it.value.size }?.key ?: "CAD"
    }
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            group.displayDate,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            formatCurrencyAmount(group.dayTotal, currency),
            style = MaterialTheme.typography.titleSmall,
            color = if (group.dayTotal < 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpenseItem(
    expense: NextExpenseEntry,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val dateTimeLabel = remember(expense.createdAt, expense.date) {
        formatExpenseDateTime(expense.createdAt, expense.date, expense.notes)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onLongClick() }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Title + tag badges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        expense.notes?.substringBefore("\n---\n") ?: "消费",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (!expense.tags.isNullOrEmpty()) {
                        expense.tags.take(2).forEach { tag ->
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                // Date/time
                if (dateTimeLabel.isNotEmpty()) {
                    Text(
                        dateTimeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (expense.photoCount > 0) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "有照片",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            val isRefund = expense.tags?.contains("退款") == true
            Text(
                (if (isRefund) "↩ " else "") + formatCurrencyAmount(expense.displayAmount(), expense.currency),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isRefund) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
            )
        }
    }
    }
}

private fun formatExpenseDateTime(createdAt: String?, date: String?, notes: String?): String {
    val dateStr = date?.substringBefore("T")
    // Extract time from notes metadata (format: "time:HH:mm" in the --- section)
    val timeFromNotes = notes?.let {
        if (it.contains("\n---\n")) {
            val meta = it.substringAfter("\n---\n")
            meta.lines().firstOrNull { line -> line.startsWith("time:") }?.removePrefix("time:")
        } else null
    }
    // Also try date field with T (legacy data)
    val timeFromDate = if (date != null && date.contains("T")) {
        try { date.substringAfter("T").take(5) } catch (_: Exception) { null }
    } else null

    val time = timeFromNotes ?: timeFromDate

    if (dateStr != null) {
        return if (time != null) "$dateStr | $time" else dateStr
    }
    return ""
}

// ── Batch Scan Section ──

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BatchScanSection(
    viewModel: ExpenseViewModel,
    uiState: ExpenseUiState,
    context: android.content.Context
) {
    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) viewModel.addPhotos(uris)
    }

    var editingResult by remember { mutableStateOf<ScanResultItem?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "scan_header") {
            Text(
                "扫描收据",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Photo preview row
        item(key = "photo_preview") {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(uiState.selectedPhotos) { index, uri ->
                    Box(modifier = Modifier.size(80.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "照片 ${index + 1}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { viewModel.removePhoto(index) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .background(
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "删除",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                }
                item {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                photoLauncher.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "添加照片",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (uiState.selectedPhotos.isNotEmpty()) {
                Text(
                    "已选择 ${uiState.selectedPhotos.size} 张照片",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Merge mode toggle
        if (uiState.selectedPhotos.size >= 2) {
            item(key = "merge_toggle") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !uiState.batchMergeMode,
                        onClick = { viewModel.setBatchMergeMode(false) },
                        label = { Text("不同单据") }
                    )
                    FilterChip(
                        selected = uiState.batchMergeMode,
                        onClick = { viewModel.setBatchMergeMode(true) },
                        label = { Text("同一单据") }
                    )
                }
            }
        }

        // Analyze button
        item(key = "analyze_btn") {
            Button(
                onClick = { viewModel.analyzePhotos(context) },
                enabled = uiState.selectedPhotos.isNotEmpty() && uiState.scanProgress == null && !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.scanProgress != null) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("分析中 ${uiState.scanProgress.first}/${uiState.scanProgress.second}...")
                } else {
                    Text("二狗分析")
                }
            }
        }

        // Scan results
        if (uiState.scanResults.isNotEmpty()) {
            item(key = "results_header") {
                Text(
                    "分析结果 (${uiState.scanResults.size} 条)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            items(uiState.scanResults, key = { it.id }) { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                result.effectiveNotes.ifBlank { "未知商家" },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                formatCurrencyAmount(result.effectiveAmount, result.effectiveCurrency),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Item lines
                        if (result.preview.items.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            result.preview.items.forEach { item ->
                                Text(
                                    "  - ${item.name} x${item.quantity.toInt()} ${"%.2f".format(item.amount)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Tags
                        result.effectiveTags?.let { tags ->
                            if (tags.isNotEmpty()) {
                                FlowRow(
                                    modifier = Modifier.padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    tags.forEach { tag ->
                                        Text("#$tag", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }

                        // Edit / Delete buttons
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { editingResult = result }) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("编辑")
                            }
                            TextButton(onClick = { viewModel.removeScanResult(result.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("删除", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Save all button
            item(key = "save_all") {
                Button(
                    onClick = { viewModel.saveAllResults(context) },
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("保存中...")
                    } else {
                        Text("确认保存全部")
                    }
                }
            }
        }

        item(key = "scan_bottom_spacer") { Spacer(modifier = Modifier.height(80.dp)) }
    }

    // Edit result dialog
    editingResult?.let { result ->
        EditScanResultDialog(
            result = result,
            onDismiss = { editingResult = null },
            onConfirm = { editAmount, notes, tags, editCurrency, editDate ->
                viewModel.updateScanResult(result.id, editAmount, notes, tags, editCurrency, editDate)
                editingResult = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditScanResultDialog(
    result: ScanResultItem,
    onDismiss: () -> Unit,
    onConfirm: (Double?, String?, List<String>?, String?, String?) -> Unit
) {
    var amount by remember { mutableStateOf(result.effectiveAmount.toString()) }
    var notes by remember { mutableStateOf(TextFieldValue(result.effectiveNotes)) }
    var tagsText by remember { mutableStateOf(TextFieldValue(result.effectiveTags?.joinToString(", ") ?: "")) }
    var currency by remember { mutableStateOf(result.effectiveCurrency) }
    var date by remember { mutableStateOf(TextFieldValue(result.effectiveDate ?: "")) }
    val currencies = listOf("CAD", "CNY")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑分析结果") },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("金额") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("币种", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    currencies.forEach { c ->
                        FilterChip(selected = currency == c, onClick = { currency = c }, label = { Text(c, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("商家/描述") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("日期 (yyyy-MM-dd)") },
                    placeholder = { Text("如 2026-03-09") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it },
                    label = { Text("标签（逗号分隔）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val tags = tagsText.text.split(",").map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { null }
                    onConfirm(amount.toDoubleOrNull(), notes.text.ifBlank { null }, tags, currency, date.text.ifBlank { null })
                },
                enabled = amount.toDoubleOrNull() != null
            ) { Text("确认") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
