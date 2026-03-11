package com.ergou.app.ui.expense

import android.content.Context
import android.net.Uri
import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextExpenseCreateRequest
import com.ergou.app.data.remote.dto.NextExpenseEntry
import com.ergou.app.data.remote.dto.NextExpenseSummary
import com.ergou.app.data.remote.dto.NextParsePreview
import com.ergou.app.data.remote.dto.NextParsePreviewItem
import com.ergou.app.data.repository.ReceiptAnalyzer
import com.ergou.app.util.NextAuthProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var nextApiService: NextApiService
    private lateinit var authProvider: NextAuthProvider
    private lateinit var appContext: Context
    private lateinit var receiptAnalyzer: ReceiptAnalyzer
    private lateinit var viewModel: ExpenseViewModel

    private val isLoggedInFlow = MutableStateFlow(false)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        nextApiService = mockk(relaxed = true)
        authProvider = mockk(relaxed = true)
        appContext = mockk(relaxed = true)
        receiptAnalyzer = mockk(relaxed = true)
        every { authProvider.isLoggedIn } returns isLoggedInFlow

        // Default stubs
        coEvery { nextApiService.getExpenses(any(), any(), any()) } returns Result.success(emptyList())
        coEvery { nextApiService.getExpenseSummary(any(), any()) } returns Result.success(
            NextExpenseSummary(totalAmount = 0.0, entryCount = 0, period = "month")
        )
        coEvery { nextApiService.getExpenseTags() } returns Result.success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ExpenseViewModel {
        return ExpenseViewModel(nextApiService, authProvider, appContext, receiptAnalyzer).also { viewModel = it }
    }

    // ── 1. 基础 CRUD ──

    @Test
    fun refreshExpenses_success_updatesState() = runTest {
        val expenses = listOf(
            NextExpenseEntry(id = "1", amount = 25.0, notes = "午餐", currency = "CAD", date = "2026-03-05"),
            NextExpenseEntry(id = "2", amount = 10.0, notes = "咖啡", currency = "CAD", date = "2026-03-05")
        )
        coEvery { nextApiService.getExpenses(any(), any(), any()) } returns Result.success(expenses)

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isLoggedIn)
        assertFalse(state.isLoading)
        assertEquals(2, state.expenses.size)
        assertEquals("午餐", state.expenses[0].notes)
    }

    @Test
    fun refreshExpenses_failure_setsError() = runTest {
        coEvery { nextApiService.getExpenses(any(), any(), any()) } returns Result.failure(Exception("网络超时"))

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("网络超时", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun addExpense_success_refreshesAll() = runTest {
        val newExpense = NextExpenseEntry(id = "3", amount = 50.0, notes = "打车", currency = "CAD")
        coEvery { nextApiService.createExpense(any()) } returns Result.success(newExpense)

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.addExpense(50.0, "打车", listOf("交通"), "CAD")
        advanceUntilIdle()

        coVerify { nextApiService.createExpense(match { it.amount == 50.0 && it.notes == "打车" }) }
        coVerify(atLeast = 2) { nextApiService.getExpenses(any(), any(), any()) }
    }

    @Test
    fun addExpense_withAllFields_sendsCorrectRequest() = runTest {
        val requestSlot = slot<NextExpenseCreateRequest>()
        coEvery { nextApiService.createExpense(capture(requestSlot)) } returns Result.success(
            NextExpenseEntry(id = "1", amount = 88.0, currency = "CAD")
        )

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.addExpense(88.0, "超市采购", listOf("超市"), "CAD", "2026-03-01")
        advanceUntilIdle()

        val req = requestSlot.captured
        assertEquals(88.0, req.amount, 0.01)
        assertEquals("超市采购", req.notes)
        assertEquals(listOf("超市"), req.tags)
        assertEquals("CAD", req.currency)
        assertEquals("2026-03-01", req.date)
    }

    @Test
    fun addExpense_cnyCurrency_sendsCorrectCurrency() = runTest {
        val requestSlot = slot<NextExpenseCreateRequest>()
        coEvery { nextApiService.createExpense(capture(requestSlot)) } returns Result.success(
            NextExpenseEntry(id = "1", amount = 150.0, currency = "CNY")
        )

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.addExpense(150.0, "", null, "CNY")
        advanceUntilIdle()

        assertEquals("CNY", requestSlot.captured.currency)
    }

    @Test
    fun addExpense_multipleTags_sendsAllTags() = runTest {
        val requestSlot = slot<NextExpenseCreateRequest>()
        coEvery { nextApiService.createExpense(capture(requestSlot)) } returns Result.success(
            NextExpenseEntry(id = "1", amount = 30.0, currency = "CAD")
        )

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.addExpense(30.0, "杂货", listOf("超市", "日用"), "CAD")
        advanceUntilIdle()

        assertEquals(listOf("超市", "日用"), requestSlot.captured.tags)
    }

    @Test
    fun addExpense_blankNotes_sendsNullNotes() = runTest {
        val requestSlot = slot<NextExpenseCreateRequest>()
        coEvery { nextApiService.createExpense(capture(requestSlot)) } returns Result.success(
            NextExpenseEntry(id = "1", amount = 25.0, currency = "CAD")
        )

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.addExpense(25.0, "", null, "CAD")
        advanceUntilIdle()

        assertNull(requestSlot.captured.notes)
    }

    @Test
    fun addExpense_failure_setsError() = runTest {
        coEvery { nextApiService.createExpense(any()) } returns Result.failure(Exception("服务器错误"))

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.addExpense(25.0, "午饭", null, "CAD")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.error?.contains("记账失败") == true)
    }

    @Test
    fun deleteExpense_success_refreshesAll() = runTest {
        coEvery { nextApiService.deleteExpense("1") } returns Result.success(Unit)

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.deleteExpense("1")
        advanceUntilIdle()

        coVerify { nextApiService.deleteExpense("1") }
        coVerify(atLeast = 2) { nextApiService.getExpenses(any(), any(), any()) }
    }

    @Test
    fun deleteExpense_failure_setsError() = runTest {
        coEvery { nextApiService.deleteExpense("1") } returns Result.failure(Exception("网络错误"))

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.deleteExpense("1")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.error?.contains("删除失败") == true)
    }

    // ── 2. 筛选和分组 ──

    @Test
    fun filterByTag_refreshesWithTag() = runTest {
        val filteredExpenses = listOf(
            NextExpenseEntry(id = "1", amount = 25.0, notes = "午餐", tags = listOf("餐饮"), currency = "CAD")
        )
        coEvery { nextApiService.getExpenses(any(), any(), tags = "餐饮") } returns Result.success(filteredExpenses)

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.filterByTag("餐饮")
        advanceUntilIdle()

        assertEquals("餐饮", viewModel.uiState.value.selectedTag)
        coVerify { nextApiService.getExpenses(any(), any(), tags = "餐饮") }
    }

    @Test
    fun filterByTag_null_clearsFilter() = runTest {
        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.filterByTag("餐饮")
        advanceUntilIdle()
        viewModel.filterByTag(null)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedTag)
    }

    @Test
    fun selectPeriod_changesStateAndRefreshes() = runTest {
        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.selectPeriod(ExpensePeriod.TODAY)
        advanceUntilIdle()

        assertEquals(ExpensePeriod.TODAY, viewModel.uiState.value.selectedPeriod)
        coVerify { nextApiService.getExpenseSummary("day", any()) }
    }

    @Test
    fun selectPeriod_week_refreshesCorrectly() = runTest {
        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.selectPeriod(ExpensePeriod.WEEK)
        advanceUntilIdle()

        assertEquals(ExpensePeriod.WEEK, viewModel.uiState.value.selectedPeriod)
        coVerify { nextApiService.getExpenseSummary("week", any()) }
    }

    @Test
    fun dayGroups_groupsByDateCorrectly() = runTest {
        val expenses = listOf(
            NextExpenseEntry(id = "1", amount = 25.0, notes = "午餐", date = "2026-03-05", currency = "CAD"),
            NextExpenseEntry(id = "2", amount = 10.0, notes = "咖啡", date = "2026-03-05", currency = "CAD"),
            NextExpenseEntry(id = "3", amount = 100.0, notes = "酒店", date = "2026-03-04", currency = "CAD")
        )
        coEvery { nextApiService.getExpenses(any(), any(), any()) } returns Result.success(expenses)

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        val groups = viewModel.uiState.value.dayGroups
        assertEquals(2, groups.size)
        assertEquals("2026-03-05", groups[0].date)
        assertEquals(2, groups[0].entries.size)
        assertEquals(35.0, groups[0].dayTotal, 0.01)
        assertEquals("2026-03-04", groups[1].date)
        assertEquals(1, groups[1].entries.size)
        assertEquals(100.0, groups[1].dayTotal, 0.01)
    }

    @Test
    fun dayGroups_excludesPendingDelete() = runTest {
        val expenses = listOf(
            NextExpenseEntry(id = "1", amount = 25.0, date = "2026-03-05", currency = "CAD"),
            NextExpenseEntry(id = "2", amount = 10.0, date = "2026-03-05", currency = "CAD")
        )
        coEvery { nextApiService.getExpenses(any(), any(), any()) } returns Result.success(expenses)

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.confirmDelete("1")

        val groups = viewModel.uiState.value.dayGroups
        assertEquals(1, groups.size)
        assertEquals(1, groups[0].entries.size)
        assertEquals("2", groups[0].entries[0].id)
    }

    // ── 3. 乐观删除 (Swipe-to-Delete) ──

    @Test
    fun confirmDelete_setsPendingDeleteId() = runTest {
        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.confirmDelete("1")

        assertEquals("1", viewModel.uiState.value.pendingDeleteId)
    }

    @Test
    fun cancelDelete_clearsPendingDeleteId() = runTest {
        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.confirmDelete("1")
        assertEquals("1", viewModel.uiState.value.pendingDeleteId)

        viewModel.cancelDelete()
        assertNull(viewModel.uiState.value.pendingDeleteId)
    }

    @Test
    fun executeDelete_callsDeleteAndClearsPending() = runTest {
        coEvery { nextApiService.deleteExpense("1") } returns Result.success(Unit)

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.confirmDelete("1")
        viewModel.executeDelete()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.pendingDeleteId)
        coVerify { nextApiService.deleteExpense("1") }
    }

    @Test
    fun executeDelete_noPending_doesNothing() = runTest {
        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.executeDelete()
        advanceUntilIdle()

        coVerify(exactly = 0) { nextApiService.deleteExpense(any()) }
    }

    @Test
    fun filteredExpenses_hidesPendingDelete() = runTest {
        val expenses = listOf(
            NextExpenseEntry(id = "1", amount = 25.0, currency = "CAD"),
            NextExpenseEntry(id = "2", amount = 10.0, currency = "CAD"),
            NextExpenseEntry(id = "3", amount = 50.0, currency = "CAD")
        )
        coEvery { nextApiService.getExpenses(any(), any(), any()) } returns Result.success(expenses)

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.filteredExpenses.size)

        viewModel.confirmDelete("2")
        assertEquals(2, viewModel.uiState.value.filteredExpenses.size)
        assertFalse(viewModel.uiState.value.filteredExpenses.any { it.id == "2" })

        viewModel.cancelDelete()
        assertEquals(3, viewModel.uiState.value.filteredExpenses.size)
    }

    // ── 4. 统计模式 ──

    @Test
    fun toggleStats_togglesShowStats() = runTest {
        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showStats)

        viewModel.toggleStats()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showStats)

        viewModel.toggleStats()
        assertFalse(viewModel.uiState.value.showStats)
    }

    @Test
    fun toggleStats_loadsStatsSummary() = runTest {
        val summary = NextExpenseSummary(totalAmount = 500.0, entryCount = 10, period = "month")
        coEvery { nextApiService.getExpenseSummary("month", any()) } returns Result.success(summary)

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.toggleStats()
        advanceUntilIdle()

        assertEquals(500.0, viewModel.uiState.value.statsSummary?.totalAmount ?: 0.0, 0.01)
    }

    // ── 5. 批量扫描状态管理 ──

    @Test
    fun toggleBatchScan_opensAndCloses() = runTest {
        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showBatchScan)

        viewModel.toggleBatchScan()
        assertTrue(viewModel.uiState.value.showBatchScan)
        assertEquals(emptyList<Uri>(), viewModel.uiState.value.selectedPhotos)

        viewModel.toggleBatchScan()
        assertFalse(viewModel.uiState.value.showBatchScan)
    }

    @Test
    fun addPhotos_addsToSelectedPhotos() = runTest {
        val uri1 = mockk<Uri>()
        val uri2 = mockk<Uri>()

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.addPhotos(listOf(uri1))
        assertEquals(1, viewModel.uiState.value.selectedPhotos.size)

        viewModel.addPhotos(listOf(uri2))
        assertEquals(2, viewModel.uiState.value.selectedPhotos.size)
    }

    @Test
    fun removePhoto_removesAtIndex() = runTest {
        val uri1 = mockk<Uri>()
        val uri2 = mockk<Uri>()
        val uri3 = mockk<Uri>()

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.addPhotos(listOf(uri1, uri2, uri3))
        assertEquals(3, viewModel.uiState.value.selectedPhotos.size)

        viewModel.removePhoto(1)
        assertEquals(2, viewModel.uiState.value.selectedPhotos.size)
        assertEquals(uri1, viewModel.uiState.value.selectedPhotos[0])
        assertEquals(uri3, viewModel.uiState.value.selectedPhotos[1])
    }

    @Test
    fun removePhoto_invalidIndex_doesNothing() = runTest {
        val uri1 = mockk<Uri>()

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.addPhotos(listOf(uri1))
        viewModel.removePhoto(5)
        assertEquals(1, viewModel.uiState.value.selectedPhotos.size)
    }

    @Test
    fun setBatchMergeMode_updatesState() = runTest {
        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.batchMergeMode)

        viewModel.setBatchMergeMode(true)
        assertTrue(viewModel.uiState.value.batchMergeMode)

        viewModel.setBatchMergeMode(false)
        assertFalse(viewModel.uiState.value.batchMergeMode)
    }

    // ── 6. 扫描结果管理 ──

    @Test
    fun updateScanResult_updatesEditedFields() = runTest {
        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        // Manually set scan results for testing
        val preview = NextParsePreview(merchant = "Test", totalAmount = 100.0)
        val uri = mockk<Uri>()
        val item = ScanResultItem(id = "scan1", preview = preview, photoUri = uri)

        // Use reflection or direct state manipulation
        val field = viewModel.javaClass.getDeclaredField("_uiState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(viewModel) as MutableStateFlow<ExpenseUiState>
        stateFlow.value = stateFlow.value.copy(scanResults = listOf(item))

        viewModel.updateScanResult("scan1", amount = 150.0, notes = "Updated", tags = listOf("餐饮"), currency = "CNY")

        val updated = viewModel.uiState.value.scanResults[0]
        assertEquals(150.0, updated.effectiveAmount, 0.01)
        assertEquals("Updated", updated.effectiveNotes)
        assertEquals(listOf("餐饮"), updated.effectiveTags)
        assertEquals("CNY", updated.effectiveCurrency)
    }

    @Test
    fun removeScanResult_removesById() = runTest {
        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        val preview = NextParsePreview(merchant = "Test", totalAmount = 100.0)
        val uri = mockk<Uri>()
        val item1 = ScanResultItem(id = "scan1", preview = preview, photoUri = uri)
        val item2 = ScanResultItem(id = "scan2", preview = preview, photoUri = uri)

        val field = viewModel.javaClass.getDeclaredField("_uiState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(viewModel) as MutableStateFlow<ExpenseUiState>
        stateFlow.value = stateFlow.value.copy(scanResults = listOf(item1, item2))

        viewModel.removeScanResult("scan1")

        assertEquals(1, viewModel.uiState.value.scanResults.size)
        assertEquals("scan2", viewModel.uiState.value.scanResults[0].id)
    }

    // ── 7. Dialog 照片分析 ──

    @Test
    fun addDialogPhotos_addsToList() = runTest {
        val uri1 = mockk<Uri>()
        val uri2 = mockk<Uri>()

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.addDialogPhotos(listOf(uri1))
        assertEquals(1, viewModel.uiState.value.dialogPhotos.size)

        viewModel.addDialogPhotos(listOf(uri2))
        assertEquals(2, viewModel.uiState.value.dialogPhotos.size)
    }

    @Test
    fun removeDialogPhoto_removesAtIndex() = runTest {
        val uri1 = mockk<Uri>()
        val uri2 = mockk<Uri>()

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.addDialogPhotos(listOf(uri1, uri2))
        viewModel.removeDialogPhoto(0)

        assertEquals(1, viewModel.uiState.value.dialogPhotos.size)
        assertEquals(uri2, viewModel.uiState.value.dialogPhotos[0])
    }

    @Test
    fun clearDialogState_resetsAllDialogFields() = runTest {
        val uri = mockk<Uri>()

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.addDialogPhotos(listOf(uri))
        viewModel.clearDialogState()

        val state = viewModel.uiState.value
        assertTrue(state.dialogPhotos.isEmpty())
        assertFalse(state.isDialogAnalyzing)
        assertNull(state.dialogPreview)
        assertNull(state.dialogError)
    }

    // ── 8. 登录状态 ──

    @Test
    fun loggedOut_clearsData() = runTest {
        val expenses = listOf(NextExpenseEntry(id = "1", amount = 25.0, currency = "CAD"))
        coEvery { nextApiService.getExpenses(any(), any(), any()) } returns Result.success(expenses)

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.expenses.size)

        isLoggedInFlow.value = false
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.expenses.isEmpty())
        assertNull(viewModel.uiState.value.summary)
        assertFalse(viewModel.uiState.value.isLoggedIn)
    }

    // ── 9. 错误处理 ──

    @Test
    fun clearError_resetsErrorState() = runTest {
        coEvery { nextApiService.getExpenses(any(), any(), any()) } returns Result.failure(Exception("error"))

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        assertEquals("error", viewModel.uiState.value.error)

        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
    }

    // ── 10. 批量保存 ──

    @Test
    fun saveAllResults_createsExpensesAndRefreshes() = runTest {
        val preview = NextParsePreview(
            merchant = "Food Basics",
            totalAmount = 19.56,
            currency = "CAD",
            tags = listOf("超市"),
            date = "2026-02-12",
            items = listOf(NextParsePreviewItem(name = "Apple", amount = 14.99))
        )
        val uri = mockk<Uri>()
        val item = ScanResultItem(id = "s1", preview = preview, photoUri = uri)

        coEvery { nextApiService.createExpense(any()) } returns Result.success(
            NextExpenseEntry(id = "new1", amount = 19.56, currency = "CAD")
        )

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        val field = viewModel.javaClass.getDeclaredField("_uiState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(viewModel) as MutableStateFlow<ExpenseUiState>
        stateFlow.value = stateFlow.value.copy(scanResults = listOf(item))

        viewModel.saveAllResults(appContext)
        advanceUntilIdle()

        coVerify { nextApiService.createExpense(match { it.amount == 19.56 }) }
        // After save, batch scan should be closed
        assertFalse(viewModel.uiState.value.showBatchScan)
        assertTrue(viewModel.uiState.value.scanResults.isEmpty())
    }

    @Test
    fun saveAllResults_partialFailure_reportsCount() = runTest {
        val preview1 = NextParsePreview(merchant = "A", totalAmount = 10.0)
        val preview2 = NextParsePreview(merchant = "B", totalAmount = 20.0)
        val uri = mockk<Uri>()
        val item1 = ScanResultItem(id = "s1", preview = preview1, photoUri = uri)
        val item2 = ScanResultItem(id = "s2", preview = preview2, photoUri = uri)

        // Second call throws exception to trigger outer catch (failCount++)
        coEvery { nextApiService.createExpense(any()) } returns Result.success(
            NextExpenseEntry(id = "new1", amount = 10.0, currency = "CAD")
        ) andThenThrows RuntimeException("create fail")
        coEvery { nextApiService.uploadExpensePhoto(any(), any(), any()) } returns Result.success(
            mockk(relaxed = true)
        )

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        val field = viewModel.javaClass.getDeclaredField("_uiState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(viewModel) as MutableStateFlow<ExpenseUiState>
        stateFlow.value = stateFlow.value.copy(scanResults = listOf(item1, item2))

        viewModel.saveAllResults(appContext)
        advanceUntilIdle()

        val error = viewModel.uiState.value.error
        assertNotNull("Error should not be null after partial failure", error)
        assertTrue("Error should mention success count: $error", error!!.contains("成功"))
        assertTrue("Error should mention failure count: $error", error.contains("失败"))
    }

    @Test
    fun saveAllResults_empty_doesNothing() = runTest {
        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        viewModel.saveAllResults(appContext)
        advanceUntilIdle()

        coVerify(exactly = 0) { nextApiService.createExpense(any()) }
    }

    // ── 11. ScanResultItem 属性测试 ──

    @Test
    fun scanResultItem_effectiveFields_usePreviewDefaults() {
        val preview = NextParsePreview(merchant = "TestMerchant", totalAmount = 99.99, currency = "CAD", tags = listOf("餐饮"))
        val uri = mockk<Uri>()
        val item = ScanResultItem(preview = preview, photoUri = uri)

        assertEquals(99.99, item.effectiveAmount, 0.01)
        assertEquals("TestMerchant", item.effectiveNotes)
        assertEquals(listOf("餐饮"), item.effectiveTags)
        assertEquals("CAD", item.effectiveCurrency)
    }

    @Test
    fun scanResultItem_effectiveFields_useEditedValues() {
        val preview = NextParsePreview(merchant = "Original", totalAmount = 100.0, currency = "CAD")
        val uri = mockk<Uri>()
        val item = ScanResultItem(
            preview = preview, photoUri = uri,
            editedAmount = 200.0, editedNotes = "Edited", editedTags = listOf("购物"), editedCurrency = "CNY"
        )

        assertEquals(200.0, item.effectiveAmount, 0.01)
        assertEquals("Edited", item.effectiveNotes)
        assertEquals(listOf("购物"), item.effectiveTags)
        assertEquals("CNY", item.effectiveCurrency)
    }

    @Test
    fun scanResultItem_photosToUpload_usesAllPhotoUrisIfPresent() {
        val uri1 = mockk<Uri>()
        val uri2 = mockk<Uri>()
        val uri3 = mockk<Uri>()
        val preview = NextParsePreview(merchant = "Test", totalAmount = 100.0)

        // Merged mode: allPhotoUris set
        val mergedItem = ScanResultItem(preview = preview, photoUri = uri1, allPhotoUris = listOf(uri1, uri2, uri3))
        assertEquals(3, mergedItem.photosToUpload.size)

        // Individual mode: allPhotoUris empty
        val singleItem = ScanResultItem(preview = preview, photoUri = uri1)
        assertEquals(1, singleItem.photosToUpload.size)
        assertEquals(uri1, singleItem.photosToUpload[0])
    }

    // ── 12. Tags 刷新 ──

    @Test
    fun refreshTags_mergesPresetAndApiTags() = runTest {
        coEvery { nextApiService.getExpenseTags() } returns Result.success(listOf("超市", "加油", "医疗"))

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        val tags = viewModel.uiState.value.availableTags
        // PRESET_TAGS = ["餐饮", "交通", "购物", "日用", "娱乐"]
        // API returns ["超市", "加油", "医疗"] — "超市" is NOT in preset, so it's added
        assertTrue(tags.contains("餐饮"))
        assertTrue(tags.contains("交通"))
        assertTrue(tags.contains("超市"))
        assertTrue(tags.contains("加油"))
        assertTrue(tags.contains("医疗"))
    }

    @Test
    fun refreshTags_failure_fallsBackToPresets() = runTest {
        coEvery { nextApiService.getExpenseTags() } returns Result.failure(Exception("fail"))

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        assertEquals(ExpenseViewModel.PRESET_TAGS, viewModel.uiState.value.availableTags)
    }

    // ── 13. Month-over-Month ──

    @Test
    fun monthOverMonthPercent_calculatesCorrectly() = runTest {
        coEvery { nextApiService.getExpenseSummary("month", isNull()) } returns Result.success(
            NextExpenseSummary(totalAmount = 150.0, entryCount = 5, period = "month")
        )
        coEvery { nextApiService.getExpenseSummary("month", match { it != null }) } returns Result.success(
            NextExpenseSummary(totalAmount = 100.0, entryCount = 3, period = "month")
        )

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        val percent = viewModel.uiState.value.monthOverMonthPercent
        assertEquals(50.0, percent ?: 0.0, 0.01)
    }

    @Test
    fun monthOverMonthPercent_lastMonthZero_returnsNull() = runTest {
        coEvery { nextApiService.getExpenseSummary("month", isNull()) } returns Result.success(
            NextExpenseSummary(totalAmount = 150.0, entryCount = 5, period = "month")
        )
        coEvery { nextApiService.getExpenseSummary("month", match { it != null }) } returns Result.success(
            NextExpenseSummary(totalAmount = 0.0, entryCount = 0, period = "month")
        )

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.monthOverMonthPercent)
    }
}
