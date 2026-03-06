package com.ergou.app.ui.trip

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
            if (uiState.isLoggedIn) {
                FloatingActionButton(onClick = {
                    if (uiState.selectedTrip != null) {
                        editingItem = null
                        showItemForm = true
                    } else {
                        editingTrip = null
                        showTripForm = true
                    }
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
                    onDeleteTrip = { viewModel.showConfirmDeleteTrip(uiState.selectedTrip!!) }
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
        ItemFormBottomSheet(
            item = editingItem,
            tripCurrency = tripCurrency,
            isUploading = editingItem?.let { it.id in uiState.uploadingItemIds } ?: false,
            viewModel = viewModel,
            onDismiss = { showItemForm = false; editingItem = null },
            onSave = { type, desc, amount, date, itemCurrency, status, notes ->
                if (editingItem != null) {
                    viewModel.updateItem(tripId, editingItem!!.id, type, desc, amount, date, status, notes)
                } else {
                    viewModel.addItem(tripId, type, desc, amount ?: 0.0, date, status, notes, itemCurrency)
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
    onDeleteTrip: () -> Unit
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
            // Inline photo thumbnails
            if (item.photos.isNotEmpty() || isUploading) {
                ItemPhotoRow(item = item, isUploading = isUploading, viewModel = viewModel)
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
fun PhotoThumbnail(photo: NextTripItemPhoto, onDelete: () -> Unit, size: Int = 56) {
    Box(modifier = Modifier.size(size.dp)) {
        AsyncImage(
            model = "${NextApiService.BASE_URL}/api/uploads/${photo.storagePath}",
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp)
                .size(18.dp)
                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Close, contentDescription = "删除照片", tint = Color.White, modifier = Modifier.size(10.dp))
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

// ── Item Form Bottom Sheet (redesigned) ──

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemFormBottomSheet(
    item: NextTripItem?,
    tripCurrency: String,
    isUploading: Boolean,
    viewModel: TripViewModel,
    onDismiss: () -> Unit,
    onSave: (type: String, desc: String, amount: Double?, date: String?, currency: String?, reimburseStatus: String?, notes: String?) -> Unit,
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

    val context = LocalContext.current
    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Photo pickers for the bottom sheet
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { item?.let { itm -> viewModel.uploadItemPhoto(itm.id, it, context) } }
    }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraUri?.let { uri -> item?.let { itm -> viewModel.uploadItemPhoto(itm.id, uri, context) } }
        }
    }

    val currencies = listOf("CAD" to "CAD", "CNY" to "CNY", "USD" to "USD", "HKD" to "HKD")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            // Type selector (compact horizontal scroll)
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

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text("描述", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Amount + Currency side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("金额", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                Column(modifier = Modifier.width(120.dp)) {
                    Text("币种", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = currencyExpanded,
                        onExpandedChange = { currencyExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = currency,
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
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
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedDate != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reimburse status
            Text("报销状态", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                REIMBURSE_STATUSES.forEach { (key, label) ->
                    val isSelected = reimburseStatus == key
                    val chipColor = reimburseColor(key)
                    val chipBg = reimburseBgColor(key)
                    FilterChip(
                        selected = isSelected,
                        onClick = { reimburseStatus = key },
                        label = {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipBg,
                            selectedLabelColor = chipColor
                        ),
                        border = if (isSelected) BorderStroke(1.5.dp, chipColor) else FilterChipDefaults.filterChipBorder(enabled = true, selected = false)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes
            Text("备注", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Photos section (only for existing items)
            if (isEdit) {
                Text("票据照片", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))

                // Existing photos
                if (item!!.photos.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        items(item.photos, key = { it.id }) { photo ->
                            PhotoThumbnail(photo = photo, onDelete = { viewModel.deleteItemPhoto(photo.id) }, size = 80)
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

                Spacer(modifier = Modifier.height(16.dp))

                // AI analysis section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        " 或 让二狗重新分析 ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = analysisText,
                    onValueChange = { analysisText = it },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    placeholder = { Text("上传新票据照片后可重新分析，或粘贴补充信息...", style = MaterialTheme.typography.bodySmall) },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        Toast.makeText(context, "二狗分析功能开发中", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF7C3AED), Color(0xFF6366F1))
                                ),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "二狗分析",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = {
                    onSave(
                        type,
                        description.text,
                        amount.toDoubleOrNull(),
                        selectedDate?.format(fmt),
                        currency,
                        reimburseStatus,
                        notes.text.ifBlank { null }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = if (isEdit) true else (description.text.isNotBlank() && amount.toDoubleOrNull() != null),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isEdit) "保存修改" else "添加费用", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            // Delete button for existing items
            if (onDelete != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("删除此费用", color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
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
