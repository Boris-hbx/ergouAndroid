package com.ergou.app.ui.expense

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ergou.app.data.remote.dto.NextExpenseEntry
import com.ergou.app.data.remote.dto.NextExpenseItem
import com.ergou.app.data.remote.dto.NextExpenseItemRequest
import com.ergou.app.data.remote.dto.NextExpensePhoto
import com.ergou.app.data.remote.dto.NextParsePreview
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val NEXT_BASE_URL = "https://next-boris.fly.dev"

/**
 * Unified expense edit dialog for both new and edit modes.
 * Follows the HTML mockup design: title → amount+currency → date/time → items → description → photos → buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditDialog(
    isNew: Boolean,
    entry: NextExpenseEntry?,
    existingPhotos: List<NextExpensePhoto>,
    sessionToken: String,
    preview: NextParsePreview?,
    previewVersion: Int,
    dialogPhotos: List<Uri>,
    isAnalyzing: Boolean,
    isSaving: Boolean,
    isDeleting: Boolean,
    isUploadingPhoto: Boolean,
    isLoading: Boolean,
    dialogError: String?,
    onDismiss: () -> Unit,
    onSave: (amount: Double, notes: String, tags: List<String>?, currency: String, date: String?, time: String?, description: String?, items: List<NextExpenseItemRequest>?) -> Unit,
    onDelete: () -> Unit,
    onAnalyze: (memoText: String?) -> Unit,
    onAddDialogPhotos: (List<Uri>) -> Unit,
    onRemoveDialogPhoto: (Int) -> Unit,
    onUploadPhoto: (Uri) -> Unit,
    onDeletePhoto: (String) -> Unit,
    onSwitchToBatch: () -> Unit
) {
    // Local edit state — initialized from entry, updated via LaunchedEffect when AI preview arrives
    val entryKey = entry?.id.orEmpty()

    var title by remember(entryKey) {
        mutableStateOf(
            if (entry != null) TextFieldValue(entry.notes?.substringBefore("\n---\n") ?: "")
            else TextFieldValue()
        )
    }
    var amount by remember(entryKey) {
        mutableStateOf(
            if (entry != null) {
                // Show negative for refund entries
                val v = if (entry.tags?.contains("退款") == true) -entry.amount else entry.amount
                if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()
            } else ""
        )
    }
    var currency by remember(entryKey) {
        mutableStateOf(entry?.currency ?: "CAD")
    }
    var selectedDate by remember(entryKey) {
        mutableStateOf<LocalDate?>(
            entry?.date?.let {
                try { LocalDate.parse(it.substringBefore("T")) } catch (_: Exception) { null }
            }
        )
    }
    var selectedTime by remember(entryKey) {
        mutableStateOf<String?>(
            // Try to extract time from notes metadata (time:HH:mm)
            entry?.notes?.let { notes ->
                if (notes.contains("\n---\n")) {
                    val meta = notes.substringAfter("\n---\n")
                    meta.lines().firstOrNull { it.startsWith("time:") }?.removePrefix("time:")
                } else null
            }
            // Fallback: legacy date field with T
            ?: entry?.date?.let { d ->
                if (d.contains("T")) d.substringAfter("T").take(5) else null
            }
        )
    }
    var description by remember(entryKey) {
        mutableStateOf(
            if (entry != null) {
                val notesText = entry.notes ?: ""
                if (notesText.contains("\n---\n")) {
                    val meta = notesText.substringAfter("\n---\n")
                    // Filter out time: metadata line, keep only description text
                    val descText = meta.lines().filter { !it.startsWith("time:") }.joinToString("\n").trim()
                    TextFieldValue(descText)
                } else TextFieldValue()
            } else TextFieldValue()
        )
    }

    // When AI preview arrives, directly mutate existing state objects
    LaunchedEffect(previewVersion) {
        if (preview != null && previewVersion > 0) {
            Timber.d("[EditDialog] LaunchedEffect filling fields: merchant='%s' amount=%.2f date=%s time=%s",
                preview.merchant, preview.totalAmount, preview.date, preview.time)
            title = TextFieldValue(preview.merchant)
            val v = preview.totalAmount
            amount = if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()
            currency = preview.currency
            preview.date?.let {
                try { selectedDate = LocalDate.parse(it) } catch (_: Exception) { }
            }
            preview.time?.let { selectedTime = it }
            preview.description?.let { description = TextFieldValue(it) }
        }
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var descriptionExpanded by remember { mutableStateOf(false) }
    var sameReceipt by remember { mutableStateOf(true) }
    var showFullScreenPhoto by remember { mutableStateOf<String?>(null) }

    val currencies = listOf("CAD", "CNY")

    // Photo picker (gallery)
    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            if (isNew) onAddDialogPhotos(uris) else uris.forEach { onUploadPhoto(it) }
        }
    }

    // Camera capture
    val context = LocalContext.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let { uri ->
                if (isNew) onAddDialogPhotos(listOf(uri)) else onUploadPhoto(uri)
            }
        }
    }
    // Prepare camera URI helper
    fun prepareCameraUri(): Uri {
        val photoFile = java.io.File(
            context.cacheDir.resolve("photos").also { it.mkdirs() },
            "receipt_${System.currentTimeMillis()}.jpg"
        )
        return androidx.core.content.FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", photoFile
        )
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = prepareCameraUri()
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    val dateDisplay = (selectedDate ?: LocalDate.now()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val timeDisplay = selectedTime ?: "票据时间"

    val items: List<NextExpenseItem>? = entry?.items ?: preview?.items?.map { p ->
        NextExpenseItem(name = p.name, quantity = p.quantity, unitPrice = p.unitPrice, amount = p.amount, specs = p.specs)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 32.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    // ── Header ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (isNew) "记一笔" else "编辑账单",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "关闭")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── 标题 ──
                    Text("标题", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    CompactInput(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = "消费标题...",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── 金额 + 币种 (same row) ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "金额",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        CompactInput(
                            value = TextFieldValue(amount),
                            onValueChange = { amount = it.text },
                            placeholder = "0.00",
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            currencies.forEach { c ->
                                val selected = currency == c
                                Box(
                                    modifier = Modifier
                                        .height(32.dp)
                                        .background(
                                            if (selected) Color(0xFFEDE9FE) else Color.White,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .then(
                                            Modifier.border(
                                                1.dp,
                                                if (selected) Color(0xFF7C5CFC) else Color(0xFFDDDDDD),
                                                RoundedCornerShape(6.dp)
                                            )
                                        )
                                        .clickable { currency = c }
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        c,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) Color(0xFF6D28D9) else Color(0xFF888888),
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── 日期 / 时间 (side by side) ──
                    Text("日期 / 时间", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().height(34.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Date bar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color(0xFFF4F4F5), RoundedCornerShape(6.dp))
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { showDatePicker = true }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("\uD83D\uDCC5 $dateDisplay", style = MaterialTheme.typography.bodySmall)
                                Text("›", color = Color(0xFFBBBBBB), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        // Time bar
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .background(Color(0xFFF4F4F5), RoundedCornerShape(6.dp))
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { showTimePicker = true }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("\uD83D\uDD53", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    timeDisplay,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (selectedTime != null) Color(0xFF333333) else Color(0xFFAAAAAA)
                                )
                            }
                        }
                    }

                    // ── 明细 (items card) ──
                    if (!items.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("明细", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        ItemsCard(
                            items = items,
                            currency = currency,
                            subtotal = preview?.subtotal,
                            tax = preview?.tax,
                            tip = preview?.tip,
                            totalAmount = amount.toDoubleOrNull()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── 描述 (collapsible) ──
                    Text("描述", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(modifier = Modifier.animateContentSize()) {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text("添加描述...") },
                            maxLines = if (descriptionExpanded) Int.MAX_VALUE else 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (description.text.length > 40) {
                            Text(
                                if (descriptionExpanded) "收起" else "展开",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .clickable { descriptionExpanded = !descriptionExpanded }
                                    .padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── 票据照片 ──
                    Text("票据照片", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))

                    // Existing photos (for edit mode)
                    if (existingPhotos.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            items(existingPhotos, key = { it.id }) { photo ->
                                val photoUrl = "$NEXT_BASE_URL/api/uploads/${photo.storagePath?.removePrefix("/data/uploads/") ?: photo.filename}"
                                Box(modifier = Modifier.size(56.dp)) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(photoUrl)
                                            .crossfade(true)
                                            .httpHeaders(NetworkHeaders.Builder().add("Cookie", "session=$sessionToken").build())
                                            .build(),
                                        contentDescription = "照片",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { showFullScreenPhoto = photoUrl },
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 4.dp, y = (-4).dp)
                                            .size(18.dp)
                                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.8f), CircleShape)
                                            .clickable { onDeletePhoto(photo.id) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "删除",
                                            modifier = Modifier.size(10.dp),
                                            tint = MaterialTheme.colorScheme.onError
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Dialog photos (for new mode, not yet uploaded)
                    if (dialogPhotos.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            itemsIndexed(dialogPhotos) { index, uri ->
                                Box(modifier = Modifier.size(56.dp)) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "照片 ${index + 1}",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { showFullScreenPhoto = uri.toString() },
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 4.dp, y = (-4).dp)
                                            .size(18.dp)
                                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.8f), CircleShape)
                                            .clickable { onRemoveDialogPhoto(index) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "删除",
                                            modifier = Modifier.size(10.dp),
                                            tint = MaterialTheme.colorScheme.onError
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Add photo buttons (gallery + camera)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Gallery picker
                        Card(
                            modifier = Modifier
                                .size(56.dp)
                                .clickable { photoLauncher.launch("image/*") },
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(8.dp),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "从相册选",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        // Camera capture
                        Card(
                            modifier = Modifier
                                .size(56.dp)
                                .clickable {
                                    if (context.checkSelfPermission(android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                        val uri = prepareCameraUri()
                                        cameraUri = uri
                                        cameraLauncher.launch(uri)
                                    } else {
                                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(8.dp),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "拍照",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        if (isUploadingPhoto) {
                            Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            }
                        }
                    }

                    // Multi-photo mode toggle (new mode only, 2+ photos)
                    if (isNew && dialogPhotos.size >= 2) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = sameReceipt,
                                onClick = { sameReceipt = true },
                                label = { Text("同一单据") }
                            )
                            FilterChip(
                                selected = !sameReceipt,
                                onClick = { sameReceipt = false },
                                label = { Text("不同单据") }
                            )
                        }
                    }

                    // Error message
                    if (dialogError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            dialogError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── 让二狗分析 (full width, own row) ──
                    val analyzeEnabled = (dialogPhotos.isNotEmpty() || existingPhotos.isNotEmpty() || title.text.isNotBlank() || description.text.isNotBlank()) && !isAnalyzing
                    val analyzeClick = {
                        if (!sameReceipt && isNew && dialogPhotos.size >= 2) {
                            onSwitchToBatch()
                        } else {
                            onAnalyze(description.text.ifBlank { title.text.ifBlank { null } })
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .then(
                                if (isAnalyzing) {
                                    Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                                } else {
                                    Modifier.background(
                                        brush = Brush.linearGradient(
                                            colors = if (analyzeEnabled) listOf(
                                                Color(0xFF7C5CFC),
                                                Color(0xFFA78BFA)
                                            ) else listOf(
                                                Color(0xFFBBAAFF),
                                                Color(0xFFD4C8FF)
                                            )
                                        )
                                    )
                                }
                            )
                            .then(
                                if (analyzeEnabled) Modifier.clickable { analyzeClick() }
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isAnalyzing) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "二狗分析中...",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Text(
                                "让二狗分析",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ── Action buttons row ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!isNew) {
                            // Delete button
                            TextButton(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                if (isDeleting) {
                                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("删除")
                                }
                            }
                        }
                        // Cancel
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("取消")
                        }
                        // Save
                        Button(
                            onClick = {
                                val rawAmount = amount.toDoubleOrNull() ?: return@Button
                                // Backend rejects negative amounts — store as positive with "退款" tag
                                val isRefund = rawAmount < 0
                                val parsedAmount = if (isRefund) -rawAmount else rawAmount
                                val baseTags = preview?.tags ?: entry?.tags
                                val tags = if (isRefund && baseTags?.contains("退款") != true) {
                                    (baseTags ?: emptyList()) + "退款"
                                } else baseTags
                                val dateStr = selectedDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                val itemReqs = preview?.items?.map {
                                    NextExpenseItemRequest(
                                        name = it.name, quantity = it.quantity,
                                        unitPrice = it.unitPrice, amount = it.amount, specs = it.specs
                                    )
                                }?.ifEmpty { null }
                                onSave(
                                    parsedAmount, title.text, tags, currency,
                                    dateStr, selectedTime,
                                    description.text.ifBlank { null },
                                    itemReqs
                                )
                            },
                            enabled = (amount.toDoubleOrNull() ?: 0.0) != 0.0 && !isSaving,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text("保存")
                        }
                    }
                }
            }
        }
    }

    // Date picker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (selectedDate ?: LocalDate.now())
                .atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // DatePicker returns UTC midnight millis — parse in UTC to avoid off-by-one
                        selectedDate = Instant.ofEpochMilli(millis).atZone(java.time.ZoneOffset.UTC).toLocalDate()
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

    // Time picker
    if (showTimePicker) {
        val initHour = selectedTime?.substringBefore(":")?.toIntOrNull() ?: 12
        val initMinute = selectedTime?.substringAfter(":")?.toIntOrNull() ?: 0
        val timePickerState = rememberTimePickerState(
            initialHour = initHour,
            initialMinute = initMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("选择时间") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    selectedTime = "%02d:%02d".format(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("取消") }
            }
        )
    }

    // Delete confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除记录") },
            text = { Text("确定要删除这条记账记录吗？") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") }
            }
        )
    }

    // Full-screen photo preview
    showFullScreenPhoto?.let { photoUrl ->
        Dialog(
            onDismissRequest = { showFullScreenPhoto = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { showFullScreenPhoto = null },
                contentAlignment = Alignment.Center
            ) {
                val data: Any = if (photoUrl.startsWith("content://")) Uri.parse(photoUrl) else photoUrl
                val model = if (photoUrl.startsWith("https://")) {
                    ImageRequest.Builder(LocalContext.current)
                        .data(data)
                        .crossfade(true)
                        .httpHeaders(NetworkHeaders.Builder().add("Cookie", "session=$sessionToken").build())
                        .build()
                } else {
                    ImageRequest.Builder(LocalContext.current)
                        .data(data)
                        .crossfade(true)
                        .build()
                }
                AsyncImage(
                    model = model,
                    contentDescription = "照片",
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

/**
 * Compact 32dp text input with thin border, matching currency chip height.
 */
@Composable
private fun CompactInput(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val hasContent = value.text.isNotEmpty()
    val borderColor = if (hasContent) Color(0xFF7C5CFC) else Color(0xFFDDDDDD)
    val bgColor = if (hasContent) Color(0xFFFAFAFF) else Color.White

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF222222)),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier
            .height(32.dp)
            .background(bgColor, RoundedCornerShape(6.dp))
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.text.isEmpty()) {
                    Text(placeholder, style = MaterialTheme.typography.bodySmall, color = Color(0xFFCCCCCC))
                }
                innerTextField()
            }
        }
    )
}

private val taxKeywords = listOf(
    "tax", "hst", "gst", "pst", "qst", "vat", "tip", "gratuity",
    "service fee", "delivery fee", "surcharge",
    "税", "小费", "服务费", "配送费", "附加费"
)

private fun isTaxOrFee(item: NextExpenseItem): Boolean {
    val lower = item.name.lowercase()
    return taxKeywords.any { lower.contains(it) }
}

/**
 * Items card: products → subtotal → tax/fees → total
 */
@Composable
private fun ItemsCard(
    items: List<NextExpenseItem>,
    currency: String,
    subtotal: Double? = null,
    tax: Double? = null,
    tip: Double? = null,
    totalAmount: Double? = null
) {
    val products = items.filter { !isTaxOrFee(it) }
    val taxItems = items.filter { isTaxOrFee(it) }
    val computedSubtotal = subtotal?.takeIf { it > 0 } ?: products.sumOf { it.amount }
    val computedTotal = totalAmount ?: items.sumOf { it.amount }

    val headerText = buildString {
        append("${products.size} 项商品")
        if (taxItems.isNotEmpty()) append(" + ${taxItems.size} 项税费")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFAFAFA))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(headerText, style = MaterialTheme.typography.labelSmall, color = Color(0xFF888888))
            }

            // Product rows
            products.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(color = Color(0xFFF5F5F5))
                }
                ItemRow(item = item, currency = currency)
            }

            // Subtotal
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFAFAFA))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("小计（${products.size}项）", style = MaterialTheme.typography.labelSmall, color = Color(0xFF999999))
                Text(formatCurrencyAmount(computedSubtotal, currency), style = MaterialTheme.typography.labelSmall, color = Color(0xFF999999))
            }

            // Tax/fee rows
            if (taxItems.isNotEmpty()) {
                HorizontalDivider(color = Color(0xFFF0F0F0))
                taxItems.forEachIndexed { index, item ->
                    if (index > 0) HorizontalDivider(color = Color(0xFFFFF9C4))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFFDE7))
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "🏷️ ${item.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFF57F17)
                            )
                            if (!item.specs.isNullOrBlank()) {
                                Text(item.specs, style = MaterialTheme.typography.labelSmall, color = Color(0xFFF57F17).copy(alpha = 0.7f))
                            }
                        }
                        Text(
                            formatCurrencyAmount(item.amount, ""),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFF57F17)
                        )
                    }
                }
            }

            // Total row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFFEDE9FE), Color(0xFFE0E7FF))))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💰 总计", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Color(0xFF6D28D9))
                    Text(formatCurrencyAmount(computedTotal, currency), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF6D28D9))
                }
            }
        }
    }
}

@Composable
private fun ItemRow(item: NextExpenseItem, currency: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f, fill = false))
                Spacer(modifier = Modifier.width(4.dp))
                LanguageTag("原始")
            }
            if (!item.specs.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.specs, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f, fill = false))
                    Spacer(modifier = Modifier.width(4.dp))
                    val isChineseOriginal = item.name.any { it.code in 0x4E00..0x9FFF }
                    LanguageTag(if (isChineseOriginal) "英文翻译" else "中文翻译")
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (item.quantity != 1.0) {
                Text(
                    "x${if (item.quantity == item.quantity.toLong().toDouble()) item.quantity.toLong().toString() else item.quantity.toString()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(formatCurrencyAmount(item.amount, ""), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun LanguageTag(text: String) {
    val (color, bgColor) = when {
        text.contains("中文") -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primaryContainer
        text.contains("英文") -> Color(0xFF0277BD) to Color(0xFFE1F5FE)
        else -> MaterialTheme.colorScheme.onSurfaceVariant to MaterialTheme.colorScheme.surfaceVariant
    }
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(3.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    )
}
