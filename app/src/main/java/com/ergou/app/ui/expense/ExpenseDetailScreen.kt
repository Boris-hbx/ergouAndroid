package com.ergou.app.ui.expense

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.network.httpHeaders
import coil3.network.NetworkHeaders
import com.ergou.app.data.remote.dto.NextExpenseItem
import com.ergou.app.data.remote.dto.NextExpensePhoto
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val NEXT_BASE_URL = "https://next-boris.fly.dev"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExpenseDetailScreen(
    expenseId: String?,
    sessionToken: String,
    onBack: () -> Unit,
    viewModel: ExpenseDetailViewModel = koinViewModel(parameters = { parametersOf(expenseId) })
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var showFullScreenPhoto by remember { mutableStateOf<String?>(null) }
    var showDeletePhotoConfirm by remember { mutableStateOf<String?>(null) }

    // Camera temp file URI
    val context = LocalContext.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    // Photo picker (GetContent for broad device compatibility, e.g. Huawei EMUI)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadPhoto(it) }
    }

    // Camera capture
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let { viewModel.uploadPhoto(it) }
        }
    }

    // Camera permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val photoFile = File(context.cacheDir, "photos").apply { mkdirs() }
                .let { File(it, "camera_${System.currentTimeMillis()}.jpg") }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    // Scan receipt photo picker
    val scanPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.parseReceipt(it) }
    }

    val scanCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let { viewModel.parseReceipt(it) }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ExpenseDetailEvent.NavigateBack -> onBack()
                is ExpenseDetailEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val isNewScan = expenseId == null
    val title = when {
        uiState.parsePreview != null -> "收据预览"
        uiState.isParsing -> "解析中..."
        isNewScan -> "拍照记账"
        else -> "记账详情"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.parsePreview != null) viewModel.cancelParse() else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (!isNewScan && uiState.entry != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.isParsing -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("AI 正在解析收据...", style = MaterialTheme.typography.bodyLarge)
                        Text("可能需要一段时间", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            isNewScan && uiState.parsePreview == null && uiState.parseError == null -> {
                // Show scan entry: pick photo to start
                ScanEntryContent(
                    modifier = Modifier.padding(padding),
                    onPickGallery = { scanPickerLauncher.launch("image/*") },
                    onCamera = {
                        val photoFile = File(context.cacheDir, "photos").apply { mkdirs() }
                            .let { File(it, "scan_${System.currentTimeMillis()}.jpg") }
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                        cameraUri = uri
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )
            }
            uiState.parseError != null && uiState.parsePreview == null -> {
                // Parse error with fallback
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Text(uiState.parseError!!, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.clearError(); onBack() }) {
                            Text("手动输入")
                        }
                    }
                }
            }
            else -> {
                // Edit form (for existing expense or parse preview)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Amount
                    OutlinedTextField(
                        value = uiState.amount,
                        onValueChange = { viewModel.updateAmount(it) },
                        label = { Text("金额") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    // Currency
                    Text("币种", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("CAD", "CNY").forEach { c ->
                            FilterChip(
                                selected = uiState.currency == c,
                                onClick = { viewModel.updateCurrency(c) },
                                label = { Text(c, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Notes
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = { viewModel.updateNotes(it) },
                        label = { Text("描述") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    // Date
                    Text("日期", style = MaterialTheme.typography.labelMedium)
                    TextButton(onClick = { showDatePicker = true }) {
                        Text(uiState.date ?: "今天")
                    }

                    Spacer(Modifier.height(12.dp))

                    // Tags
                    Text("标签", style = MaterialTheme.typography.labelMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ExpenseViewModel.PRESET_TAGS.forEach { tag ->
                            FilterChip(
                                selected = tag in uiState.tags,
                                onClick = { viewModel.toggleTag(tag) },
                                label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                        uiState.tags.filter { it !in ExpenseViewModel.PRESET_TAGS }.forEach { tag ->
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.toggleTag(tag) },
                                label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = uiState.customTagInput,
                        onValueChange = { viewModel.updateCustomTagInput(it) },
                        label = { Text("新标签（逗号分隔）") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Item lines (read-only)
                    val items = uiState.entry?.items ?: uiState.parsePreview?.items?.map { p ->
                        NextExpenseItem(name = p.name, quantity = p.quantity, unitPrice = p.unitPrice, amount = p.amount, specs = p.specs)
                    }
                    if (!items.isNullOrEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))
                        Text("明细", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        items.forEach { item ->
                            ItemLineRow(item)
                        }
                    }

                    // Photos section (only for existing expense, not scan preview)
                    if (!isNewScan || uiState.parsePreview == null) {
                        uiState.entry?.let {
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(8.dp))
                            PhotoSection(
                                photos = uiState.photos,
                                sessionToken = sessionToken,
                                isUploading = uiState.isUploadingPhoto,
                                onAddPhoto = { showPhotoSourceDialog = true },
                                onPhotoClick = { showFullScreenPhoto = it },
                                onDeletePhoto = { showDeletePhotoConfirm = it }
                            )
                            // AI analysis button for existing expenses with photos
                            if (uiState.photos.isNotEmpty() && uiState.parsePreview == null) {
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        scanPickerLauncher.launch("image/*")
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("二狗分析")
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Save / Confirm button
                    if (uiState.parsePreview != null) {
                        Button(
                            onClick = { viewModel.confirmParseAndSave() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState.amount.toDoubleOrNull() != null && !uiState.isSaving
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                            }
                            Text("确认保存")
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.cancelParse() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("取消")
                        }
                    } else if (!isNewScan) {
                        Button(
                            onClick = { viewModel.saveChanges() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState.amount.toDoubleOrNull() != null && !uiState.isSaving
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                            }
                            Text("保存修改")
                        }
                    }

                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (uiState.date?.let { LocalDate.parse(it) } ?: LocalDate.now())
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.updateDate(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    }
                    showDatePicker = false
                }) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Delete confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除记录") },
            text = { Text("确定要删除这条记账记录吗？") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteExpense(); showDeleteConfirm = false }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") }
            }
        )
    }

    // Photo source selection dialog
    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            title = { Text("添加照片") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false
                            photoPickerLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("从相册选择")
                    }
                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false
                            val photoFile = File(context.cacheDir, "photos").apply { mkdirs() }
                                .let { File(it, "camera_${System.currentTimeMillis()}.jpg") }
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                            cameraUri = uri
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("拍照")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoSourceDialog = false }) { Text("取消") }
            }
        )
    }

    // Full screen photo viewer
    showFullScreenPhoto?.let { photoUrl ->
        Dialog(
            onDismissRequest = { showFullScreenPhoto = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showFullScreenPhoto = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(photoUrl)
                        .crossfade(true)
                        .httpHeaders(NetworkHeaders.Builder().add("Cookie", "session=$sessionToken").build())
                        .build(),
                    contentDescription = "照片",
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }

    // Delete photo confirmation
    showDeletePhotoConfirm?.let { photoId ->
        AlertDialog(
            onDismissRequest = { showDeletePhotoConfirm = null },
            title = { Text("删除照片") },
            text = { Text("确定删除这张照片吗？") },
            confirmButton = {
                TextButton(onClick = { viewModel.deletePhoto(photoId); showDeletePhotoConfirm = null }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePhotoConfirm = null }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun ScanEntryContent(
    modifier: Modifier = Modifier,
    onPickGallery: () -> Unit,
    onCamera: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("选择收据照片", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text("AI 将自动识别收据内容", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onPickGallery) {
            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("从相册选择")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onCamera) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("拍照")
        }
    }
}

@Composable
private fun ItemLineRow(item: NextExpenseItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, style = MaterialTheme.typography.bodyMedium)
            if (!item.specs.isNullOrBlank()) {
                Text(item.specs, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                formatCurrencyAmount(item.amount, ""),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (item.quantity != 1.0) {
                Text(
                    "${item.quantity} x ${item.unitPrice ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PhotoSection(
    photos: List<NextExpensePhoto>,
    sessionToken: String,
    isUploading: Boolean,
    onAddPhoto: () -> Unit,
    onPhotoClick: (String) -> Unit,
    onDeletePhoto: (String) -> Unit
) {
    val context = LocalContext.current

    Text("照片", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(4.dp))

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(photos, key = { it.id }) { photo ->
            val photoUrl = "$NEXT_BASE_URL/api/uploads/${photo.storagePath?.removePrefix("/data/uploads/") ?: photo.filename}"
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(photoUrl)
                        .crossfade(true)
                        .httpHeaders(NetworkHeaders.Builder().add("Cookie", "session=$sessionToken").build())
                        .build(),
                    contentDescription = "收据照片",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onPhotoClick(photoUrl) },
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = { onDeletePhoto(photo.id) },
                    modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "删除",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        item {
            if (isUploading) {
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                }
            } else {
                Card(
                    modifier = Modifier.size(80.dp).clickable { onAddPhoto() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = "添加照片")
                    }
                }
            }
        }
    }
}
