package com.ergou.app.ui.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextTrip
import com.ergou.app.data.remote.dto.NextTripCreateRequest
import com.ergou.app.data.remote.dto.NextTripItem
import com.ergou.app.data.remote.dto.NextTripItemCreateRequest
import com.ergou.app.data.remote.dto.NextTripItemUpdateRequest
import com.ergou.app.data.remote.dto.NextTripUpdateRequest
import com.ergou.app.util.NextAuthProvider
import android.net.Uri
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate

enum class TripStatus(val label: String) {
    ONGOING("进行中"),
    PLANNED("计划中"),
    COMPLETED("已结束")
}

data class TripItemDayGroup(
    val date: String,
    val displayDate: String,
    val items: List<NextTripItem>,
    val dayTotal: Double
)

data class TypeStat(
    val type: String,
    val label: String,
    val amount: Double,
    val ratio: Float
)

data class TripUiState(
    val trips: List<NextTrip> = emptyList(),
    val selectedTrip: NextTrip? = null,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val pendingDeleteTripIds: Set<String> = emptySet(),
    val pendingDeleteItemIds: Set<String> = emptySet(),
    val confirmDeleteTrip: NextTrip? = null,
    val confirmDeleteItem: NextTripItem? = null,
    val uploadingItemIds: Set<String> = emptySet()
) {
    val sortedTrips: List<Pair<NextTrip, TripStatus>>
        get() {
            val withStatus = trips.filter { it.id !in pendingDeleteTripIds }.map { it to getTripStatus(it) }
            return withStatus.sortedWith(
                compareBy<Pair<NextTrip, TripStatus>> {
                    when (it.second) {
                        TripStatus.ONGOING -> 0
                        TripStatus.PLANNED -> 1
                        TripStatus.COMPLETED -> 2
                    }
                }.thenByDescending { it.first.dateFrom ?: "" }
            )
        }

    val detailDayGroups: List<TripItemDayGroup>
        get() {
            val items = selectedTrip?.items?.filter { it.id !in pendingDeleteItemIds } ?: return emptyList()
            val grouped = items
                .groupBy { it.date ?: "未知日期" }
                .toSortedMap()
            return grouped.map { (date, dayItems) ->
                TripItemDayGroup(
                    date = date,
                    displayDate = formatTripDate(date),
                    items = dayItems.sortedBy { it.sortOrder },
                    dayTotal = dayItems.sumOf { it.amount }
                )
            }
        }

    val typeStats: List<TypeStat>
        get() {
            val items = selectedTrip?.items ?: return emptyList()
            val total = items.sumOf { it.amount }
            if (total == 0.0) return emptyList()
            return items.groupBy { it.type }
                .map { (type, typeItems) ->
                    val amount = typeItems.sumOf { it.amount }
                    TypeStat(
                        type = type,
                        label = typeToLabel(type),
                        amount = amount,
                        ratio = (amount / total).toFloat()
                    )
                }
                .sortedByDescending { it.amount }
        }

    val reimburseApproved: Int
        get() = selectedTrip?.reimburseSummary?.approved ?: 0

    val reimburseTotal: Int
        get() = selectedTrip?.reimburseSummary?.total ?: 0

    val reimburseRatio: Float
        get() = if (reimburseTotal > 0) (reimburseApproved.toFloat() / reimburseTotal).coerceIn(0f, 1f) else 0f
}

fun getTripStatus(trip: NextTrip): TripStatus {
    val today = LocalDate.now()
    return try {
        val from = trip.dateFrom?.let { LocalDate.parse(it) }
        val to = trip.dateTo?.let { LocalDate.parse(it) }
        when {
            from != null && to != null && !today.isBefore(from) && !today.isAfter(to) -> TripStatus.ONGOING
            to != null && today.isAfter(to) -> TripStatus.COMPLETED
            from != null && today.isBefore(from) -> TripStatus.PLANNED
            from != null && to == null && !today.isBefore(from) -> TripStatus.ONGOING
            else -> TripStatus.PLANNED
        }
    } catch (_: Exception) {
        TripStatus.PLANNED
    }
}

val ITEM_TYPES = listOf(
    "flight" to "机票",
    "train" to "火车",
    "hotel" to "酒店",
    "taxi" to "打车",
    "meal" to "餐饮",
    "meeting" to "会议",
    "telecom" to "通讯",
    "misc" to "其他"
)

val REIMBURSE_STATUSES = listOf(
    "pending" to "待提交",
    "submitted" to "已提交",
    "approved" to "已批准",
    "rejected" to "已拒绝",
    "na" to "无需报销"
)

fun typeToLabel(type: String): String = when (type) {
    "flight" -> "机票"
    "train" -> "火车"
    "hotel" -> "酒店"
    "taxi" -> "打车"
    "meal" -> "餐饮"
    "meeting" -> "会议"
    "telecom" -> "通讯"
    else -> "其他"
}

fun typeToEmoji(type: String): String = when (type) {
    "flight" -> "✈\uFE0F"
    "train" -> "\uD83D\uDE84"
    "hotel" -> "\uD83C\uDFE8"
    "taxi" -> "\uD83D\uDE95"
    "meal" -> "\uD83C\uDF5D\uFE0F"
    "meeting" -> "\uD83D\uDCCB"
    "telecom" -> "\uD83D\uDCF1"
    else -> "\uD83D\uDCE6"
}

fun reimburseStatusLabel(status: String): String = when (status) {
    "pending" -> "待提交"
    "submitted" -> "已提交"
    "approved" -> "已批准"
    "rejected" -> "已拒绝"
    "na" -> "无需报销"
    else -> status
}

private fun formatTripDate(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr)
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val dayOfWeek = when (date.dayOfWeek.value) {
            1 -> "周一"; 2 -> "周二"; 3 -> "周三"; 4 -> "周四"
            5 -> "周五"; 6 -> "周六"; 7 -> "周日"; else -> ""
        }
        when (date) {
            today -> "今天 $dayOfWeek"
            yesterday -> "昨天 $dayOfWeek"
            else -> "${date.monthValue}月${date.dayOfMonth}日 $dayOfWeek"
        }
    } catch (_: Exception) {
        dateStr
    }
}

class TripViewModel(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripUiState())
    val uiState: StateFlow<TripUiState> = _uiState

    private val _snackbarEvent = MutableSharedFlow<SnackbarEvent>()
    val snackbarEvent: SharedFlow<SnackbarEvent> = _snackbarEvent

    init {
        viewModelScope.launch {
            authProvider.isLoggedIn.collect { loggedIn ->
                _uiState.value = _uiState.value.copy(isLoggedIn = loggedIn)
                if (loggedIn) {
                    refreshTrips()
                } else {
                    _uiState.value = _uiState.value.copy(trips = emptyList(), selectedTrip = null)
                }
            }
        }
    }

    fun refreshTrips() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = nextApiService.getTrips()
            result.fold(
                onSuccess = { trips ->
                    _uiState.value = _uiState.value.copy(trips = trips, isLoading = false)
                },
                onFailure = { e ->
                    Timber.e(e, "[Trip] 刷新出差失败")
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "加载失败")
                }
            )
        }
    }

    fun selectTrip(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = nextApiService.getTripById(id)
            result.fold(
                onSuccess = { trip ->
                    _uiState.value = _uiState.value.copy(selectedTrip = trip, isLoading = false)
                },
                onFailure = { e ->
                    Timber.e(e, "[Trip] 获取出差详情失败 id=%s", id)
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "获取详情失败：${e.message}")
                }
            )
        }
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedTrip = null)
        refreshTrips()
    }

    fun addTrip(title: String, destination: String?, dateFrom: String?, dateTo: String?, purpose: String?, currency: String? = null) {
        viewModelScope.launch {
            val result = nextApiService.createTrip(
                NextTripCreateRequest(
                    title = title,
                    destination = destination,
                    dateFrom = dateFrom,
                    dateTo = dateTo,
                    purpose = purpose,
                    currency = currency
                )
            )
            result.fold(
                onSuccess = { trip ->
                    Timber.d("[Trip] 创建出差成功 id=%s title=%s", trip.id, trip.title)
                    refreshTrips()
                },
                onFailure = { e ->
                    Timber.e(e, "[Trip] 创建出差失败")
                    _uiState.value = _uiState.value.copy(error = "创建失败：${e.message}")
                }
            )
        }
    }

    fun updateTrip(id: String, title: String?, destination: String?, dateFrom: String?, dateTo: String?, purpose: String?, notes: String?, currency: String?) {
        viewModelScope.launch {
            val result = nextApiService.updateTrip(
                id,
                NextTripUpdateRequest(
                    title = title,
                    destination = destination,
                    dateFrom = dateFrom,
                    dateTo = dateTo,
                    purpose = purpose,
                    notes = notes,
                    currency = currency
                )
            )
            result.fold(
                onSuccess = {
                    Timber.d("[Trip] 更新出差成功 id=%s", id)
                    selectTrip(id)
                },
                onFailure = { e ->
                    Timber.e(e, "[Trip] 更新出差失败")
                    _uiState.value = _uiState.value.copy(error = "更新失败：${e.message}")
                }
            )
        }
    }

    fun addItem(tripId: String, type: String, description: String, amount: Double, date: String?, reimburseStatus: String? = null, notes: String? = null, currency: String? = null) {
        viewModelScope.launch {
            val result = nextApiService.createTripItem(
                tripId,
                NextTripItemCreateRequest(
                    type = type,
                    description = description,
                    amount = amount,
                    date = date,
                    currency = currency,
                    reimburseStatus = reimburseStatus,
                    notes = notes
                )
            )
            result.fold(
                onSuccess = {
                    Timber.d("[Trip] 添加费用成功 tripId=%s type=%s", tripId, type)
                    selectTrip(tripId)
                },
                onFailure = { e ->
                    Timber.e(e, "[Trip] 添加费用失败")
                    _uiState.value = _uiState.value.copy(error = "添加费用失败：${e.message}")
                }
            )
        }
    }

    fun updateItem(tripId: String, itemId: String, type: String?, description: String?, amount: Double?, date: String?, reimburseStatus: String?, notes: String?) {
        viewModelScope.launch {
            val result = nextApiService.updateTripItem(
                tripId,
                itemId,
                NextTripItemUpdateRequest(
                    type = type,
                    description = description,
                    amount = amount,
                    date = date,
                    reimburseStatus = reimburseStatus,
                    notes = notes
                )
            )
            result.fold(
                onSuccess = {
                    Timber.d("[Trip] 更新费用成功 itemId=%s", itemId)
                    selectTrip(tripId)
                },
                onFailure = { e ->
                    Timber.e(e, "[Trip] 更新费用失败")
                    _uiState.value = _uiState.value.copy(error = "更新费用失败：${e.message}")
                }
            )
        }
    }

    fun deleteItem(tripId: String, itemId: String) {
        viewModelScope.launch {
            val result = nextApiService.deleteTripItem(tripId, itemId)
            result.fold(
                onSuccess = {
                    Timber.d("[Trip] 删除费用成功 itemId=%s", itemId)
                    selectTrip(tripId)
                },
                onFailure = { e ->
                    Timber.e(e, "[Trip] 删除费用失败")
                    _uiState.value = _uiState.value.copy(error = "删除费用失败：${e.message}")
                }
            )
        }
    }

    fun deleteTrip(id: String) {
        viewModelScope.launch {
            val result = nextApiService.deleteTrip(id)
            result.fold(
                onSuccess = {
                    Timber.d("[Trip] 删除出差成功 id=%s", id)
                    _uiState.value = _uiState.value.copy(selectedTrip = null)
                    refreshTrips()
                },
                onFailure = { e ->
                    Timber.e(e, "[Trip] 删除出差失败")
                    _uiState.value = _uiState.value.copy(error = "删除失败：${e.message}")
                }
            )
        }
    }

    // ── Confirm dialog state ──

    fun showConfirmDeleteTrip(trip: NextTrip) {
        _uiState.value = _uiState.value.copy(confirmDeleteTrip = trip)
    }

    fun dismissConfirmDeleteTrip() {
        _uiState.value = _uiState.value.copy(confirmDeleteTrip = null)
    }

    fun showConfirmDeleteItem(item: NextTripItem) {
        _uiState.value = _uiState.value.copy(confirmDeleteItem = item)
    }

    fun dismissConfirmDeleteItem() {
        _uiState.value = _uiState.value.copy(confirmDeleteItem = null)
    }

    // ── Delayed delete: Trip ──

    fun confirmDeleteTrip(id: String) {
        _uiState.value = _uiState.value.copy(
            pendingDeleteTripIds = _uiState.value.pendingDeleteTripIds + id,
            confirmDeleteTrip = null
        )
        viewModelScope.launch {
            _snackbarEvent.emit(SnackbarEvent(
                message = "行程已删除",
                actionLabel = "撤销",
                onAction = { undoDeleteTrip(id) },
                onDismiss = { executeDeleteTrip(id) }
            ))
        }
    }

    private fun undoDeleteTrip(id: String) {
        _uiState.value = _uiState.value.copy(
            pendingDeleteTripIds = _uiState.value.pendingDeleteTripIds - id
        )
        Timber.d("[Trip] 撤销删除 tripId=%s", id)
    }

    private fun executeDeleteTrip(id: String) {
        if (id !in _uiState.value.pendingDeleteTripIds) return
        _uiState.value = _uiState.value.copy(
            pendingDeleteTripIds = _uiState.value.pendingDeleteTripIds - id
        )
        viewModelScope.launch {
            val result = nextApiService.deleteTrip(id)
            result.fold(
                onSuccess = {
                    Timber.d("[Trip] 延迟删除成功 tripId=%s", id)
                    if (_uiState.value.selectedTrip?.id == id) {
                        _uiState.value = _uiState.value.copy(selectedTrip = null)
                    }
                    refreshTrips()
                },
                onFailure = { e ->
                    Timber.e(e, "[Trip] 延迟删除失败 tripId=%s", id)
                    _uiState.value = _uiState.value.copy(error = "删除失败：${e.message}")
                    refreshTrips()
                }
            )
        }
    }

    // ── Delayed delete: Item ──

    fun confirmDeleteItem(tripId: String, itemId: String) {
        _uiState.value = _uiState.value.copy(
            pendingDeleteItemIds = _uiState.value.pendingDeleteItemIds + itemId,
            confirmDeleteItem = null
        )
        viewModelScope.launch {
            _snackbarEvent.emit(SnackbarEvent(
                message = "费用项已删除",
                actionLabel = "撤销",
                onAction = { undoDeleteItem(itemId) },
                onDismiss = { executeDeleteItem(tripId, itemId) }
            ))
        }
    }

    private fun undoDeleteItem(itemId: String) {
        _uiState.value = _uiState.value.copy(
            pendingDeleteItemIds = _uiState.value.pendingDeleteItemIds - itemId
        )
        Timber.d("[Trip] 撤销删除费用项 itemId=%s", itemId)
    }

    private fun executeDeleteItem(tripId: String, itemId: String) {
        if (itemId !in _uiState.value.pendingDeleteItemIds) return
        _uiState.value = _uiState.value.copy(
            pendingDeleteItemIds = _uiState.value.pendingDeleteItemIds - itemId
        )
        viewModelScope.launch {
            val result = nextApiService.deleteTripItem(tripId, itemId)
            result.fold(
                onSuccess = {
                    Timber.d("[Trip] 延迟删除费用项成功 itemId=%s", itemId)
                    selectTrip(tripId)
                },
                onFailure = { e ->
                    Timber.e(e, "[Trip] 延迟删除费用项失败 itemId=%s", itemId)
                    _uiState.value = _uiState.value.copy(error = "删除费用失败：${e.message}")
                    selectTrip(tripId)
                }
            )
        }
    }

    // ── Photos ──

    fun uploadItemPhoto(itemId: String, uri: Uri, context: android.content.Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                uploadingItemIds = _uiState.value.uploadingItemIds + itemId
            )
            val result = nextApiService.uploadTripItemPhoto(itemId, uri, context)
            _uiState.value = _uiState.value.copy(
                uploadingItemIds = _uiState.value.uploadingItemIds - itemId
            )
            result.fold(
                onSuccess = {
                    Timber.d("[Trip] 照片上传成功 itemId=%s", itemId)
                    _uiState.value.selectedTrip?.let { selectTrip(it.id) }
                },
                onFailure = { e ->
                    Timber.e(e, "[Trip] 照片上传失败 itemId=%s", itemId)
                    _uiState.value = _uiState.value.copy(error = "上传失败：${e.message}")
                }
            )
        }
    }

    fun deleteItemPhoto(photoId: String) {
        viewModelScope.launch {
            val result = nextApiService.deleteTripItemPhoto(photoId)
            result.fold(
                onSuccess = {
                    Timber.d("[Trip] 照片删除成功 photoId=%s", photoId)
                    _uiState.value.selectedTrip?.let { selectTrip(it.id) }
                },
                onFailure = { e ->
                    Timber.e(e, "[Trip] 照片删除失败 photoId=%s", photoId)
                    _uiState.value = _uiState.value.copy(error = "删除照片失败：${e.message}")
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class SnackbarEvent(
    val message: String,
    val actionLabel: String? = null,
    val onAction: () -> Unit = {},
    val onDismiss: () -> Unit = {}
)
