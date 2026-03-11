package com.ergou.app.ui.expense

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.text.input.TextFieldValue
import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextExpenseDetailResponse
import com.ergou.app.data.remote.dto.NextExpenseEntry
import com.ergou.app.data.remote.dto.NextExpensePhoto
import com.ergou.app.data.remote.dto.NextExpenseUpdateRequest
import com.ergou.app.data.remote.dto.NextParsePreview
import com.ergou.app.data.remote.dto.NextParsePreviewItem
import com.ergou.app.data.repository.ReceiptAnalyzer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var nextApiService: NextApiService
    private lateinit var appContext: Context
    private lateinit var contentResolver: ContentResolver
    private lateinit var receiptAnalyzer: ReceiptAnalyzer
    private lateinit var viewModel: ExpenseDetailViewModel

    private val sampleEntry = NextExpenseEntry(
        id = "exp1",
        amount = 19.56,
        date = "2026-02-12",
        notes = "Food Basics",
        tags = listOf("超市"),
        currency = "CAD",
        photoCount = 0,
        itemCount = 0
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        nextApiService = mockk(relaxed = true)
        appContext = mockk(relaxed = true)
        contentResolver = mockk(relaxed = true)
        receiptAnalyzer = mockk(relaxed = true)
        every { appContext.contentResolver } returns contentResolver

        // Mock android.util.Base64 which is not available in JVM tests
        mockkStatic(Base64::class)
        every { Base64.encodeToString(any(), any()) } answers {
            java.util.Base64.getEncoder().encodeToString(firstArg())
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Base64::class)
    }

    private fun createViewModel(expenseId: String? = null): ExpenseDetailViewModel {
        return ExpenseDetailViewModel(expenseId, nextApiService, appContext, receiptAnalyzer).also { viewModel = it }
    }

    // ── 1. 加载详情 ──

    @Test
    fun loadDetail_success_populatesState() = runTest {
        coEvery { nextApiService.getExpenseDetail("exp1") } returns Result.success(NextExpenseDetailResponse(success = true, entry = sampleEntry))

        createViewModel("exp1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("exp1", state.entry?.id)
        assertEquals("19.56", state.amount)
        assertEquals("Food Basics", state.notes.text)
        assertEquals("2026-02-12", state.date)
        assertEquals("CAD", state.currency)
        assertTrue(state.tags.contains("超市"))
    }

    @Test
    fun loadDetail_integerAmount_displaysAsInteger() = runTest {
        val entry = sampleEntry.copy(amount = 100.0)
        coEvery { nextApiService.getExpenseDetail("exp1") } returns Result.success(NextExpenseDetailResponse(success = true, entry = entry))

        createViewModel("exp1")
        advanceUntilIdle()

        assertEquals("100", viewModel.uiState.value.amount)
    }

    @Test
    fun loadDetail_failure_setsError() = runTest {
        coEvery { nextApiService.getExpenseDetail("exp1") } returns Result.failure(Exception("网络错误"))

        createViewModel("exp1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.error?.contains("加载失败") == true)
    }

    @Test
    fun loadDetail_nullNotes_setsEmptyString() = runTest {
        val entry = sampleEntry.copy(notes = null)
        coEvery { nextApiService.getExpenseDetail("exp1") } returns Result.success(NextExpenseDetailResponse(success = true, entry = entry))

        createViewModel("exp1")
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.notes.text)
    }

    @Test
    fun loadDetail_nullTags_setsEmptySet() = runTest {
        val entry = sampleEntry.copy(tags = null)
        coEvery { nextApiService.getExpenseDetail("exp1") } returns Result.success(NextExpenseDetailResponse(success = true, entry = entry))

        createViewModel("exp1")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.tags.isEmpty())
    }

    @Test
    fun loadDetail_success_loadsPhotos() = runTest {
        val photos = listOf(
            NextExpensePhoto(id = "p1", entryId = "exp1", filename = "a.jpg"),
            NextExpensePhoto(id = "p2", entryId = "exp1", filename = "b.jpg")
        )
        coEvery { nextApiService.getExpenseDetail("exp1") } returns Result.success(
            NextExpenseDetailResponse(success = true, entry = sampleEntry, photos = photos)
        )

        createViewModel("exp1")
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.photos.size)
        assertEquals("p1", viewModel.uiState.value.photos[0].id)
        assertEquals("p2", viewModel.uiState.value.photos[1].id)
    }

    @Test
    fun init_nullExpenseId_doesNotLoad() = runTest {
        createViewModel(null)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.entry)
        coVerify(exactly = 0) { nextApiService.getExpenseDetail(any()) }
    }

    // ── 2. 编辑字段 ──

    @Test
    fun updateAmount_updatesState() = runTest {
        createViewModel()

        viewModel.updateAmount("20.00")
        assertEquals("20.00", viewModel.uiState.value.amount)
    }

    @Test
    fun updateNotes_updatesState() = runTest {
        createViewModel()

        viewModel.updateNotes(TextFieldValue("含小费"))
        assertEquals("含小费", viewModel.uiState.value.notes.text)
    }

    @Test
    fun updateDate_updatesState() = runTest {
        createViewModel()

        viewModel.updateDate("2026-02-15")
        assertEquals("2026-02-15", viewModel.uiState.value.date)
    }

    @Test
    fun updateCurrency_updatesState() = runTest {
        createViewModel()

        viewModel.updateCurrency("CNY")
        assertEquals("CNY", viewModel.uiState.value.currency)
    }

    @Test
    fun toggleTag_addsAndRemoves() = runTest {
        createViewModel()

        viewModel.toggleTag("餐饮")
        assertTrue(viewModel.uiState.value.tags.contains("餐饮"))

        viewModel.toggleTag("日用")
        assertEquals(setOf("餐饮", "日用"), viewModel.uiState.value.tags)

        viewModel.toggleTag("餐饮")
        assertEquals(setOf("日用"), viewModel.uiState.value.tags)
    }

    // ── 3. 保存变更 ──

    @Test
    fun saveChanges_sendsUpdateRequest() = runTest {
        coEvery { nextApiService.getExpenseDetail("exp1") } returns Result.success(NextExpenseDetailResponse(success = true, entry = sampleEntry))
        val requestSlot = slot<NextExpenseUpdateRequest>()
        coEvery { nextApiService.updateExpense("exp1", capture(requestSlot)) } returns Result.success(sampleEntry)

        createViewModel("exp1")
        advanceUntilIdle()

        viewModel.updateAmount("20.00")
        viewModel.updateNotes(TextFieldValue("含小费"))
        viewModel.saveChanges()
        advanceUntilIdle()

        val req = requestSlot.captured
        assertEquals(20.0, req.amount ?: 0.0, 0.01)
        assertEquals("含小费", req.notes)
    }

    @Test
    fun saveChanges_sendsNavigateBackEvent() = runTest {
        coEvery { nextApiService.getExpenseDetail("exp1") } returns Result.success(NextExpenseDetailResponse(success = true, entry = sampleEntry))
        coEvery { nextApiService.updateExpense(any(), any()) } returns Result.success(sampleEntry)

        createViewModel("exp1")
        advanceUntilIdle()

        viewModel.saveChanges()
        advanceUntilIdle()

        val event = viewModel.events.first()
        assertTrue(event is ExpenseDetailEvent.ShowMessage || event is ExpenseDetailEvent.NavigateBack)
    }

    @Test
    fun saveChanges_failure_setsError() = runTest {
        coEvery { nextApiService.getExpenseDetail("exp1") } returns Result.success(NextExpenseDetailResponse(success = true, entry = sampleEntry))
        coEvery { nextApiService.updateExpense(any(), any()) } returns Result.failure(Exception("网络错误"))

        createViewModel("exp1")
        advanceUntilIdle()

        viewModel.saveChanges()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.error?.contains("保存失败") == true)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun saveChanges_invalidAmount_doesNothing() = runTest {
        coEvery { nextApiService.getExpenseDetail("exp1") } returns Result.success(NextExpenseDetailResponse(success = true, entry = sampleEntry))

        createViewModel("exp1")
        advanceUntilIdle()

        viewModel.updateAmount("abc")
        viewModel.saveChanges()
        advanceUntilIdle()

        coVerify(exactly = 0) { nextApiService.updateExpense(any(), any()) }
    }

    @Test
    fun saveChanges_noEntry_doesNothing() = runTest {
        createViewModel(null)
        advanceUntilIdle()

        viewModel.saveChanges()
        advanceUntilIdle()

        coVerify(exactly = 0) { nextApiService.updateExpense(any(), any()) }
    }

    @Test
    fun saveChanges_currencySwitch_sendsCurrency() = runTest {
        coEvery { nextApiService.getExpenseDetail("exp1") } returns Result.success(NextExpenseDetailResponse(success = true, entry = sampleEntry))
        val requestSlot = slot<NextExpenseUpdateRequest>()
        coEvery { nextApiService.updateExpense("exp1", capture(requestSlot)) } returns Result.success(sampleEntry)

        createViewModel("exp1")
        advanceUntilIdle()

        viewModel.updateCurrency("CNY")
        viewModel.saveChanges()
        advanceUntilIdle()

        assertEquals("CNY", requestSlot.captured.currency)
    }

    @Test
    fun saveChanges_withCustomTags_mergesAll() = runTest {
        coEvery { nextApiService.getExpenseDetail("exp1") } returns Result.success(NextExpenseDetailResponse(success = true, entry = sampleEntry))
        val requestSlot = slot<NextExpenseUpdateRequest>()
        coEvery { nextApiService.updateExpense("exp1", capture(requestSlot)) } returns Result.success(sampleEntry)

        createViewModel("exp1")
        advanceUntilIdle()

        viewModel.toggleTag("餐饮")
        viewModel.updateCustomTagInput(TextFieldValue("自定义"))
        viewModel.saveChanges()
        advanceUntilIdle()

        val tags = requestSlot.captured.tags
        assertNotNull(tags)
        assertTrue(tags!!.contains("超市"))
        assertTrue(tags.contains("餐饮"))
        assertTrue(tags.contains("自定义"))
    }

    // ── 4. 删除 ──

    @Test
    fun deleteExpense_success_sendsNavigateBack() = runTest {
        coEvery { nextApiService.getExpenseDetail("exp1") } returns Result.success(NextExpenseDetailResponse(success = true, entry = sampleEntry))
        coEvery { nextApiService.deleteExpense("exp1") } returns Result.success(Unit)

        createViewModel("exp1")
        advanceUntilIdle()

        viewModel.deleteExpense()
        advanceUntilIdle()

        coVerify { nextApiService.deleteExpense("exp1") }
        assertFalse(viewModel.uiState.value.isDeleting)
    }

    @Test
    fun deleteExpense_failure_setsError() = runTest {
        coEvery { nextApiService.getExpenseDetail("exp1") } returns Result.success(NextExpenseDetailResponse(success = true, entry = sampleEntry))
        coEvery { nextApiService.deleteExpense("exp1") } returns Result.failure(Exception("删除失败"))

        createViewModel("exp1")
        advanceUntilIdle()

        viewModel.deleteExpense()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.error?.contains("删除失败") == true)
        assertFalse(viewModel.uiState.value.isDeleting)
    }

    @Test
    fun deleteExpense_noEntry_doesNothing() = runTest {
        createViewModel(null)
        advanceUntilIdle()

        viewModel.deleteExpense()
        advanceUntilIdle()

        coVerify(exactly = 0) { nextApiService.deleteExpense(any()) }
    }

    // ── 5. 照片管理 ──

    @Test
    fun uploadPhoto_success_addsToPhotos() = runTest {
        coEvery { nextApiService.getExpenseDetail("exp1") } returns Result.success(NextExpenseDetailResponse(success = true, entry = sampleEntry))
        val photo = NextExpensePhoto(id = "p1", entryId = "exp1", filename = "test.jpg")
        val uri = mockk<Uri>()
        // compressImage fallback reads raw bytes via openInputStream (BitmapFactory unavailable in unit tests)
        every { contentResolver.openInputStream(uri) } answers { ByteArrayInputStream(ByteArray(100)) }
        coEvery { nextApiService.uploadExpensePhotoBytes("exp1", any(), any()) } returns Result.success(photo)

        createViewModel("exp1")
        advanceUntilIdle()

        viewModel.uploadPhoto(uri)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.photos.size)
        assertEquals("p1", viewModel.uiState.value.photos[0].id)
        assertFalse(viewModel.uiState.value.isUploadingPhoto)
    }

    @Test
    fun uploadPhoto_exceeds10MB_setsError() = runTest {
        coEvery { nextApiService.getExpenseDetail("exp1") } returns Result.success(NextExpenseDetailResponse(success = true, entry = sampleEntry))
        val uri = mockk<Uri>()
        // compressImage fallback reads raw bytes; uploadExpensePhotoBytes rejects > 10MB
        every { contentResolver.openInputStream(uri) } answers { ByteArrayInputStream(ByteArray(11 * 1024 * 1024)) }
        coEvery { nextApiService.uploadExpensePhotoBytes(any(), any(), any()) } returns Result.failure(Exception("照片大小不能超过10MB"))

        createViewModel("exp1")
        advanceUntilIdle()

        viewModel.uploadPhoto(uri)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.error?.contains("上传失败") == true)
        coVerify { nextApiService.uploadExpensePhotoBytes(any(), any(), any()) }
    }

    @Test
    fun uploadPhoto_failure_setsError() = runTest {
        coEvery { nextApiService.getExpenseDetail("exp1") } returns Result.success(NextExpenseDetailResponse(success = true, entry = sampleEntry))
        val uri = mockk<Uri>()
        every { contentResolver.openInputStream(uri) } answers { ByteArrayInputStream(ByteArray(100)) }
        coEvery { nextApiService.uploadExpensePhotoBytes(any(), any(), any()) } returns Result.failure(Exception("上传失败"))

        createViewModel("exp1")
        advanceUntilIdle()

        viewModel.uploadPhoto(uri)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.error?.contains("上传失败") == true)
    }

    @Test
    fun deletePhoto_success_removesFromList() = runTest {
        coEvery { nextApiService.getExpenseDetail("exp1") } returns Result.success(NextExpenseDetailResponse(success = true, entry = sampleEntry))
        coEvery { nextApiService.deleteExpensePhoto("p1") } returns Result.success(Unit)

        createViewModel("exp1")
        advanceUntilIdle()

        // Manually add photos to state
        val field = viewModel.javaClass.getDeclaredField("_uiState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(viewModel) as MutableStateFlow<ExpenseDetailUiState>
        val photo1 = NextExpensePhoto(id = "p1", entryId = "exp1", filename = "a.jpg")
        val photo2 = NextExpensePhoto(id = "p2", entryId = "exp1", filename = "b.jpg")
        stateFlow.value = stateFlow.value.copy(photos = listOf(photo1, photo2))

        viewModel.deletePhoto("p1")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.photos.size)
        assertEquals("p2", viewModel.uiState.value.photos[0].id)
    }

    @Test
    fun deletePhoto_failure_setsError() = runTest {
        coEvery { nextApiService.getExpenseDetail("exp1") } returns Result.success(NextExpenseDetailResponse(success = true, entry = sampleEntry))
        coEvery { nextApiService.deleteExpensePhoto("p1") } returns Result.failure(Exception("网络错误"))

        createViewModel("exp1")
        advanceUntilIdle()

        viewModel.deletePhoto("p1")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.error?.contains("删除照片失败") == true)
    }

    // ── 6. AI 解析 ──

    @Test
    fun parseReceipt_success_populatesPreview() = runTest {
        val uri = mockk<Uri>()
        val preview = NextParsePreview(
            merchant = "Food Basics",
            totalAmount = 19.56,
            date = "2026-02-12",
            currency = "CAD",
            tags = listOf("超市"),
            items = listOf(NextParsePreviewItem(name = "Apple", amount = 14.99))
        )
        val photoBytes = ByteArray(500)
        val afd = mockk<android.content.res.AssetFileDescriptor>(relaxed = true)
        every { afd.length } returns 500L
        every { contentResolver.openAssetFileDescriptor(uri, "r") } returns afd
        every { contentResolver.openInputStream(uri) } returns ByteArrayInputStream(photoBytes)
        coEvery { receiptAnalyzer.analyze(any(), any()) } returns preview

        createViewModel()

        viewModel.parseReceipt(uri)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isParsing)
        assertNotNull(state.parsePreview)
        assertEquals("Food Basics", state.notes.text)
        assertEquals("19.56", state.amount)
        assertEquals("2026-02-12", state.date)
        assertEquals("CAD", state.currency)
        assertTrue(state.tags.contains("超市"))
    }

    @Test
    fun parseReceipt_timeout_setsTimeoutError() = runTest {
        val uri = mockk<Uri>()
        val photoBytes = ByteArray(500)
        val afd = mockk<android.content.res.AssetFileDescriptor>(relaxed = true)
        every { afd.length } returns 500L
        every { contentResolver.openAssetFileDescriptor(uri, "r") } returns afd
        every { contentResolver.openInputStream(uri) } returns ByteArrayInputStream(photoBytes)
        coEvery { receiptAnalyzer.analyze(any(), any()) } throws Exception("Connection timeout")

        createViewModel()

        viewModel.parseReceipt(uri)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isParsing)
        assertTrue(state.parseError?.contains("超时") == true)
    }

    @Test
    fun parseReceipt_exceeds10MB_setsError() = runTest {
        val uri = mockk<Uri>()
        val afd = mockk<android.content.res.AssetFileDescriptor>(relaxed = true)
        every { afd.length } returns (11L * 1024 * 1024)
        every { contentResolver.openAssetFileDescriptor(uri, "r") } returns afd

        createViewModel()

        viewModel.parseReceipt(uri)
        advanceUntilIdle()

        assertEquals("照片不能超过 10MB", viewModel.uiState.value.parseError)
        coVerify(exactly = 0) { receiptAnalyzer.analyze(any(), any()) }
    }

    @Test
    fun parseReceipt_cannotReadImage_setsError() = runTest {
        val uri = mockk<Uri>()
        every { contentResolver.openAssetFileDescriptor(uri, "r") } returns null
        every { contentResolver.openInputStream(uri) } returns null

        createViewModel()

        viewModel.parseReceipt(uri)
        advanceUntilIdle()

        assertEquals("无法读取图片", viewModel.uiState.value.parseError)
    }

    @Test
    fun parseReceipt_genericFailure_setsParseError() = runTest {
        val uri = mockk<Uri>()
        val photoBytes = ByteArray(500)
        val afd = mockk<android.content.res.AssetFileDescriptor>(relaxed = true)
        every { afd.length } returns 500L
        every { contentResolver.openAssetFileDescriptor(uri, "r") } returns afd
        every { contentResolver.openInputStream(uri) } returns ByteArrayInputStream(photoBytes)
        coEvery { receiptAnalyzer.analyze(any(), any()) } throws Exception("AI 返回格式异常")

        createViewModel()

        viewModel.parseReceipt(uri)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.parseError?.contains("解析失败") == true)
    }

    // ── 7. 确认解析并保存 ──

    @Test
    fun confirmParseAndSave_createsExpenseAndUploadsPhoto() = runTest {
        val uri = mockk<Uri>()
        val preview = NextParsePreview(
            merchant = "Food Basics", totalAmount = 19.56, currency = "CAD",
            tags = listOf("超市"), date = "2026-02-12",
            items = listOf(NextParsePreviewItem(name = "Apple", amount = 14.99))
        )
        val newEntry = NextExpenseEntry(id = "new1", amount = 19.56, currency = "CAD")

        coEvery { nextApiService.createExpense(any()) } returns Result.success(newEntry)
        // compressImage fallback reads raw bytes
        every { contentResolver.openInputStream(uri) } answers { ByteArrayInputStream(ByteArray(100)) }
        coEvery { nextApiService.uploadExpensePhotoBytes("new1", any(), any()) } returns Result.success(
            NextExpensePhoto(id = "p1", entryId = "new1", filename = "test.jpg")
        )

        createViewModel()
        // Set up parse state
        val field = viewModel.javaClass.getDeclaredField("_uiState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(viewModel) as MutableStateFlow<ExpenseDetailUiState>
        stateFlow.value = stateFlow.value.copy(
            parsePreview = preview,
            amount = "19.56",
            notes = TextFieldValue("Food Basics"),
            date = "2026-02-12",
            currency = "CAD",
            tags = setOf("超市"),
            scanPhotoUri = uri
        )

        viewModel.confirmParseAndSave()
        advanceUntilIdle()

        coVerify { nextApiService.createExpense(match { it.amount == 19.56 && it.notes == "Food Basics" }) }
        coVerify { nextApiService.uploadExpensePhotoBytes("new1", any(), any()) }
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun confirmParseAndSave_createFails_setsError() = runTest {
        val preview = NextParsePreview(merchant = "Test", totalAmount = 100.0)
        coEvery { nextApiService.createExpense(any()) } returns Result.failure(Exception("创建失败"))

        createViewModel()
        val field = viewModel.javaClass.getDeclaredField("_uiState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(viewModel) as MutableStateFlow<ExpenseDetailUiState>
        stateFlow.value = stateFlow.value.copy(
            parsePreview = preview, amount = "100", notes = TextFieldValue("Test"),
            currency = "CAD", tags = emptySet()
        )

        viewModel.confirmParseAndSave()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.error?.contains("保存失败") == true)
    }

    @Test
    fun confirmParseAndSave_photoUploadFails_stillSavesExpense() = runTest {
        val uri = mockk<Uri>()
        val preview = NextParsePreview(merchant = "Test", totalAmount = 50.0)
        val newEntry = NextExpenseEntry(id = "new1", amount = 50.0, currency = "CAD")

        coEvery { nextApiService.createExpense(any()) } returns Result.success(newEntry)
        // compressImage fallback reads raw bytes
        every { contentResolver.openInputStream(uri) } answers { ByteArrayInputStream(ByteArray(100)) }
        coEvery { nextApiService.uploadExpensePhotoBytes(any(), any(), any()) } returns Result.failure(Exception("upload fail"))

        createViewModel()
        val field = viewModel.javaClass.getDeclaredField("_uiState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(viewModel) as MutableStateFlow<ExpenseDetailUiState>
        stateFlow.value = stateFlow.value.copy(
            parsePreview = preview, amount = "50", notes = TextFieldValue("Test"),
            currency = "CAD", tags = emptySet(), scanPhotoUri = uri
        )

        viewModel.confirmParseAndSave()
        advanceUntilIdle()

        // Expense was created successfully despite photo upload failure
        coVerify { nextApiService.createExpense(any()) }
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun confirmParseAndSave_noPreview_doesNothing() = runTest {
        createViewModel()

        viewModel.confirmParseAndSave()
        advanceUntilIdle()

        coVerify(exactly = 0) { nextApiService.createExpense(any()) }
    }

    // ── 8. 取消解析 ──

    @Test
    fun cancelParse_resetsAllParseState() = runTest {
        createViewModel()

        val field = viewModel.javaClass.getDeclaredField("_uiState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(viewModel) as MutableStateFlow<ExpenseDetailUiState>
        stateFlow.value = stateFlow.value.copy(
            parsePreview = NextParsePreview(merchant = "Test", totalAmount = 100.0),
            amount = "100",
            notes = TextFieldValue("Test"),
            date = "2026-01-01",
            currency = "CNY",
            tags = setOf("餐饮"),
            scanPhotoUri = mockk()
        )

        viewModel.cancelParse()

        val state = viewModel.uiState.value
        assertNull(state.parsePreview)
        assertNull(state.parseError)
        assertNull(state.scanPhotoUri)
        assertEquals("", state.amount)
        assertEquals("", state.notes.text)
        assertNull(state.date)
        assertEquals("CAD", state.currency)
        assertTrue(state.tags.isEmpty())
    }

    // ── 9. 错误处理 ──

    @Test
    fun clearError_clearsBothErrors() = runTest {
        createViewModel()

        val field = viewModel.javaClass.getDeclaredField("_uiState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(viewModel) as MutableStateFlow<ExpenseDetailUiState>
        stateFlow.value = stateFlow.value.copy(error = "some error", parseError = "parse error")

        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.parseError)
    }

    // ── 10. 编辑后保存完整流程 ──

    @Test
    fun editAndSave_multipleFieldChanges_allSent() = runTest {
        coEvery { nextApiService.getExpenseDetail("exp1") } returns Result.success(NextExpenseDetailResponse(success = true, entry = sampleEntry))
        val requestSlot = slot<NextExpenseUpdateRequest>()
        coEvery { nextApiService.updateExpense("exp1", capture(requestSlot)) } returns Result.success(sampleEntry)

        createViewModel("exp1")
        advanceUntilIdle()

        // Modify multiple fields
        viewModel.updateAmount("20.00")
        viewModel.updateNotes(TextFieldValue("含小费"))
        viewModel.updateDate("2026-02-15")
        viewModel.updateCurrency("CNY")
        viewModel.toggleTag("餐饮") // add
        viewModel.toggleTag("超市") // remove (was from load)

        viewModel.saveChanges()
        advanceUntilIdle()

        val req = requestSlot.captured
        assertEquals(20.0, req.amount ?: 0.0, 0.01)
        assertEquals("含小费", req.notes)
        assertEquals("2026-02-15", req.date)
        assertEquals("CNY", req.currency)
        assertTrue(req.tags?.contains("餐饮") == true)
        assertFalse(req.tags?.contains("超市") == true)
    }
}
