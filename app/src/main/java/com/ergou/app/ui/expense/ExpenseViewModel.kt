package com.ergou.app.ui.expense

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextExpenseCreateRequest
import com.ergou.app.data.remote.dto.NextExpenseEntry
import com.ergou.app.data.remote.dto.NextExpenseItemRequest
import com.ergou.app.data.remote.dto.NextExpenseSummary
import com.ergou.app.data.remote.dto.NextParsePreview
import com.ergou.app.data.remote.dto.NextTagTotal
import com.ergou.app.util.NextAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale
import java.util.UUID

private val Context.recycleBinDataStore by preferencesDataStore(name = "expense_recycle_bin")

data class ScanResultItem(
    val id: String = UUID.randomUUID().toString(),
    val preview: NextParsePreview,
    val photoUri: Uri,
    val allPhotoUris: List<Uri> = emptyList(),
    val editedAmount: Double? = null,
    val editedNotes: String? = null,
    val editedTags: List<String>? = null,
    val editedCurrency: String? = null,
    val editedDate: String? = null
) {
    val effectiveAmount: Double get() = editedAmount ?: preview.totalAmount
    val effectiveNotes: String get() = editedNotes ?: preview.merchant
    val effectiveTags: List<String>? get() = editedTags ?: preview.tags
    val effectiveCurrency: String get() = editedCurrency ?: preview.currency
    val effectiveDate: String? get() = editedDate ?: preview.date
    val photosToUpload: List<Uri> get() = allPhotoUris.ifEmpty { listOf(photoUri) }
}

enum class ExpensePeriod(val label: String, val apiValue: String) {
    TODAY("日", "day"),
    WEEK("周", "week"),
    MONTH("月", "month")
}

data class ExpenseDayGroup(
    val date: String,
    val displayDate: String,
    val entries: List<NextExpenseEntry>,
    val dayTotal: Double
)

@Serializable
data class DeletedExpenseItem(
    val entry: NextExpenseEntry,
    val deletedAt: Long = System.currentTimeMillis(),
    /** Local cached photo file paths (absolute) for restore */
    val cachedPhotoPaths: List<String> = emptyList()
)

data class ExpenseUiState(
    val expenses: List<NextExpenseEntry> = emptyList(),
    val summary: NextExpenseSummary? = null,
    val lastMonthSummary: NextExpenseSummary? = null,
    val availableTags: List<String> = emptyList(),
    val selectedTag: String? = null,
    val selectedPeriod: ExpensePeriod = ExpensePeriod.MONTH,
    val periodOffset: Int = 0,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val pendingDeleteId: String? = null,
    // Recycle bin (last 10 deleted items, in-memory only)
    val deletedItems: List<DeletedExpenseItem> = emptyList(),
    val isRestoring: Boolean = false,
    // Batch scan state
    val showBatchScan: Boolean = false,
    val selectedPhotos: List<Uri> = emptyList(),
    val scanProgress: Pair<Int, Int>? = null,
    val scanResults: List<ScanResultItem> = emptyList(),
    val isSaving: Boolean = false,
    val batchMergeMode: Boolean = false,
    // Edit dialog state
    val editingExpenseId: String? = null,
    val editingEntry: NextExpenseEntry? = null,
    val editingPhotos: List<com.ergou.app.data.remote.dto.NextExpensePhoto> = emptyList(),
    val isEditLoading: Boolean = false,
    val isEditSaving: Boolean = false,
    val isEditDeleting: Boolean = false,
    val isEditUploadingPhoto: Boolean = false,
    // Add dialog photo analysis
    val dialogPhotos: List<Uri> = emptyList(),
    val isDialogAnalyzing: Boolean = false,
    val dialogPreview: NextParsePreview? = null,
    val dialogPreviewVersion: Int = 0,
    val dialogError: String? = null
) {
    val filteredExpenses: List<NextExpenseEntry>
        get() = if (pendingDeleteId != null) expenses.filter { it.id != pendingDeleteId } else expenses

    val dayGroups: List<ExpenseDayGroup>
        get() {
            val grouped = filteredExpenses
                .groupBy { (it.date ?: "未知日期").substringBefore("T") }
                .toSortedMap(compareByDescending { it })
            return grouped.map { (date, entries) ->
                ExpenseDayGroup(
                    date = date,
                    displayDate = formatDisplayDate(date),
                    entries = entries,
                    dayTotal = entries.sumOf { if (it.tags?.contains("退款") == true) -it.amount else it.amount }
                )
            }
        }

    /** Tags sorted by frequency in current expenses (descending) */
    val sortedTags: List<String>
        get() {
            val tagCounts = mutableMapOf<String, Int>()
            for (expense in expenses) {
                expense.tags?.forEach { tag ->
                    tagCounts[tag] = (tagCounts[tag] ?: 0) + 1
                }
            }
            return tagCounts.entries.sortedByDescending { it.value }.map { it.key }
        }

    /** Total of filtered expenses (respects tag filter + pending delete) */
    val filteredTotal: Double
        get() = filteredExpenses.sumOf { if (it.tags?.contains("退款") == true) -it.amount else it.amount }

    /** Dominant currency from current expenses */
    val dominantCurrency: String
        get() = expenses
            .groupBy { it.currency }
            .maxByOrNull { it.value.size }
            ?.key ?: "CAD"

    /** Period navigation label */
    val periodLabel: String
        get() {
            val today = LocalDate.now()
            return when (selectedPeriod) {
                ExpensePeriod.MONTH -> {
                    val month = today.plusMonths(periodOffset.toLong())
                    "${month.year}年${month.monthValue}月"
                }
                ExpensePeriod.WEEK -> {
                    val weekStart = today.with(DayOfWeek.MONDAY).plusWeeks(periodOffset.toLong())
                    val weekEnd = weekStart.plusDays(6)
                    val fmtStart = weekStart.format(DateTimeFormatter.ofPattern("M月d日"))
                    val fmtEnd = weekEnd.format(DateTimeFormatter.ofPattern("M月d日"))
                    "$fmtStart - $fmtEnd"
                }
                ExpensePeriod.TODAY -> {
                    val day = today.plusDays(periodOffset.toLong())
                    day.format(DateTimeFormatter.ofPattern("M月d日 E", Locale.CHINESE))
                }
            }
        }
}

private fun formatDisplayDate(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr.substringBefore("T"))
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        when (date) {
            today -> "今天"
            yesterday -> "昨天"
            else -> date.format(DateTimeFormatter.ofPattern("M月d日 E", Locale.CHINESE))
        }
    } catch (_: Exception) {
        dateStr
    }
}

class ExpenseViewModel(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider,
    private val appContext: Context,
    private val receiptAnalyzer: com.ergou.app.data.repository.ReceiptAnalyzer
) : ViewModel() {

    companion object {
        val PRESET_TAGS = listOf("餐饮", "交通", "购物", "日用", "娱乐")
        private const val MAX_IMAGE_DIMENSION = 1920
        private const val JPEG_QUALITY = 80
        private val RECYCLE_BIN_KEY = stringPreferencesKey("deleted_items")
        private const val MAX_RECYCLE_BIN_SIZE = 10
    }

    private val recycleBinJson = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState

    init {
        // Load recycle bin from DataStore
        viewModelScope.launch {
            try {
                val prefs = appContext.recycleBinDataStore.data.first()
                val json = prefs[RECYCLE_BIN_KEY]
                if (!json.isNullOrBlank()) {
                    val items = recycleBinJson.decodeFromString<List<DeletedExpenseItem>>(json)
                    _uiState.value = _uiState.value.copy(deletedItems = items)
                    Timber.d("[Expense] 加载回收站 %d 条", items.size)
                }
            } catch (e: Exception) {
                Timber.w(e, "[Expense] 回收站加载失败")
            }
        }
        viewModelScope.launch {
            authProvider.isLoggedIn.collect { loggedIn ->
                _uiState.value = _uiState.value.copy(isLoggedIn = loggedIn)
                if (loggedIn) {
                    refreshAll()
                } else {
                    _uiState.value = _uiState.value.copy(expenses = emptyList(), summary = null, lastMonthSummary = null, availableTags = emptyList())
                }
            }
        }
    }

    private fun refreshAll() {
        refreshExpenses()
        refreshSummary()
        refreshTags()
    }

    fun selectPeriod(period: ExpensePeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period, periodOffset = 0, selectedTag = null)
        refreshExpenses()
        refreshSummary()
    }

    fun navigatePeriod(delta: Int) {
        _uiState.value = _uiState.value.copy(periodOffset = _uiState.value.periodOffset + delta)
        refreshExpenses()
        refreshSummary()
    }

    /** Navigate to the period window containing the given date */
    private fun navigateToDate(dateStr: String?) {
        val date = try { dateStr?.let { LocalDate.parse(it.substringBefore("T")) } } catch (_: Exception) { null } ?: return
        val today = LocalDate.now()
        val offset = when (_uiState.value.selectedPeriod) {
            ExpensePeriod.TODAY -> {
                java.time.temporal.ChronoUnit.DAYS.between(today, date).toInt()
            }
            ExpensePeriod.WEEK -> {
                val todayWeekStart = today.with(DayOfWeek.MONDAY)
                val dateWeekStart = date.with(DayOfWeek.MONDAY)
                java.time.temporal.ChronoUnit.WEEKS.between(todayWeekStart, dateWeekStart).toInt()
            }
            ExpensePeriod.MONTH -> {
                (date.year - today.year) * 12 + (date.monthValue - today.monthValue)
            }
        }
        _uiState.value = _uiState.value.copy(periodOffset = offset, selectedTag = null)
        refreshExpenses()
        refreshSummary()
    }

    fun refreshExpenses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val tag = _uiState.value.selectedTag
            val (from, to) = getPeriodDateRange(_uiState.value.selectedPeriod)
            Timber.d("[Expense] 查询账单 period=%s from=%s to=%s tag=%s", _uiState.value.selectedPeriod, from, to, tag)
            val result = nextApiService.getExpenses(from = from, to = to, tags = tag)
            result.fold(
                onSuccess = { entries ->
                    Timber.d("[Expense] 查询返回 %d 条", entries.size)
                    _uiState.value = _uiState.value.copy(expenses = entries, isLoading = false)
                },
                onFailure = { e ->
                    Timber.e(e, "[Expense] 刷新账单失败")
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "加载失败")
                }
            )
        }
    }

    private fun refreshSummary() {
        viewModelScope.launch {
            val period = _uiState.value.selectedPeriod.apiValue
            val result = nextApiService.getExpenseSummary(period)
            result.fold(
                onSuccess = { summary ->
                    _uiState.value = _uiState.value.copy(summary = summary)
                },
                onFailure = { Timber.w(it, "[Expense] 获取汇总失败") }
            )
        }
    }

    private fun refreshTags() {
        viewModelScope.launch {
            val result = nextApiService.getExpenseTags()
            result.fold(
                onSuccess = { apiTags ->
                    val merged = PRESET_TAGS + apiTags.filter { it !in PRESET_TAGS }
                    _uiState.value = _uiState.value.copy(availableTags = merged)
                },
                onFailure = {
                    Timber.w(it, "[Expense] 获取标签失败")
                    _uiState.value = _uiState.value.copy(availableTags = PRESET_TAGS)
                }
            )
        }
    }

    fun filterByTag(tag: String?) {
        _uiState.value = _uiState.value.copy(selectedTag = tag)
        refreshExpenses()
    }

    fun addExpense(amount: Double, notes: String, tags: List<String>?, currency: String, date: String? = null) {
        viewModelScope.launch {
            val result = nextApiService.createExpense(
                NextExpenseCreateRequest(amount = amount, notes = notes.ifBlank { null }, tags = tags, currency = currency, date = date)
            )
            result.fold(
                onSuccess = { entry -> navigateToDate(date ?: entry.date) },
                onFailure = { e ->
                    Timber.e(e, "[Expense] 记账失败")
                    _uiState.value = _uiState.value.copy(error = "记账失败：${e.message}")
                }
            )
        }
    }

    /**
     * Add expense with photo upload support (for dialog "二狗分析" results)
     */
    fun addExpenseWithPhotos(
        amount: Double, notes: String, tags: List<String>?, currency: String,
        date: String? = null, items: List<NextExpenseItemRequest>? = null,
        photoUris: List<Uri> = emptyList()
    ) {
        _uiState.value = _uiState.value.copy(isSaving = true)
        viewModelScope.launch {
            Timber.d("[Expense] 创建账单 amount=%.2f date=%s currency=%s tags=%s", amount, date, currency, tags)
            val result = nextApiService.createExpense(
                NextExpenseCreateRequest(
                    amount = amount, notes = notes.ifBlank { null },
                    tags = tags, currency = currency, date = date, items = items
                )
            )
            result.fold(
                onSuccess = { entry ->
                    Timber.d("[Expense] 创建成功 id=%s amount=%.2f date=%s currency=%s", entry.id, entry.amount, entry.date, entry.currency)
                    // Upload photos (compressed)
                    var failCount = 0
                    for (uri in photoUris) {
                        try {
                            val compressed = compressImage(appContext, uri)
                            if (compressed == null) {
                                Timber.w("[Expense] 照片压缩失败，跳过 entryId=%s", entry.id)
                                failCount++
                                continue
                            }
                            val uploadResult = nextApiService.uploadExpensePhotoBytes(entry.id, compressed)
                            if (uploadResult.isFailure) {
                                Timber.w("[Expense] 照片上传失败 entryId=%s", entry.id)
                                failCount++
                            }
                        } catch (e: Exception) {
                            Timber.w(e, "[Expense] 照片上传失败 entryId=%s", entry.id)
                            failCount++
                        }
                    }
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    if (failCount > 0) {
                        _uiState.value = _uiState.value.copy(
                            error = "记账已保存，但${failCount}张照片上传失败"
                        )
                    }
                    clearDialogState()
                    navigateToDate(date ?: entry.date)
                },
                onFailure = { e ->
                    Timber.e(e, "[Expense] 记账失败")
                    _uiState.value = _uiState.value.copy(isSaving = false, error = "记账失败：${e.message}")
                }
            )
        }
    }

    fun deleteExpense(id: String) {
        val entry = _uiState.value.expenses.find { it.id == id }
        // Optimistic: remove from list immediately
        _uiState.value = _uiState.value.copy(
            expenses = _uiState.value.expenses.filter { it.id != id }
        )
        viewModelScope.launch {
            // 1. Download photos to local cache before deleting (best-effort)
            val cachedPaths = if (entry != null && entry.photoCount > 0) {
                cachePhotosForEntry(id)
            } else emptyList()

            // 2. Delete from server
            val result = nextApiService.deleteExpense(id)
            result.fold(
                onSuccess = {
                    if (entry != null) addToRecycleBin(entry, cachedPaths)
                    refreshAll()
                },
                onFailure = { e ->
                    Timber.e(e, "[Expense] 删除失败")
                    // Restore to list on failure
                    refreshAll()
                    _uiState.value = _uiState.value.copy(error = "删除失败：${e.message}")
                }
            )
        }
    }

    /**
     * Download photos for an expense entry to local cache before deletion.
     * Returns list of cached file paths. Best-effort: failures are skipped.
     */
    private suspend fun cachePhotosForEntry(entryId: String): List<String> {
        val paths = mutableListOf<String>()
        try {
            val detail = nextApiService.getExpenseDetail(entryId).getOrNull() ?: return paths
            val photos = detail.photos.ifEmpty { detail.entry?.photos.orEmpty() }
            if (photos.isEmpty()) return paths

            val cacheDir = java.io.File(appContext.cacheDir, "recycle_bin/$entryId")
            cacheDir.mkdirs()

            for (photo in photos) {
                try {
                    val photoUrl = "https://next-boris.fly.dev/api/uploads/${photo.storagePath?.removePrefix("/data/uploads/") ?: photo.filename}"
                    val bytes = nextApiService.downloadPhotoBytes(photoUrl).getOrNull() ?: continue
                    val file = java.io.File(cacheDir, photo.filename)
                    file.writeBytes(bytes)
                    paths.add(file.absolutePath)
                    Timber.d("[Expense] 缓存照片 %s (%d KB)", photo.filename, bytes.size / 1024)
                } catch (e: Exception) {
                    Timber.w(e, "[Expense] 缓存照片失败 photo=%s", photo.id)
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "[Expense] 获取照片详情失败 entryId=%s", entryId)
        }
        return paths
    }

    private fun addToRecycleBin(entry: NextExpenseEntry, cachedPhotoPaths: List<String> = emptyList()) {
        val current = _uiState.value.deletedItems.toMutableList()
        // If bin is full, clean up photos of the evicted item
        if (current.size >= MAX_RECYCLE_BIN_SIZE) {
            val evicted = current.removeAt(current.lastIndex)
            cleanupCachedPhotos(evicted)
        }
        current.add(0, DeletedExpenseItem(entry, cachedPhotoPaths = cachedPhotoPaths))
        _uiState.value = _uiState.value.copy(deletedItems = current)
        Timber.d("[Expense] 已加入回收站 id=%s photos=%d, 回收站共 %d 条", entry.id, cachedPhotoPaths.size, current.size)
        persistRecycleBin(current)
    }

    /** Download photos using already-known metadata (from edit dialog) */
    private suspend fun cachePhotosFromMetadata(
        entryId: String,
        photos: List<com.ergou.app.data.remote.dto.NextExpensePhoto>
    ): List<String> {
        val paths = mutableListOf<String>()
        val cacheDir = java.io.File(appContext.cacheDir, "recycle_bin/$entryId")
        cacheDir.mkdirs()
        for (photo in photos) {
            try {
                val photoUrl = "https://next-boris.fly.dev/api/uploads/${photo.storagePath?.removePrefix("/data/uploads/") ?: photo.filename}"
                val bytes = nextApiService.downloadPhotoBytes(photoUrl).getOrNull() ?: continue
                val file = java.io.File(cacheDir, photo.filename)
                file.writeBytes(bytes)
                paths.add(file.absolutePath)
            } catch (e: Exception) {
                Timber.w(e, "[Expense] 缓存照片失败 photo=%s", photo.id)
            }
        }
        return paths
    }

    /** Clean up cached photo files for a recycle bin item */
    private fun cleanupCachedPhotos(item: DeletedExpenseItem) {
        for (path in item.cachedPhotoPaths) {
            try { java.io.File(path).delete() } catch (_: Exception) {}
        }
        // Also try to remove the parent directory
        try {
            val dir = java.io.File(appContext.cacheDir, "recycle_bin/${item.entry.id}")
            if (dir.exists()) dir.deleteRecursively()
        } catch (_: Exception) {}
    }

    fun restoreExpense(deletedItem: DeletedExpenseItem) {
        val entry = deletedItem.entry
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRestoring = true)
            val result = nextApiService.createExpense(
                NextExpenseCreateRequest(
                    amount = entry.amount,
                    notes = entry.notes,
                    tags = entry.tags,
                    currency = entry.currency,
                    date = entry.date
                )
            )
            result.fold(
                onSuccess = { newEntry ->
                    // Re-upload cached photos
                    var photoFail = 0
                    for (path in deletedItem.cachedPhotoPaths) {
                        try {
                            val file = java.io.File(path)
                            if (!file.exists()) { photoFail++; continue }
                            val bytes = file.readBytes()
                            val uploadResult = nextApiService.uploadExpensePhotoBytes(newEntry.id, bytes)
                            if (uploadResult.isFailure) photoFail++
                        } catch (e: Exception) {
                            Timber.w(e, "[Expense] 恢复照片上传失败")
                            photoFail++
                        }
                    }
                    // Clean up cached files
                    cleanupCachedPhotos(deletedItem)
                    // Remove from recycle bin
                    val updated = _uiState.value.deletedItems.filter { it !== deletedItem }
                    _uiState.value = _uiState.value.copy(deletedItems = updated, isRestoring = false)
                    persistRecycleBin(updated)
                    if (photoFail > 0 && deletedItem.cachedPhotoPaths.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(error = "已恢复，但${photoFail}张照片恢复失败")
                    }
                    Timber.d("[Expense] 已恢复 notes=%s photos=%d/%d",
                        entry.notes?.take(20), deletedItem.cachedPhotoPaths.size - photoFail, deletedItem.cachedPhotoPaths.size)
                    navigateToDate(entry.date)
                },
                onFailure = { e ->
                    Timber.e(e, "[Expense] 恢复失败")
                    _uiState.value = _uiState.value.copy(isRestoring = false, error = "恢复失败：${e.message}")
                }
            )
        }
    }

    fun clearRecycleBin() {
        // Clean up all cached photo files
        for (item in _uiState.value.deletedItems) {
            cleanupCachedPhotos(item)
        }
        _uiState.value = _uiState.value.copy(deletedItems = emptyList())
        persistRecycleBin(emptyList())
    }

    private fun persistRecycleBin(items: List<DeletedExpenseItem>) {
        viewModelScope.launch {
            try {
                appContext.recycleBinDataStore.edit { prefs ->
                    prefs[RECYCLE_BIN_KEY] = recycleBinJson.encodeToString(items)
                }
            } catch (e: Exception) {
                Timber.w(e, "[Expense] 回收站持久化失败")
            }
        }
    }

    // --- Swipe-to-delete (optimistic) ---

    fun confirmDelete(id: String) {
        _uiState.value = _uiState.value.copy(pendingDeleteId = id)
        Timber.d("[Expense] 待删除 id=%s", id)
    }

    fun executeDelete() {
        val id = _uiState.value.pendingDeleteId ?: return
        _uiState.value = _uiState.value.copy(pendingDeleteId = null)
        deleteExpense(id)
    }

    fun cancelDelete() {
        _uiState.value = _uiState.value.copy(pendingDeleteId = null)
        Timber.d("[Expense] 撤销删除")
    }

    // --- Add dialog photo analysis ---

    fun addDialogPhotos(uris: List<Uri>) {
        _uiState.value = _uiState.value.copy(
            dialogPhotos = _uiState.value.dialogPhotos + uris
        )
    }

    fun removeDialogPhoto(index: Int) {
        val photos = _uiState.value.dialogPhotos.toMutableList()
        if (index in photos.indices) {
            photos.removeAt(index)
            _uiState.value = _uiState.value.copy(dialogPhotos = photos)
        }
    }

    fun analyzeDialogPhotos(memoText: String? = null) {
        val localPhotos = _uiState.value.dialogPhotos
        val serverPhotos = _uiState.value.editingPhotos
        if (localPhotos.isEmpty() && serverPhotos.isEmpty() && memoText.isNullOrBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDialogAnalyzing = true, dialogPreview = null, dialogError = null)
            try {
                // 1. 本地照片压缩转 base64
                val localBase64 = localPhotos.mapNotNull { uri ->
                    compressImage(appContext, uri)?.let {
                        Base64.encodeToString(it, Base64.NO_WRAP)
                    }
                }

                // 2. 服务器照片下载转 base64
                val serverBase64 = serverPhotos.mapNotNull { photo ->
                    val photoUrl = "${NextApiService.BASE_URL}/api/uploads/${photo.storagePath?.removePrefix("/data/uploads/") ?: photo.filename}"
                    try {
                        nextApiService.downloadPhotoBytes(photoUrl).getOrNull()?.let {
                            Base64.encodeToString(it, Base64.NO_WRAP)
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "[Expense] 下载服务器照片失败 photo=%s", photo.id)
                        null
                    }
                }

                val allBase64 = localBase64 + serverBase64
                if (allBase64.isEmpty() && memoText.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isDialogAnalyzing = false,
                        dialogError = "无法读取照片"
                    )
                    return@launch
                }
                val preview = receiptAnalyzer.analyze(allBase64, memoText)
                _uiState.value = _uiState.value.copy(
                    isDialogAnalyzing = false,
                    dialogPreview = preview,
                    dialogPreviewVersion = _uiState.value.dialogPreviewVersion + 1
                )
                Timber.d("[Expense] Dialog分析成功 merchant=%s amount=%.2f images=%d", preview.merchant, preview.totalAmount, allBase64.size)
            } catch (e: Exception) {
                Timber.e(e, "[Expense] Dialog分析失败")
                val msg = if (e.message?.contains("timeout", ignoreCase = true) == true) {
                    "分析超时，请手动输入"
                } else {
                    "分析失败：${e.message}"
                }
                _uiState.value = _uiState.value.copy(isDialogAnalyzing = false, dialogError = msg)
            }
        }
    }

    fun clearDialogState() {
        _uiState.value = _uiState.value.copy(
            editingExpenseId = null,
            editingEntry = null,
            editingPhotos = emptyList(),
            isEditLoading = false,
            isEditSaving = false,
            isEditDeleting = false,
            isEditUploadingPhoto = false,
            dialogPhotos = emptyList(),
            isDialogAnalyzing = false,
            dialogPreview = null,
            dialogError = null
        )
    }

    /** Open edit dialog for existing expense */
    fun openEditDialog(expenseId: String) {
        _uiState.value = _uiState.value.copy(
            editingExpenseId = expenseId,
            isEditLoading = true
        )
        viewModelScope.launch {
            val result = nextApiService.getExpenseDetail(expenseId)
            result.fold(
                onSuccess = { detail ->
                    // Photos may come from top-level response or nested in entry
                    val photos = detail.photos.ifEmpty { detail.entry?.photos.orEmpty() }
                    Timber.d("[Expense] 加载详情 id=%s photos=%d (top=%d, entry=%d)",
                        expenseId, photos.size, detail.photos.size, detail.entry?.photos?.size ?: 0)
                    _uiState.value = _uiState.value.copy(
                        isEditLoading = false,
                        editingEntry = detail.entry,
                        editingPhotos = photos
                    )
                },
                onFailure = { e ->
                    Timber.e(e, "[Expense] 加载详情失败 id=%s", expenseId)
                    _uiState.value = _uiState.value.copy(
                        isEditLoading = false,
                        dialogError = "加载失败：${e.message}"
                    )
                }
            )
        }
    }

    /** Open new expense dialog */
    fun openNewDialog() {
        _uiState.value = _uiState.value.copy(editingExpenseId = "new")
    }

    /** Save changes to existing expense */
    fun saveEditChanges(
        amount: Double, notes: String, tags: List<String>?,
        currency: String, date: String?, time: String?,
        description: String?, items: List<NextExpenseItemRequest>? = null
    ) {
        val entryId = _uiState.value.editingEntry?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isEditSaving = true)
            // notes already includes time+description metadata from the caller
            val request = com.ergou.app.data.remote.dto.NextExpenseUpdateRequest(
                amount = amount,
                date = date,
                notes = notes,
                tags = tags,
                currency = currency
            )
            val result = nextApiService.updateExpense(entryId, request)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isEditSaving = false)
                    clearDialogState()
                    navigateToDate(date)
                },
                onFailure = { e ->
                    Timber.e(e, "[Expense] 保存失败 id=%s", entryId)
                    _uiState.value = _uiState.value.copy(
                        isEditSaving = false,
                        dialogError = "保存失败：${e.message}"
                    )
                }
            )
        }
    }

    /** Delete expense from edit dialog */
    fun deleteEditExpense() {
        val entry = _uiState.value.editingEntry ?: return
        val editingPhotos = _uiState.value.editingPhotos
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isEditDeleting = true)

            // Cache photos from edit dialog (already have metadata)
            val cachedPaths = if (editingPhotos.isNotEmpty()) {
                cachePhotosFromMetadata(entry.id, editingPhotos)
            } else emptyList()

            val result = nextApiService.deleteExpense(entry.id)
            result.fold(
                onSuccess = {
                    addToRecycleBin(entry, cachedPaths)
                    _uiState.value = _uiState.value.copy(isEditDeleting = false)
                    clearDialogState()
                    refreshAll()
                },
                onFailure = { e ->
                    Timber.e(e, "[Expense] 删除失败 id=%s", entry.id)
                    _uiState.value = _uiState.value.copy(
                        isEditDeleting = false,
                        dialogError = "删除失败：${e.message}"
                    )
                }
            )
        }
    }

    /** Upload photo for editing expense */
    fun uploadEditPhoto(uri: Uri) {
        val entryId = _uiState.value.editingEntry?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isEditUploadingPhoto = true)
            try {
                val compressed = compressImage(appContext, uri)
                if (compressed == null) {
                    _uiState.value = _uiState.value.copy(isEditUploadingPhoto = false, dialogError = "无法读取照片")
                    return@launch
                }
                val result = nextApiService.uploadExpensePhotoBytes(entryId, compressed)
                result.fold(
                    onSuccess = { photo ->
                        _uiState.value = _uiState.value.copy(
                            isEditUploadingPhoto = false,
                            editingPhotos = _uiState.value.editingPhotos + photo
                        )
                    },
                    onFailure = { e ->
                        Timber.e(e, "[Expense] 上传照片失败")
                        _uiState.value = _uiState.value.copy(isEditUploadingPhoto = false, dialogError = "上传失败：${e.message}")
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "[Expense] 压缩/上传照片异常")
                _uiState.value = _uiState.value.copy(isEditUploadingPhoto = false, dialogError = "上传失败：${e.message}")
            }
        }
    }

    /** Delete photo from editing expense */
    fun deleteEditPhoto(photoId: String) {
        viewModelScope.launch {
            val result = nextApiService.deleteExpensePhoto(photoId)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        editingPhotos = _uiState.value.editingPhotos.filter { it.id != photoId }
                    )
                },
                onFailure = { e ->
                    Timber.e(e, "[Expense] 删除照片失败")
                    _uiState.value = _uiState.value.copy(dialogError = "删除照片失败：${e.message}")
                }
            )
        }
    }

    /** Build notes string combining title and description */
    private fun buildNotes(notes: String, description: String?): String {
        return if (!description.isNullOrBlank() && description != notes) {
            "$notes\n---\n$description"
        } else {
            notes
        }
    }

    // --- Batch scan ---

    fun toggleBatchScan() {
        val show = !_uiState.value.showBatchScan
        _uiState.value = _uiState.value.copy(
            showBatchScan = show,
            selectedPhotos = if (show) emptyList() else _uiState.value.selectedPhotos,
            scanResults = if (show) emptyList() else _uiState.value.scanResults,
            scanProgress = null,
            batchMergeMode = false
        )
    }

    fun setBatchMergeMode(merge: Boolean) {
        _uiState.value = _uiState.value.copy(batchMergeMode = merge)
    }

    /**
     * Transfer photos from dialog to batch scan (for "不同单据" mode).
     * Closes dialog state, opens batch scan, and auto-starts individual analysis.
     */
    fun startBatchFromDialog() {
        val photos = _uiState.value.dialogPhotos
        _uiState.value = _uiState.value.copy(
            // Clear dialog state
            dialogPhotos = emptyList(),
            dialogPreview = null,
            dialogError = null,
            isDialogAnalyzing = false,
            // Open batch scan with photos
            showBatchScan = true,
            selectedPhotos = photos,
            batchMergeMode = false,
            scanResults = emptyList()
        )
        analyzePhotos(appContext)
    }

    fun addPhotos(uris: List<Uri>) {
        _uiState.value = _uiState.value.copy(
            selectedPhotos = _uiState.value.selectedPhotos + uris
        )
        Timber.d("[Expense] 添加照片 count=%d total=%d", uris.size, _uiState.value.selectedPhotos.size)
    }

    fun removePhoto(index: Int) {
        val photos = _uiState.value.selectedPhotos.toMutableList()
        if (index in photos.indices) {
            photos.removeAt(index)
            _uiState.value = _uiState.value.copy(selectedPhotos = photos)
        }
    }

    fun analyzePhotos(context: Context) {
        val photos = _uiState.value.selectedPhotos
        if (photos.isEmpty()) return

        if (_uiState.value.batchMergeMode && photos.size >= 2) {
            analyzePhotosMerged(context)
            return
        }

        viewModelScope.launch {
            val results = mutableListOf<ScanResultItem>()
            val total = photos.size
            _uiState.value = _uiState.value.copy(scanProgress = 0 to total, scanResults = emptyList())

            photos.forEachIndexed { index, uri ->
                _uiState.value = _uiState.value.copy(scanProgress = (index + 1) to total)
                try {
                    val bytes = compressImage(context, uri)
                    if (bytes == null) {
                        Timber.w("[Expense] 无法读取照片 index=%d", index)
                        _uiState.value = _uiState.value.copy(error = "第 ${index + 1} 张照片无法读取")
                        return@forEachIndexed
                    }
                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    Timber.d("[Expense] 分析照片 index=%d compressedSize=%dKB", index, bytes.size / 1024)
                    val preview = receiptAnalyzer.analyze(listOf(base64))
                    results.add(ScanResultItem(preview = preview, photoUri = uri))
                    Timber.d("[Expense] 分析成功 index=%d merchant=%s", index, preview.merchant)
                } catch (e: Exception) {
                    Timber.e(e, "[Expense] 处理照片异常 index=%d", index)
                }
            }

            _uiState.value = _uiState.value.copy(scanProgress = null, scanResults = results)
        }
    }

    fun updateScanResult(id: String, amount: Double?, notes: String?, tags: List<String>?, currency: String?, date: String? = null) {
        val updated = _uiState.value.scanResults.map {
            if (it.id == id) it.copy(editedAmount = amount, editedNotes = notes, editedTags = tags, editedCurrency = currency, editedDate = date) else it
        }
        _uiState.value = _uiState.value.copy(scanResults = updated)
    }

    fun removeScanResult(id: String) {
        _uiState.value = _uiState.value.copy(
            scanResults = _uiState.value.scanResults.filter { it.id != id }
        )
    }

    fun saveAllResults(context: Context) {
        val results = _uiState.value.scanResults
        if (results.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            var successCount = 0
            var failCount = 0

            for (item in results) {
                try {
                    val request = NextExpenseCreateRequest(
                        amount = item.effectiveAmount,
                        notes = item.effectiveNotes.ifBlank { null },
                        tags = item.effectiveTags,
                        currency = item.effectiveCurrency,
                        date = item.effectiveDate,
                        items = item.preview.items.map {
                            NextExpenseItemRequest(
                                name = it.name,
                                quantity = it.quantity,
                                unitPrice = it.unitPrice,
                                amount = it.amount
                            )
                        }.ifEmpty { null }
                    )
                    val createResult = nextApiService.createExpense(request)
                    createResult.fold(
                        onSuccess = { entry ->
                            successCount++
                            for (photoUri in item.photosToUpload) {
                                try {
                                    val compressed = compressImage(context, photoUri)
                                    if (compressed == null) {
                                        Timber.w("[Expense] 照片压缩失败，跳过 entryId=%s", entry.id)
                                        continue
                                    }
                                    nextApiService.uploadExpensePhotoBytes(entry.id, compressed)
                                } catch (e: Exception) {
                                    Timber.w(e, "[Expense] 照片上传失败 entryId=%s", entry.id)
                                }
                            }
                        },
                        onFailure = { e ->
                            failCount++
                            Timber.e(e, "[Expense] 保存失败 merchant=%s", item.effectiveNotes)
                        }
                    )
                } catch (e: Exception) {
                    failCount++
                    Timber.e(e, "[Expense] 保存异常")
                }
            }

            _uiState.value = _uiState.value.copy(
                isSaving = false,
                showBatchScan = false,
                selectedPhotos = emptyList(),
                scanResults = emptyList(),
                error = if (failCount > 0) "保存 $successCount 条成功，$failCount 条失败" else null
            )
            refreshAll()
            Timber.d("[Expense] 批量保存完成 success=%d fail=%d", successCount, failCount)
        }
    }

    /**
     * 合并模式：将所有照片作为同一张单据的多张图片发送给 API 分析。
     */
    private fun analyzePhotosMerged(context: Context) {
        val photos = _uiState.value.selectedPhotos
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(scanProgress = 1 to 1, scanResults = emptyList())
            try {
                val base64Images = photos.mapNotNull { uri ->
                    compressImage(context, uri)?.let {
                        Base64.encodeToString(it, Base64.NO_WRAP)
                    }
                }
                if (base64Images.isEmpty()) {
                    _uiState.value = _uiState.value.copy(scanProgress = null, error = "无法读取照片")
                    return@launch
                }
                Timber.d("[Expense] 合并分析 %d 张照片", base64Images.size)
                val preview = receiptAnalyzer.analyze(base64Images)
                val scanResult = ScanResultItem(
                    preview = preview,
                    photoUri = photos.first(),
                    allPhotoUris = photos
                )
                _uiState.value = _uiState.value.copy(scanProgress = null, scanResults = listOf(scanResult))
                Timber.d("[Expense] 合并分析成功 merchant=%s amount=%.2f", preview.merchant, preview.totalAmount)
            } catch (e: Exception) {
                Timber.e(e, "[Expense] 合并分析异常")
                _uiState.value = _uiState.value.copy(scanProgress = null, error = "照片处理失败")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun getPeriodDateRange(period: ExpensePeriod): Pair<String?, String?> {
        val today = LocalDate.now()
        val offset = _uiState.value.periodOffset
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        // Backend uses exclusive `to` — add 1 day to include the end date
        return when (period) {
            ExpensePeriod.TODAY -> {
                val day = today.plusDays(offset.toLong())
                day.format(fmt) to day.plusDays(1).format(fmt)
            }
            ExpensePeriod.WEEK -> {
                val weekStart = today.with(DayOfWeek.MONDAY).plusWeeks(offset.toLong())
                val weekEnd = weekStart.plusDays(7) // Mon to next Mon (exclusive)
                weekStart.format(fmt) to weekEnd.format(fmt)
            }
            ExpensePeriod.MONTH -> {
                val month = today.plusMonths(offset.toLong())
                val monthStart = month.withDayOfMonth(1)
                val monthEnd = monthStart.plusMonths(1) // 1st of next month (exclusive)
                monthStart.format(fmt) to monthEnd.format(fmt)
            }
        }
    }

    /**
     * Compress and resize image to reduce upload size.
     * Max dimension: 1920px, JPEG quality: 80%.
     */
    private fun compressImage(context: Context, uri: Uri, maxDim: Int = MAX_IMAGE_DIMENSION, quality: Int = JPEG_QUALITY): ByteArray? {
        // First pass: get dimensions
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val opened = context.contentResolver.openInputStream(uri)
        if (opened == null) {
            Timber.w("[Expense] 无法打开图片URI uri=%s", uri)
            return null
        }
        opened.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }

        val width = options.outWidth
        val height = options.outHeight
        if (width <= 0 || height <= 0) return null

        // Calculate sample size for downscaling
        var sampleSize = 1
        val maxSide = maxOf(width, height)
        if (maxSide > maxDim) {
            sampleSize = maxSide / maxDim
        }

        // Second pass: decode with sample size
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: return null

        // Compress to JPEG
        val bw = bitmap.width
        val bh = bitmap.height
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
        bitmap.recycle()

        val bytes = output.toByteArray()
        Timber.d("[Expense] 压缩图片 %dx%d → %dx%d size=%dKB", width, height, bw, bh, bytes.size / 1024)
        return bytes
    }

}
