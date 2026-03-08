package com.ergou.app.data.remote.api

import android.content.Context
import android.net.Uri
import com.ergou.app.data.remote.dto.*
import com.ergou.app.util.NextAuthProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.flow.first
import timber.log.Timber

class NextApiService(
    private val httpClient: HttpClient,
    private val authProvider: NextAuthProvider
) {

    companion object {
        internal const val BASE_URL = "https://next-boris.fly.dev"
    }

    // ── Auth ──

    suspend fun login(username: String, password: String): Result<String> = runCatching {
        Timber.d("[Next] 登录 username=%s", username)
        val response = httpClient.post("$BASE_URL/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(NextLoginRequest(username, password))
        }

        val setCookie = response.headers["Set-Cookie"] ?: ""
        val token = setCookie.substringAfter("session=", "")
            .substringBefore(";")
            .trim()

        val body = response.body<NextLoginResponse>()
        if (!body.success || token.isBlank()) {
            throw Exception(body.message ?: "登录失败")
        }

        authProvider.saveSession(token, username)
        Timber.d("[Next] 登录成功 username=%s", username)
        username
    }

    suspend fun logout() {
        authProvider.clearSession()
        Timber.d("[Next] 已登出")
    }

    // ── Todo CRUD ──

    suspend fun getTodos(tab: String? = null): Result<List<com.ergou.app.data.remote.dto.NextTodo>> =
        apiCall {
            val url = if (tab != null) "$BASE_URL/api/todos?tab=$tab" else "$BASE_URL/api/todos"
            val response = httpClient.get(url) { authHeader() }
            response.body<NextTodoListResponse>().items
        }

    suspend fun createTodo(request: NextTodoCreateRequest): Result<com.ergou.app.data.remote.dto.NextTodo> =
        apiCall {
            val response = httpClient.post("$BASE_URL/api/todos") {
                authHeader()
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.body<NextTodoDetailResponse>().item
                ?: throw Exception("创建失败：响应中无 item")
        }

    suspend fun getTodoById(id: String): Result<com.ergou.app.data.remote.dto.NextTodo> =
        apiCall {
            val response = httpClient.get("$BASE_URL/api/todos/$id") { authHeader() }
            response.body<NextTodoDetailResponse>().item
                ?: throw Exception("未找到 todo: $id")
        }

    suspend fun updateTodo(id: String, request: NextTodoUpdateRequest): Result<com.ergou.app.data.remote.dto.NextTodo> =
        apiCall {
            val response = httpClient.put("$BASE_URL/api/todos/$id") {
                authHeader()
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.body<NextTodoDetailResponse>().item
                ?: throw Exception("更新失败：响应中无 item")
        }

    suspend fun deleteTodo(id: String): Result<Unit> =
        apiCall {
            httpClient.delete("$BASE_URL/api/todos/$id") { authHeader() }
            Unit
        }

    suspend fun restoreTodo(id: String): Result<com.ergou.app.data.remote.dto.NextTodo> =
        apiCall {
            val response = httpClient.post("$BASE_URL/api/todos/$id/restore") { authHeader() }
            response.body<NextTodoDetailResponse>().item
                ?: throw Exception("恢复失败：响应中无 item")
        }

    suspend fun getTodoCounts(tab: String): Result<NextTodoCountsResponse> =
        apiCall {
            val response = httpClient.get("$BASE_URL/api/todos/counts?tab=$tab") { authHeader() }
            response.body<NextTodoCountsResponse>()
        }

    // ── Routine CRUD ──

    suspend fun getRoutines(): Result<List<NextRoutine>> =
        apiCall {
            val response = httpClient.get("$BASE_URL/api/routines") { authHeader() }
            response.body<NextRoutineListResponse>().items
        }

    suspend fun createRoutine(text: String): Result<NextRoutine> =
        apiCall {
            val response = httpClient.post("$BASE_URL/api/routines") {
                authHeader()
                contentType(ContentType.Application.Json)
                setBody(NextRoutineCreateRequest(text))
            }
            response.body<NextRoutineDetailResponse>().item
                ?: throw Exception("创建失败：响应中无 item")
        }

    suspend fun toggleRoutine(id: String): Result<NextRoutine> =
        apiCall {
            val response = httpClient.post("$BASE_URL/api/routines/$id/toggle") { authHeader() }
            response.body<NextRoutineDetailResponse>().item
                ?: throw Exception("切换失败：响应中无 item")
        }

    suspend fun deleteRoutine(id: String): Result<Unit> =
        apiCall {
            httpClient.delete("$BASE_URL/api/routines/$id") { authHeader() }
            Unit
        }

    // ── Review CRUD ──

    suspend fun getReviews(): Result<List<NextReview>> =
        apiCall {
            val response = httpClient.get("$BASE_URL/api/reviews") { authHeader() }
            response.body<NextReviewListResponse>().items
        }

    suspend fun createReview(request: NextReviewCreateRequest): Result<NextReview> =
        apiCall {
            val response = httpClient.post("$BASE_URL/api/reviews") {
                authHeader()
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.body<NextReviewDetailResponse>().item
                ?: throw Exception("创建失败：响应中无 item")
        }

    suspend fun updateReview(id: String, request: NextReviewUpdateRequest): Result<NextReview> =
        apiCall {
            val response = httpClient.put("$BASE_URL/api/reviews/$id") {
                authHeader()
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.body<NextReviewDetailResponse>().item
                ?: throw Exception("更新失败：响应中无 item")
        }

    suspend fun deleteReview(id: String): Result<Unit> =
        apiCall {
            httpClient.delete("$BASE_URL/api/reviews/$id") { authHeader() }
            Unit
        }

    suspend fun completeReview(id: String): Result<NextReview> =
        apiCall {
            val response = httpClient.post("$BASE_URL/api/reviews/$id/complete") { authHeader() }
            response.body<NextReviewDetailResponse>().item
                ?: throw Exception("完成失败：响应中无 item")
        }

    suspend fun uncompleteReview(id: String): Result<NextReview> =
        apiCall {
            val response = httpClient.post("$BASE_URL/api/reviews/$id/uncomplete") { authHeader() }
            response.body<NextReviewDetailResponse>().item
                ?: throw Exception("取消完成失败：响应中无 item")
        }

    // ── Expense CRUD ──

    suspend fun getExpenses(from: String? = null, to: String? = null, tags: String? = null): Result<List<NextExpenseEntry>> =
        apiCall {
            val params = mutableListOf<String>()
            from?.let { params.add("from=$it") }
            to?.let { params.add("to=$it") }
            tags?.let { params.add("tags=$it") }
            val query = if (params.isNotEmpty()) "?${params.joinToString("&")}" else ""
            val response = httpClient.get("$BASE_URL/api/expenses$query") { authHeader() }
            response.body<NextExpenseListResponse>().entries
        }

    suspend fun createExpense(request: NextExpenseCreateRequest): Result<NextExpenseEntry> =
        apiCall {
            val response = httpClient.post("$BASE_URL/api/expenses") {
                authHeader()
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.body<NextExpenseDetailResponse>().entry
                ?: throw Exception("创建失败：响应中无 entry")
        }

    suspend fun getExpenseById(id: String): Result<NextExpenseEntry> =
        apiCall {
            val response = httpClient.get("$BASE_URL/api/expenses/$id") { authHeader() }
            response.body<NextExpenseDetailResponse>().entry
                ?: throw Exception("未找到 expense: $id")
        }

    suspend fun updateExpense(id: String, request: NextExpenseUpdateRequest): Result<NextExpenseEntry> =
        apiCall {
            val response = httpClient.put("$BASE_URL/api/expenses/$id") {
                authHeader()
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.body<NextExpenseDetailResponse>().entry
                ?: throw Exception("更新失败：响应中无 entry")
        }

    suspend fun deleteExpense(id: String): Result<Unit> =
        apiCall {
            httpClient.delete("$BASE_URL/api/expenses/$id") { authHeader() }
            Unit
        }

    suspend fun getExpenseSummary(period: String, date: String? = null): Result<NextExpenseSummary> =
        apiCall {
            val query = if (date != null) "?period=$period&date=$date" else "?period=$period"
            val response = httpClient.get("$BASE_URL/api/expenses/summary$query") { authHeader() }
            response.body<NextExpenseSummaryResponse>().summary
                ?: throw Exception("获取汇总失败")
        }

    suspend fun getExpenseTags(): Result<List<String>> =
        apiCall {
            val response = httpClient.get("$BASE_URL/api/expenses/tags") { authHeader() }
            response.body<NextExpenseTagsResponse>().tags
        }

    suspend fun uploadExpensePhoto(entryId: String, uri: Uri, context: Context): Result<NextExpensePhoto> =
        apiCall {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw Exception("无法读取图片")
            val fileName = "photo.${mimeType.substringAfter("/", "jpg")}"

            val token = authProvider.sessionToken.first()
            val response = httpClient.submitFormWithBinaryData(
                url = "$BASE_URL/api/expenses/$entryId/photos",
                formData = formData {
                    append("photo", bytes, Headers.build {
                        append(HttpHeaders.ContentType, mimeType)
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    })
                }
            ) {
                if (token.isNotBlank()) header("Cookie", "session=$token")
            }
            response.body<NextExpensePhotoResponse>().photo
                ?: throw Exception("上传失败：响应中无 photo")
        }

    suspend fun deleteExpensePhoto(photoId: String): Result<Unit> =
        apiCall {
            httpClient.delete("$BASE_URL/api/expenses/photos/$photoId") { authHeader() }
            Unit
        }

    suspend fun parseReceiptPreview(imageBase64: String, mimeType: String = "image/jpeg"): Result<NextParsePreview> =
        parseReceiptPreview(images = listOf(imageBase64), mimeTypes = listOf(mimeType))

    suspend fun parseReceiptPreview(images: List<String> = emptyList(), mimeTypes: List<String> = emptyList(), text: String? = null): Result<NextParsePreview> =
        apiCall {
            val imageObjects = images.mapIndexed { index, base64 ->
                NextParsePreviewImage(
                    data = base64,
                    mimeType = mimeTypes.getOrElse(index) { "image/jpeg" }
                )
            }
            val request = NextParsePreviewRequest(
                images = imageObjects,
                text = text?.ifBlank { null }
            )
            val response = httpClient.post("$BASE_URL/api/expenses/parse-preview") {
                authHeader()
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val parsed = response.body<NextParsePreviewResponse>()
            parsed.preview
                ?: throw Exception("解析失败：${parsed.message ?: "响应中无 preview"}")
        }

    // ── English CRUD ──

    suspend fun getScenarios(archived: Boolean? = null, category: String? = null): Result<List<NextEnglishScenario>> =
        apiCall {
            val params = mutableListOf<String>()
            archived?.let { params.add("archived=${if (it) 1 else 0}") }
            category?.let { params.add("category=$it") }
            val query = if (params.isNotEmpty()) "?${params.joinToString("&")}" else ""
            val response = httpClient.get("$BASE_URL/api/english/scenarios$query") { authHeader() }
            response.body<NextEnglishListResponse>().items
        }

    suspend fun createScenario(request: NextEnglishCreateRequest): Result<NextEnglishScenario> =
        apiCall {
            val response = httpClient.post("$BASE_URL/api/english/scenarios") {
                authHeader()
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.body<NextEnglishDetailResponse>().item
                ?: throw Exception("创建失败：响应中无 item")
        }

    suspend fun getScenarioById(id: String): Result<NextEnglishScenario> =
        apiCall {
            val response = httpClient.get("$BASE_URL/api/english/scenarios/$id") { authHeader() }
            response.body<NextEnglishDetailResponse>().item
                ?: throw Exception("未找到 scenario: $id")
        }

    suspend fun updateScenario(id: String, request: NextEnglishUpdateRequest): Result<NextEnglishScenario> =
        apiCall {
            val response = httpClient.put("$BASE_URL/api/english/scenarios/$id") {
                authHeader()
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.body<NextEnglishDetailResponse>().item
                ?: throw Exception("更新失败：响应中无 item")
        }

    suspend fun deleteScenario(id: String): Result<Unit> =
        apiCall {
            httpClient.delete("$BASE_URL/api/english/scenarios/$id") { authHeader() }
            Unit
        }

    suspend fun generateScenario(id: String): Result<NextEnglishScenario> =
        apiCall {
            val response = httpClient.post("$BASE_URL/api/english/scenarios/$id/generate") { authHeader() }
            response.body<NextEnglishDetailResponse>().item
                ?: throw Exception("生成失败：响应中无 item")
        }

    suspend fun archiveScenario(id: String): Result<NextEnglishScenario> =
        apiCall {
            val response = httpClient.post("$BASE_URL/api/english/scenarios/$id/archive") { authHeader() }
            response.body<NextEnglishDetailResponse>().item
                ?: throw Exception("归档失败：响应中无 item")
        }

    // ── Trip CRUD ──

    suspend fun getTrips(): Result<List<NextTrip>> =
        apiCall {
            val response = httpClient.get("$BASE_URL/api/trips") { authHeader() }
            response.body<NextTripListResponse>().trips
        }

    suspend fun createTrip(request: NextTripCreateRequest): Result<NextTrip> =
        apiCall {
            val response = httpClient.post("$BASE_URL/api/trips") {
                authHeader()
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.body<NextTripDetailResponse>().trip
                ?: throw Exception("创建失败：响应中无 trip")
        }

    suspend fun getTripById(id: String): Result<NextTrip> =
        apiCall {
            val response = httpClient.get("$BASE_URL/api/trips/$id") { authHeader() }
            response.body<NextTripDetailResponse>().trip
                ?: throw Exception("未找到 trip: $id")
        }

    suspend fun updateTrip(id: String, request: NextTripUpdateRequest): Result<NextTrip> =
        apiCall {
            val response = httpClient.put("$BASE_URL/api/trips/$id") {
                authHeader()
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.body<NextTripDetailResponse>().trip
                ?: throw Exception("更新失败：响应中无 trip")
        }

    suspend fun deleteTrip(id: String): Result<Unit> =
        apiCall {
            httpClient.delete("$BASE_URL/api/trips/$id") { authHeader() }
            Unit
        }

    suspend fun createTripItem(tripId: String, request: NextTripItemCreateRequest): Result<NextTripItem> =
        apiCall {
            val response = httpClient.post("$BASE_URL/api/trips/$tripId/items") {
                authHeader()
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.body<NextTripItemDetailResponse>().item
                ?: throw Exception("创建失败：响应中无 item")
        }

    suspend fun updateTripItem(tripId: String, itemId: String, request: NextTripItemUpdateRequest): Result<NextTripItem> =
        apiCall {
            val response = httpClient.put("$BASE_URL/api/trips/items/$itemId") {
                authHeader()
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.body<NextTripItemDetailResponse>().item
                ?: throw Exception("更新失败：响应中无 item")
        }

    suspend fun deleteTripItem(tripId: String, itemId: String): Result<Unit> =
        apiCall {
            httpClient.delete("$BASE_URL/api/trips/items/$itemId") { authHeader() }
            Unit
        }

    suspend fun uploadTripItemPhoto(itemId: String, uri: Uri, context: Context): Result<NextTripItemPhoto> =
        apiCall {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw Exception("无法读取图片")
            if (bytes.size > 10 * 1024 * 1024) {
                throw Exception("照片大小不能超过10MB")
            }
            val fileName = "photo.${mimeType.substringAfter("/", "jpg")}"

            val token = authProvider.sessionToken.first()
            val response = httpClient.submitFormWithBinaryData(
                url = "$BASE_URL/api/trips/items/$itemId/photos",
                formData = formData {
                    append("photo", bytes, Headers.build {
                        append(HttpHeaders.ContentType, mimeType)
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    })
                }
            ) {
                if (token.isNotBlank()) header("Cookie", "session=$token")
            }
            response.body<NextTripItemPhotoResponse>().photo
                ?: throw Exception("上传失败：响应中无 photo")
        }

    suspend fun deleteTripItemPhoto(photoId: String): Result<Unit> =
        apiCall {
            httpClient.delete("$BASE_URL/api/trips/photos/$photoId") { authHeader() }
            Unit
        }

    // ── Health ──

    suspend fun getHealthCategories(): Result<List<com.ergou.app.data.remote.dto.NextHealthCategory>> =
        apiCall {
            val response = httpClient.get("$BASE_URL/api/health/categories") { authHeader() }
            response.body<com.ergou.app.data.remote.dto.NextHealthCategoryListResponse>().categories
        }

    suspend fun getHealthItems(categoryId: String): Result<List<com.ergou.app.data.remote.dto.NextHealthItem>> =
        apiCall {
            val response = httpClient.get("$BASE_URL/api/health/items?category=$categoryId") { authHeader() }
            response.body<com.ergou.app.data.remote.dto.NextHealthItemListResponse>().items
        }

    suspend fun searchHealth(keyword: String): Result<List<com.ergou.app.data.remote.dto.NextHealthItem>> =
        apiCall {
            val response = httpClient.get("$BASE_URL/api/health/search?q=$keyword") { authHeader() }
            response.body<com.ergou.app.data.remote.dto.NextHealthItemListResponse>().items
        }

    // ── Internal ──

    private suspend fun <T> apiCall(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: io.ktor.client.plugins.ClientRequestException) {
        if (e.response.status.value == 401) {
            Timber.w("[Next] 401 未授权，清除 session")
            authProvider.clearSession()
        }
        Timber.e(e, "[Next] API 请求失败 status=%d", e.response.status.value)
        Result.failure(e)
    } catch (e: Exception) {
        Timber.e(e, "[Next] API 请求异常")
        Result.failure(e)
    }

    private suspend fun io.ktor.client.request.HttpRequestBuilder.authHeader() {
        val token = authProvider.sessionToken.first()
        if (token.isNotBlank()) {
            header("Cookie", "session=$token")
        }
    }
}
