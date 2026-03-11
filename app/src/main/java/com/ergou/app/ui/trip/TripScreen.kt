package com.ergou.app.ui.trip

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.window.DialogProperties
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextTrip
import com.ergou.app.data.remote.dto.NextTripItem
import com.ergou.app.data.remote.dto.NextTripItemPhoto
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ── Reimburse status colors ──

val ReimburseColorPending = Color(0xFFD4A017)
val ReimburseColorSubmitted = Color(0xFF4A90D9)
val ReimburseColorApproved = Color(0xFF43A047)
val ReimburseColorRejected = Color(0xFFE53935)
val ReimburseColorNa = Color(0xFF9E9E9E)

val ReimburseBgPending = Color(0xFFFFF8E1)
val ReimburseBgSubmitted = Color(0xFFE3F2FD)
val ReimburseBgApproved = Color(0xFFE8F5E9)
val ReimburseBgRejected = Color(0xFFFFEBEE)
val ReimburseBgNa = Color(0xFFF5F5F5)

fun reimburseColor(status: String): Color = when (status) {
    "pending" -> ReimburseColorPending
    "submitted" -> ReimburseColorSubmitted
    "approved" -> ReimburseColorApproved
    "rejected" -> ReimburseColorRejected
    "na" -> ReimburseColorNa
    else -> ReimburseColorNa
}

fun reimburseBgColor(status: String): Color = when (status) {
    "pending" -> ReimburseBgPending
    "submitted" -> ReimburseBgSubmitted
    "approved" -> ReimburseBgApproved
    "rejected" -> ReimburseBgRejected
    "na" -> ReimburseBgNa
    else -> ReimburseBgNa
}

// ── Main Screen ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripScreen(
    onBack: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: TripViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showTripForm by remember { mutableStateOf(false) }
    var editingTrip by remember { mutableStateOf<NextTrip?>(null) }
    var showItemForm by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<NextTripItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { event ->
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = event.message,
                    actionLabel = event.actionLabel,
                    duration = SnackbarDuration.Short
                )
                when (result) {
                    SnackbarResult.ActionPerformed -> event.onAction()
                    SnackbarResult.Dismissed -> event.onDismiss()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.selectedTrip?.title ?: "出差管理") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.selectedTrip != null) viewModel.clearSelection() else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (uiState.selectedTrip != null) {
                        IconButton(onClick = {
                            editingTrip = uiState.selectedTrip
                            showTripForm = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "编辑行程")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (uiState.isLoggedIn && uiState.selectedTrip == null) {
                FloatingActionButton(onClick = {
                    editingTrip = null
                    showTripForm = true
                }) {
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
            } else if (uiState.isLoading && uiState.selectedTrip == null && uiState.trips.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.selectedTrip != null) {
                TripDetail(
                    trip = uiState.selectedTrip!!,
                    uiState = uiState,
                    viewModel = viewModel,
                    onItemClick = { item ->
                        editingItem = item
                        showItemForm = true
                    },
                    onDeleteTrip = { viewModel.showConfirmDeleteTrip(uiState.selectedTrip!!) },
                    onAddItem = {
                        editingItem = null
                        showItemForm = true
                    }
                )
            } else if (uiState.trips.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("还没有出差记录", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("点击 + 或告诉二狗来创建", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item(key = "top_spacer") { Spacer(modifier = Modifier.height(4.dp)) }
                    items(uiState.sortedTrips, key = { it.first.id }) { (trip, status) ->
                        SwipeToDismissTripItem(
                            trip = trip,
                            status = status,
                            onClick = { viewModel.selectTrip(trip.id) },
                            onSwipeDelete = { viewModel.showConfirmDeleteTrip(trip) }
                        )
                    }
                    item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Trip form dialog (create / edit)
    if (showTripForm) {
        TripFormDialog(
            trip = editingTrip,
            onDismiss = { showTripForm = false; editingTrip = null },
            onSave = { title, dest, from, to, purpose, notes, currency ->
                if (editingTrip != null) {
                    viewModel.updateTrip(editingTrip!!.id, title, dest, from, to, purpose, notes, currency)
                } else {
                    viewModel.addTrip(title, dest, from, to, purpose, currency)
                }
                showTripForm = false
                editingTrip = null
            }
        )
    }

    // Item form bottom sheet (create / edit)
    if (showItemForm && uiState.selectedTrip != null) {
        val tripId = uiState.selectedTrip!!.id
        val tripCurrency = uiState.selectedTrip!!.currency
        val context = LocalContext.current
        ItemFormDialog(
            item = editingItem,
            tripCurrency = tripCurrency,
            isUploading = editingItem?.let { it.id in uiState.uploadingItemIds } ?: false,
            viewModel = viewModel,
            onDismiss = { showItemForm = false; editingItem = null },
            onSave = { type, desc, amount, date, itemCurrency, status, notes, pendingPhotos ->
                if (editingItem != null) {
                    viewModel.updateItem(tripId, editingItem!!.id, type, desc, amount, date, status, notes)
                } else {
                    viewModel.addItem(tripId, type, desc, amount ?: 0.0, date, status, notes, itemCurrency, pendingPhotos, context)
                }
                showItemForm = false
                editingItem = null
            },
            onDelete = if (editingItem != null) {
                {
                    viewModel.showConfirmDeleteItem(editingItem!!)
                    showItemForm = false
                    editingItem = null
                }
            } else null
        )
    }

    // Delete trip confirmation
    uiState.confirmDeleteTrip?.let { trip ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissConfirmDeleteTrip() },
            title = { Text("删除行程") },
            text = { Text("确定删除「${trip.title}」？所有费用项和照片都会被删除。") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteTrip(trip.id) }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissConfirmDeleteTrip() }) { Text("取消") }
            }
        )
    }

    // Delete item confirmation
    uiState.confirmDeleteItem?.let { item ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissConfirmDeleteItem() },
            title = { Text("删除费用") },
            text = { Text("确定删除此费用项？") },
            confirmButton = {
                TextButton(onClick = {
                    uiState.selectedTrip?.let { trip ->
                        viewModel.confirmDeleteItem(trip.id, item.id)
                    }
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissConfirmDeleteItem() }) { Text("取消") }
            }
        )
    }
}

// ── Trip List ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDismissTripItem(trip: NextTrip, status: TripStatus, onClick: () -> Unit, onSwipeDelete: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onSwipeDelete()
            }
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        TripListItem(trip = trip, status = status, onClick = onClick)
    }
}

@Composable
fun TripListItem(trip: NextTrip, status: TripStatus, onClick: () -> Unit) {
    val isOngoing = status == TripStatus.ONGOING
    val statusColor = when (status) {
        TripStatus.ONGOING -> MaterialTheme.colorScheme.primary
        TripStatus.PLANNED -> MaterialTheme.colorScheme.tertiary
        TripStatus.COMPLETED -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isOngoing) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isOngoing) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(trip.title, style = MaterialTheme.typography.bodyLarge, fontWeight = if (isOngoing) FontWeight.Bold else FontWeight.Normal)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            status.label,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (trip.destination != null) {
                        Text(trip.destination, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    val dates = listOfNotNull(trip.dateFrom, trip.dateTo).joinToString(" ~ ")
                    if (dates.isNotBlank()) {
                        Text(dates, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (trip.totalAmount > 0) {
                        Text(
                            "${currencySymbol(trip.currency)}${"%.2f".format(trip.totalAmount)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (trip.itemCount > 0) {
                        Text("${trip.itemCount} 笔费用", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                trip.reimburseSummary?.let { summary ->
                    if (summary.total > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        ReimburseSummaryBar(summary.approved, summary.submitted, summary.pending, summary.rejected, summary.na, summary.total)
                    }
                }
            }
        }
    }
}

@Composable
fun ReimburseSummaryBar(approved: Int, submitted: Int, pending: Int, rejected: Int, na: Int, total: Int) {
    if (total == 0) return
    val parts = listOf(
        approved to ReimburseColorApproved,
        submitted to ReimburseColorSubmitted,
        pending to ReimburseColorPending,
        rejected to ReimburseColorRejected,
        na to ReimburseColorNa
    ).filter { it.first > 0 }

    Row(
        modifier = Modifier.fillMaxWidth().height(4.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        parts.forEach { (count, color) ->
            Surface(
                modifier = Modifier.weight(count.toFloat()).height(4.dp),
                shape = RoundedCornerShape(2.dp),
                color = color
            ) {}
        }
    }
}

// ── Trip Detail (redesigned) ──

@Composable
fun TripDetail(
    trip: NextTrip,
    uiState: TripUiState,
    viewModel: TripViewModel,
    onItemClick: (NextTripItem) -> Unit,
    onDeleteTrip: () -> Unit,
    onAddItem: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Header: subtitle with destination and dates
            item(key = "header") {
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    val subtitle = buildString {
                        if (!trip.destination.isNullOrBlank()) append(trip.destination)
                        val dates = listOfNotNull(trip.dateFrom, trip.dateTo).joinToString(" ~ ")
                        if (dates.isNotBlank()) {
                            if (isNotEmpty()) append(" \u00B7 ")
                            append(dates)
                        }
                    }
                    if (subtitle.isNotBlank()) {
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!trip.purpose.isNullOrBlank()) {
                        Text(
                            trip.purpose,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Day groups
            val dayGroups = uiState.detailDayGroups
            if (dayGroups.isEmpty()) {
                item(key = "empty_items") {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("暂无费用明细", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("点击 + 添加费用项", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                dayGroups.forEach { group ->
                    item(key = "day_${group.date}") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${group.date} ${group.displayDate}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                    items(group.items, key = { it.id }) { item ->
                        SwipeToDismissItemRow(
                            item = item,
                            currency = trip.currency,
                            isUploading = item.id in uiState.uploadingItemIds,
                            viewModel = viewModel,
                            onClick = { onItemClick(item) },
                            onSwipeDelete = { viewModel.showConfirmDeleteItem(item) }
                        )
                    }
                }
            }

            // Bottom spacer for FAB + summary bar
            item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(120.dp)) }
        }

        // FAB above summary bar
        FloatingActionButton(
            onClick = onAddItem,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 72.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "添加事项")
        }

        // Bottom summary bar
        val pendingCount = trip.reimburseSummary?.pending ?: 0
        val symbol = currencySymbol(trip.currency)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("合计 ", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "$symbol${"%.2f".format(trip.totalAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (pendingCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ReimburseBgPending
                    ) {
                        Text(
                            "待提交 $pendingCount",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = ReimburseColorPending,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ── Item Row (redesigned) ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDismissItemRow(
    item: NextTripItem,
    currency: String,
    isUploading: Boolean,
    viewModel: TripViewModel,
    onClick: () -> Unit,
    onSwipeDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onSwipeDelete()
            }
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(8.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onClick)
                .padding(vertical = 10.dp)
        ) {
            TripItemRow(item = item, currency = currency)
            // Inline photo thumbnails (view-only, no add/delete)
            if (item.photos.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(start = 40.dp, end = 4.dp, top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(item.photos, key = { it.id }) { photo ->
                        PhotoThumbnail(photo = photo)
                    }
                }
            }
        }
    }
}

@Composable
fun TripItemRow(item: NextTripItem, currency: String) {
    val emoji = remember(item.type) { typeToEmoji(item.type) }
    val statusLabel = remember(item.reimburseStatus) { reimburseStatusLabel(item.reimburseStatus) }
    val statusColor = reimburseColor(item.reimburseStatus)
    val statusBg = reimburseBgColor(item.reimburseStatus)
    val displayCurrency = if (item.currency != currency) item.currency else currency

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Emoji
        Text(
            emoji,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(end = 12.dp, top = 2.dp)
        )
        // Description + notes
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.description.ifBlank { typeToLabel(item.type) },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (!item.notes.isNullOrBlank()) {
                Text(
                    item.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3
                )
            }
        }
        // Amount + status
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${currencySymbol(displayCurrency)}${"%.2f".format(item.amount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = statusBg
            ) {
                Text(
                    statusLabel,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ItemPhotoRow(
    item: NextTripItem,
    isUploading: Boolean,
    viewModel: TripViewModel
) {
    val context = LocalContext.current
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.uploadItemPhoto(item.id, it, context) }
    }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraUri?.let { viewModel.uploadItemPhoto(item.id, it, context) }
        }
    }

    LazyRow(
        modifier = Modifier.padding(start = 40.dp, end = 4.dp, top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(item.photos, key = { it.id }) { photo ->
            PhotoThumbnail(
                photo = photo,
                onDelete = { viewModel.deleteItemPhoto(photo.id) }
            )
        }
        item(key = "add_photo") {
            if (isUploading) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .clickable { showPhotoSourceDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加照片", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }
        }
    }

    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            title = { Text("添加照片") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("从相册选取")
                    }
                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                java.io.File.createTempFile("trip_photo_", ".jpg", context.cacheDir)
                            )
                            cameraUri = uri
                            takePicture.launch(uri)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
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
}

@Composable
fun PhotoThumbnail(photo: NextTripItemPhoto, onDelete: (() -> Unit)? = null, onClick: (() -> Unit)? = null, size: Int = 56) {
    Box(modifier = Modifier.size(size.dp).padding(top = 4.dp, end = 4.dp)) {
        AsyncImage(
            model = "${NextApiService.BASE_URL}/api/uploads/${photo.storagePath}",
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            contentScale = ContentScale.Crop
        )
        if (onDelete != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = "删除照片", tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
fun StatusChip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

// ── Trip Form Dialog (Create / Edit) ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripFormDialog(
    trip: NextTrip?,
    onDismiss: () -> Unit,
    onSave: (title: String, dest: String?, from: String?, to: String?, purpose: String?, notes: String?, currency: String?) -> Unit
) {
    val isEdit = trip != null
    var title by remember { mutableStateOf(TextFieldValue(trip?.title ?: "")) }
    var destination by remember { mutableStateOf(TextFieldValue(trip?.destination ?: "")) }
    var dateFrom by remember { mutableStateOf(trip?.dateFrom?.let { runCatching { LocalDate.parse(it) }.getOrNull() }) }
    var dateTo by remember { mutableStateOf(trip?.dateTo?.let { runCatching { LocalDate.parse(it) }.getOrNull() }) }
    var purpose by remember { mutableStateOf(TextFieldValue(trip?.purpose ?: "")) }
    var notes by remember { mutableStateOf(TextFieldValue(trip?.notes ?: "")) }
    var currency by remember { mutableStateOf(trip?.currency ?: "CAD") }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "编辑行程" else "创建出差") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("出差标题") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = destination, onValueChange = { destination = it }, label = { Text("目的地（可选）") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("开始日期", style = MaterialTheme.typography.labelMedium)
                        TextButton(onClick = { showFromPicker = true }) {
                            Text(dateFrom?.format(fmt) ?: "选择日期")
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("结束日期", style = MaterialTheme.typography.labelMedium)
                        TextButton(onClick = { showToPicker = true }) {
                            Text(dateTo?.format(fmt) ?: "选择日期")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = purpose, onValueChange = { purpose = it }, label = { Text("目的（可选）") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("备注（可选）") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                Spacer(modifier = Modifier.height(8.dp))
                Text("币种", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("CAD" to "CA$", "CNY" to "\u00A5").forEach { (code, symbol) ->
                        FilterChip(
                            selected = currency == code,
                            onClick = { currency = code },
                            label = { Text("$symbol $code") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        title.text,
                        destination.text.ifBlank { null },
                        dateFrom?.format(fmt),
                        dateTo?.format(fmt),
                        purpose.text.ifBlank { null },
                        notes.text.ifBlank { null },
                        currency
                    )
                },
                enabled = title.text.isNotBlank()
            ) { Text(if (isEdit) "保存" else "创建") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )

    if (showFromPicker) {
        TripDatePickerDialog(
            initialDate = dateFrom,
            onConfirm = { dateFrom = it; showFromPicker = false },
            onDismiss = { showFromPicker = false }
        )
    }
    if (showToPicker) {
        TripDatePickerDialog(
            initialDate = dateTo ?: dateFrom,
            onConfirm = { dateTo = it; showToPicker = false },
            onDismiss = { showToPicker = false }
        )
    }
}

// ── Item Form Dialog (redesigned from BottomSheet) ──

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemFormDialog(
    item: NextTripItem?,
    tripCurrency: String,
    isUploading: Boolean,
    viewModel: TripViewModel,
    onDismiss: () -> Unit,
    onSave: (type: String, desc: String, amount: Double?, date: String?, currency: String?, reimburseStatus: String?, notes: String?, pendingPhotos: List<Uri>) -> Unit,
    onDelete: (() -> Unit)?
) {
    val isEdit = item != null
    var type by remember { mutableStateOf(item?.type ?: "misc") }
    var description by remember { mutableStateOf(TextFieldValue(item?.description ?: "")) }
    var amount by remember { mutableStateOf(if (isEdit) (if (item!!.amount == 0.0) "" else "%.2f".format(item.amount)) else "") }
    var selectedDate by remember { mutableStateOf(item?.date?.let { runCatching { LocalDate.parse(it) }.getOrNull() }) }
    var currency by remember { mutableStateOf(item?.currency ?: tripCurrency) }
    var reimburseStatus by remember { mutableStateOf(item?.reimburseStatus ?: "pending") }
    var notes by remember { mutableStateOf(TextFieldValue(item?.notes ?: "")) }
    var analysisText by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }
    var previewImageModel by remember { mutableStateOf<Any?>(null) }
    var notesExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Pending photos for new items (not yet uploaded)
    var pendingPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    // Cached bytes for analysis (read immediately when picked, so no permission issues)
    var pendingPhotoBytes by remember { mutableStateOf<List<ByteArray>>(emptyList()) }

    val context = LocalContext.current
    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Analysis state
    val analysisState by viewModel.analysisState.collectAsState()

    // Auto-fill from analysis result
    LaunchedEffect(analysisState.preview) {
        analysisState.preview?.let { preview ->
            if (preview.merchant.isNotBlank()) description = TextFieldValue(preview.merchant)
            if (preview.totalAmount > 0) {
                amount = if (preview.totalAmount == preview.totalAmount.toLong().toDouble()) {
                    preview.totalAmount.toLong().toString()
                } else {
                    "%.2f".format(preview.totalAmount)
                }
            }
            if (preview.currency.isNotBlank()) currency = preview.currency
            preview.date?.let {
                try { selectedDate = LocalDate.parse(it) } catch (_: Exception) {}
            }
        }
    }

    // Clear analysis state when dialog opens
    LaunchedEffect(Unit) { viewModel.clearAnalysis() }

    // Photo pickers
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            if (isEdit) {
                viewModel.uploadItemPhoto(item!!.id, it, context)
            } else {
                // Read bytes immediately while we have permission
                val bytes = try {
                    context.contentResolver.openInputStream(it)?.use { s -> s.readBytes() }
                } catch (_: Exception) { null }
                if (bytes != null && bytes.isNotEmpty()) {
                    pendingPhotos = pendingPhotos + it
                    pendingPhotoBytes = pendingPhotoBytes + bytes
                }
            }
        }
    }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraUri?.let { uri ->
                if (isEdit) {
                    viewModel.uploadItemPhoto(item!!.id, uri, context)
                } else {
                    val bytes = try {
                        context.contentResolver.openInputStream(uri)?.use { s -> s.readBytes() }
                    } catch (_: Exception) { null }
                    pendingPhotos = pendingPhotos + uri
                    if (bytes != null) pendingPhotoBytes = pendingPhotoBytes + bytes
                }
            }
        }
    }

    val currencies = listOf("CAD" to "CAD", "CNY" to "CNY", "USD" to "USD", "HKD" to "HKD")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 8.dp, top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isEdit) "编辑费用" else "添加费用",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Type selector
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ITEM_TYPES) { (key, label) ->
                            FilterChip(
                                selected = type == key,
                                onClick = { type = key },
                                label = { Text("${typeToEmoji(key)} $label", style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Description
                    Text("标题", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Amount + Currency (compact)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            singleLine = true,
                            label = { Text("金额", style = MaterialTheme.typography.bodySmall) },
                            textStyle = MaterialTheme.typography.bodySmall,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).heightIn(min = 40.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenuBox(
                            expanded = currencyExpanded,
                            onExpandedChange = { currencyExpanded = it },
                            modifier = Modifier.width(100.dp)
                        ) {
                            OutlinedTextField(
                                value = currency,
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                label = { Text("币种", style = MaterialTheme.typography.bodySmall) },
                                textStyle = MaterialTheme.typography.bodySmall,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth().heightIn(min = 40.dp),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = currencyExpanded,
                                onDismissRequest = { currencyExpanded = false }
                            ) {
                                currencies.forEach { (code, display) ->
                                    DropdownMenuItem(
                                        text = { Text(display) },
                                        onClick = {
                                            currency = code
                                            currencyExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date
                    Text("日期", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            selectedDate?.format(fmt) ?: "选择日期",
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selectedDate != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Notes (collapsible)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { notesExpanded = !notesExpanded }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("备注", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (!notesExpanded && notes.text.isNotBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                notes.text.take(20) + if (notes.text.length > 20) "..." else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            if (notesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (notesExpanded) "收起" else "展开",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    AnimatedVisibility(visible = notesExpanded) {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Photos section
                    Text("票据照片", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Existing photos (edit mode)
                    if (isEdit && item!!.photos.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            items(item.photos, key = { it.id }) { photo ->
                                PhotoThumbnail(photo = photo, onDelete = { viewModel.deleteItemPhoto(photo.id) }, onClick = { previewImageModel = "${NextApiService.BASE_URL}/api/uploads/${photo.storagePath}" }, size = 80)
                            }
                        }
                    }

                    // Pending photos (new item, not yet uploaded)
                    if (!isEdit && pendingPhotos.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            itemsIndexed(pendingPhotos) { index, uri ->
                                Box(modifier = Modifier.size(80.dp).padding(top = 4.dp, end = 4.dp)) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "照片 ${index + 1}",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { previewImageModel = uri },
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(20.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                            .clickable {
                                                pendingPhotos = pendingPhotos.toMutableList().apply { removeAt(index) }
                                                pendingPhotoBytes = pendingPhotoBytes.toMutableList().apply { if (index < size) removeAt(index) }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "删除", tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Add photo button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPhotoSourceDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("上传中...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("添加票据", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    analysisState.error?.let { errorMsg ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Bottom buttons (fixed, not scrollable)
                HorizontalDivider()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: 二狗分析
                    val hasContent = pendingPhotos.isNotEmpty() || notes.text.isNotBlank()
                    Button(
                        onClick = {
                            android.widget.Toast.makeText(context, "photos=${pendingPhotos.size} bytes=${pendingPhotoBytes.size} notes=${notes.text.length}", android.widget.Toast.LENGTH_LONG).show()
                            viewModel.analyzeReceipt(pendingPhotoBytes, notes.text.ifBlank { null })
                        },
                        enabled = hasContent && !analysisState.isAnalyzing,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        if (analysisState.isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onTertiary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("分析中...", color = MaterialTheme.colorScheme.onTertiary)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("让二狗分析", color = MaterialTheme.colorScheme.onTertiary)
                        }
                    }

                    // Row 2: 删除 | 取消 | 保存
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onDelete != null) {
                            IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = onDismiss) {
                            Text("取消")
                        }
                        Button(
                            onClick = {
                                onSave(
                                    type,
                                    description.text,
                                    amount.toDoubleOrNull(),
                                    selectedDate?.format(fmt),
                                    currency,
                                    reimburseStatus,
                                    notes.text.ifBlank { null },
                                    pendingPhotos
                                )
                            },
                            enabled = if (isEdit) true else (description.text.isNotBlank() && amount.toDoubleOrNull() != null)
                        ) {
                            Text(if (isEdit) "保存" else "添加")
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        TripDatePickerDialog(
            initialDate = selectedDate,
            onConfirm = { selectedDate = it; showDatePicker = false },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            title = { Text("添加照片") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("从相册选取")
                    }
                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                java.io.File.createTempFile("trip_photo_", ".jpg", context.cacheDir)
                            )
                            cameraUri = uri
                            takePicture.launch(uri)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
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

    // Delete confirmation dialog
    if (showDeleteConfirm && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定删除此费用项？") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) { Text("确定", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") }
            }
        )
    }

    // Photo preview dialog
    if (previewImageModel != null) {
        Dialog(
            onDismissRequest = { previewImageModel = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = previewImageModel,
                    contentDescription = "照片预览",
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { previewImageModel = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "关闭", tint = Color.White)
                }
            }
        }
    }
}

// ── Date Picker ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDatePickerDialog(
    initialDate: LocalDate?,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = (initialDate ?: LocalDate.now())
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate())
                }
            }) { Text("确认") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

// ── Helpers ──

fun currencySymbol(currency: String): String = when (currency) {
    "CNY" -> "\u00A5"
    "CAD" -> "CA$"
    "USD" -> "$"
    "HKD" -> "HK$"
    else -> "$currency "
}

/** Copy a content URI to app cache, returning a file URI that remains readable. */
private fun copyToCache(context: android.content.Context, uri: Uri): Uri? {
    return try {
        val file = java.io.File.createTempFile("pick_", ".jpg", context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        Uri.fromFile(file)
    } catch (e: Exception) {
        timber.log.Timber.w(e, "[Trip] copyToCache failed uri=%s", uri)
        null
    }
}
