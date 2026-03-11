package com.ergou.app.ui.expense

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextExpenseCreateRequest
import com.ergou.app.data.remote.dto.NextExpenseEntry
import com.ergou.app.data.remote.dto.NextExpenseItemRequest
import com.ergou.app.data.remote.dto.NextExpensePhoto
import com.ergou.app.data.remote.dto.NextExpenseUpdateRequest
import com.ergou.app.data.remote.dto.NextParsePreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber

data class ExpenseDetailUiState(
    val isLoading: Boolean = false,
    val entry: NextExpenseEntry? = null,
    val photos: List<NextExpensePhoto> = emptyList(),
    // Edit fields
    val amount: String = "",
    val notes: TextFieldValue = TextFieldValue(),
    val date: String? = null,
    val currency: String = "CAD",
    val tags: Set<String> = emptySet(),
    val customTagInput: TextFieldValue = TextFieldValue(),
    // Save/delete state
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val error: String? = null,
    // Parse preview
    val parsePreview: NextParsePreview? = null,
    val isParsing: Boolean = false,
    val parseError: String? = null,
    val scanPhotoUri: Uri? = null
)

sealed class ExpenseDetailEvent {
    data object NavigateBack : ExpenseDetailEvent()
    data class ShowMessage(val message: String) : ExpenseDetailEvent()
}

class ExpenseDetailViewModel(
    private val expenseId: String?,
    private val nextApiService: NextApiService,
    private val appContext: Context,
    private val receiptAnalyzer: com.ergou.app.data.repository.ReceiptAnalyzer
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseDetailUiState())
    val uiState: StateFlow<ExpenseDetailUiState> = _uiState

    private val _events = Channel<ExpenseDetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        if (expenseId != null) {
            loadDetail(expenseId)
        }
    }

    fun loadDetail(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = nextApiService.getExpenseDetail(id)
            result.fold(
                onSuccess = { detail ->
                    val entry = detail.entry!!
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        entry = entry,
                        amount = entry.amount.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() },
                        notes = TextFieldValue(entry.notes ?: ""),
                        date = entry.date,
                        currency = entry.currency,
                        tags = entry.tags?.toSet() ?: emptySet(),
                        photos = detail.photos
                    )
                },
                onFailure = { e ->
                    Timber.e(e, "[ExpenseDetail] 加载详情失败")
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "加载失败：${e.message}")
                }
            )
        }
    }

    fun updateAmount(value: String) {
        _uiState.value = _uiState.value.copy(amount = value)
    }

    fun updateNotes(value: TextFieldValue) {
        _uiState.value = _uiState.value.copy(notes = value)
    }

    fun updateDate(value: String?) {
        _uiState.value = _uiState.value.copy(date = value)
    }

    fun updateCurrency(value: String) {
        _uiState.value = _uiState.value.copy(currency = value)
    }

    fun toggleTag(tag: String) {
        val current = _uiState.value.tags
        _uiState.value = _uiState.value.copy(
            tags = if (tag in current) current - tag else current + tag
        )
    }

    fun updateCustomTagInput(value: TextFieldValue) {
        _uiState.value = _uiState.value.copy(customTagInput = value)
    }

    fun saveChanges() {
        val state = _uiState.value
        val id = state.entry?.id ?: return
        val parsedAmount = state.amount.toDoubleOrNull() ?: return

        val customTags = state.customTagInput.text.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val allTags = state.tags.toList() + customTags

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            val request = NextExpenseUpdateRequest(
                amount = parsedAmount,
                date = state.date,
                notes = state.notes.text,
                tags = allTags,
                currency = state.currency
            )
            val result = nextApiService.updateExpense(id, request)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    _events.send(ExpenseDetailEvent.NavigateBack)
                },
                onFailure = { e ->
                    Timber.e(e, "[ExpenseDetail] 保存失败")
                    _uiState.value = _uiState.value.copy(isSaving = false, error = "保存失败：${e.message}")
                }
            )
        }
    }

    fun deleteExpense() {
        val id = _uiState.value.entry?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, error = null)
            val result = nextApiService.deleteExpense(id)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isDeleting = false)
                    _events.send(ExpenseDetailEvent.NavigateBack)
                },
                onFailure = { e ->
                    Timber.e(e, "[ExpenseDetail] 删除失败")
                    _uiState.value = _uiState.value.copy(isDeleting = false, error = "删除失败：${e.message}")
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null, parseError = null)
    }

    // ── Photo management ──

    fun uploadPhoto(uri: Uri) {
        val id = _uiState.value.entry?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingPhoto = true, error = null)
            try {
                val compressed = compressImage(uri)
                if (compressed == null) {
                    _uiState.value = _uiState.value.copy(isUploadingPhoto = false, error = "无法读取照片")
                    return@launch
                }
                val result = nextApiService.uploadExpensePhotoBytes(id, compressed)
                result.fold(
                    onSuccess = { photo ->
                        _uiState.value = _uiState.value.copy(
                            isUploadingPhoto = false,
                            photos = _uiState.value.photos + photo
                        )
                    },
                    onFailure = { e ->
                        Timber.e(e, "[ExpenseDetail] 上传照片失败")
                        _uiState.value = _uiState.value.copy(isUploadingPhoto = false, error = "上传失败：${e.message}")
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "[ExpenseDetail] 压缩/上传照片异常")
                _uiState.value = _uiState.value.copy(isUploadingPhoto = false, error = "上传失败：${e.message}")
            }
        }
    }

    /**
     * 压缩图片。如果 BitmapFactory 无法解码（非图片格式），回退为读取原始字节。
     */
    internal fun compressImage(uri: Uri, maxDim: Int = 1920, quality: Int = 80): ByteArray? {
        try {
            val options = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
            appContext.contentResolver.openInputStream(uri)?.use { stream ->
                android.graphics.BitmapFactory.decodeStream(stream, null, options)
            } ?: return null

            val width = options.outWidth
            val height = options.outHeight
            if (width > 0 && height > 0) {
                var sampleSize = 1
                val maxSide = maxOf(width, height)
                if (maxSide > maxDim) sampleSize = maxSide / maxDim

                val decodeOptions = android.graphics.BitmapFactory.Options().apply { inSampleSize = sampleSize }
                val bitmap = appContext.contentResolver.openInputStream(uri)?.use { stream ->
                    android.graphics.BitmapFactory.decodeStream(stream, null, decodeOptions)
                }
                if (bitmap != null) {
                    val output = java.io.ByteArrayOutputStream()
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, output)
                    bitmap.recycle()
                    return output.toByteArray()
                }
            }
        } catch (_: Exception) { /* fall through to raw read */ }

        // Fallback: read raw bytes (BitmapFactory unavailable or non-image)
        return appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
    }

    fun deletePhoto(photoId: String) {
        viewModelScope.launch {
            val result = nextApiService.deleteExpensePhoto(photoId)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        photos = _uiState.value.photos.filter { it.id != photoId }
                    )
                },
                onFailure = { e ->
                    Timber.e(e, "[ExpenseDetail] 删除照片失败")
                    _uiState.value = _uiState.value.copy(error = "删除照片失败：${e.message}")
                }
            )
        }
    }

    // ── AI Receipt Scan ──

    fun parseReceipt(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isParsing = true, parseError = null, scanPhotoUri = uri)

            // Check file size before reading to avoid OOM
            val fileSize = try {
                appContext.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
            } catch (_: Exception) { -1L }
            if (fileSize > 10 * 1024 * 1024) {
                _uiState.value = _uiState.value.copy(isParsing = false, parseError = "照片不能超过 10MB")
                return@launch
            }

            val bytes = appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes == null) {
                _uiState.value = _uiState.value.copy(isParsing = false, parseError = "无法读取图片")
                return@launch
            }

            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            try {
                val preview = receiptAnalyzer.analyze(listOf(base64))
                _uiState.value = _uiState.value.copy(
                    isParsing = false,
                    parsePreview = preview,
                    amount = preview.totalAmount.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() },
                    notes = TextFieldValue(preview.merchant),
                    date = preview.date,
                    currency = preview.currency,
                    tags = preview.tags?.toSet() ?: emptySet()
                )
            } catch (e: Exception) {
                Timber.e(e, "[ExpenseDetail] AI解析失败")
                val msg = if (e.message?.contains("timeout", ignoreCase = true) == true) {
                    "解析超时，请手动输入"
                } else {
                    "解析失败：${e.message}"
                }
                _uiState.value = _uiState.value.copy(isParsing = false, parseError = msg)
            }
        }
    }

    fun confirmParseAndSave() {
        val state = _uiState.value
        val preview = state.parsePreview ?: return
        val parsedAmount = state.amount.toDoubleOrNull() ?: return

        val customTags = state.customTagInput.text.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val allTags = state.tags.toList() + customTags

        val existingId = state.entry?.id

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)

            if (existingId != null) {
                // Update existing expense with AI-parsed fields
                val request = NextExpenseUpdateRequest(
                    amount = parsedAmount,
                    date = state.date,
                    notes = state.notes.text,
                    tags = allTags,
                    currency = state.currency
                )
                val result = nextApiService.updateExpense(existingId, request)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            parsePreview = null,
                            scanPhotoUri = null
                        )
                        _events.send(ExpenseDetailEvent.NavigateBack)
                    },
                    onFailure = { e ->
                        Timber.e(e, "[ExpenseDetail] 更新解析结果失败")
                        _uiState.value = _uiState.value.copy(isSaving = false, error = "保存失败：${e.message}")
                    }
                )
            } else {
                // Create new expense
                val items = preview.items.map { item ->
                    NextExpenseItemRequest(
                        name = item.name,
                        quantity = item.quantity,
                        unitPrice = item.unitPrice,
                        amount = item.amount,
                        specs = item.specs
                    )
                }.ifEmpty { null }

                val request = NextExpenseCreateRequest(
                    amount = parsedAmount,
                    date = state.date,
                    notes = state.notes.text.ifBlank { null },
                    tags = allTags.ifEmpty { null },
                    currency = state.currency,
                    items = items
                )
                val createResult = nextApiService.createExpense(request)
                createResult.fold(
                    onSuccess = { entry ->
                        // Upload the scanned photo for new expense (compressed)
                        val photoUri = state.scanPhotoUri
                        if (photoUri != null) {
                            val compressed = compressImage(photoUri)
                            val uploadResult = if (compressed != null) {
                                nextApiService.uploadExpensePhotoBytes(entry.id, compressed)
                            } else {
                                Result.failure(Exception("无法压缩照片"))
                            }
                            if (uploadResult.isFailure) {
                                _events.send(ExpenseDetailEvent.ShowMessage("记账已保存，但照片上传失败"))
                            }
                        }
                        _uiState.value = _uiState.value.copy(isSaving = false)
                        _events.send(ExpenseDetailEvent.NavigateBack)
                    },
                    onFailure = { e ->
                        Timber.e(e, "[ExpenseDetail] 保存解析结果失败")
                        _uiState.value = _uiState.value.copy(isSaving = false, error = "保存失败：${e.message}")
                    }
                )
            }
        }
    }

    fun cancelParse() {
        val entry = _uiState.value.entry
        if (entry != null) {
            // Restore original entry fields
            _uiState.value = _uiState.value.copy(
                parsePreview = null,
                parseError = null,
                scanPhotoUri = null,
                amount = entry.amount.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() },
                notes = TextFieldValue(entry.notes ?: ""),
                date = entry.date,
                currency = entry.currency,
                tags = entry.tags?.toSet() ?: emptySet()
            )
        } else {
            // New scan — reset to empty
            _uiState.value = _uiState.value.copy(
                parsePreview = null,
                parseError = null,
                scanPhotoUri = null,
                amount = "",
                notes = TextFieldValue(),
                date = null,
                currency = "CAD",
                tags = emptySet()
            )
        }
    }
}
