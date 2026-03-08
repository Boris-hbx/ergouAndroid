package com.ergou.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Auth ──

@Serializable
data class NextLoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class NextLoginResponse(
    val success: Boolean = false,
    val message: String? = null,
    val user: NextUser? = null
)

@Serializable
data class NextUser(
    val id: String? = null,
    val username: String? = null
)

// ── 通用响应 ──

@Serializable
data class NextBaseResponse(
    val success: Boolean = false,
    val message: String? = null
)

// ── Todo ──

@Serializable
data class NextTodo(
    val id: String = "",
    val text: String = "",
    val content: String? = null,
    val tab: String = "today",
    val quadrant: String? = null,
    val progress: Int = 0,
    val completed: Boolean = false,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("due_date") val dueDate: String? = null,
    val assignee: String? = null,
    val tags: List<String>? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val changelog: List<NextChangelog>? = null,
    val deleted: Boolean = false,
    @SerialName("deleted_at") val deletedAt: String? = null,
    @SerialName("next_reminder") val nextReminder: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0
)

@Serializable
data class NextChangelog(
    val field: String = "",
    val label: String? = null,
    @SerialName("old_value") val oldValue: String? = null,
    @SerialName("new_value") val newValue: String? = null,
    val timestamp: String? = null
)

@Serializable
data class NextTodoListResponse(
    val success: Boolean = false,
    val items: List<NextTodo> = emptyList()
)

@Serializable
data class NextTodoDetailResponse(
    val success: Boolean = false,
    val item: NextTodo? = null,
    val message: String? = null
)

@Serializable
data class NextTodoCreateRequest(
    val text: String,
    val content: String? = null,
    val tab: String? = null,
    val quadrant: String? = null,
    val tags: List<String>? = null,
    @SerialName("due_date") val dueDate: String? = null
)

@Serializable
data class NextTodoUpdateRequest(
    val text: String? = null,
    val content: String? = null,
    val tab: String? = null,
    val quadrant: String? = null,
    val progress: Int? = null,
    val completed: Boolean? = null,
    val tags: List<String>? = null,
    @SerialName("due_date") val dueDate: String? = null
)

@Serializable
data class NextTodoCountsResponse(
    val success: Boolean = false,
    val counts: Map<String, Int> = emptyMap(),
    val total: Int = 0,
    val completed: Int = 0,
    val pending: Int = 0
)

// ── Routine ──

@Serializable
data class NextRoutine(
    val id: String = "",
    val text: String = "",
    @SerialName("completed_today") val completedToday: Boolean = false,
    @SerialName("last_completed_date") val lastCompletedDate: String? = null,
    @SerialName("is_collaborative") val isCollaborative: Boolean = false,
    @SerialName("owner_name") val ownerName: String? = null,
    @SerialName("owner_id") val ownerId: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class NextRoutineListResponse(
    val success: Boolean = false,
    val items: List<NextRoutine> = emptyList()
)

@Serializable
data class NextRoutineDetailResponse(
    val success: Boolean = false,
    val item: NextRoutine? = null,
    val message: String? = null
)

@Serializable
data class NextRoutineCreateRequest(
    val text: String
)

// ── Review ──

@Serializable
data class NextReview(
    val id: String = "",
    val text: String = "",
    val frequency: String = "monthly",
    @SerialName("frequency_config") val frequencyConfig: NextFrequencyConfig? = null,
    val notes: String? = null,
    val category: String? = null,
    @SerialName("last_completed") val lastCompleted: String? = null,
    val paused: Boolean = false,
    @SerialName("due_status") val dueStatus: String? = null,
    @SerialName("days_until_due") val daysUntilDue: Int? = null,
    @SerialName("due_label") val dueLabel: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class NextFrequencyConfig(
    @SerialName("day_of_week") val dayOfWeek: Int? = null,
    @SerialName("day_of_month") val dayOfMonth: Int? = null,
    val month: Int? = null,
    val day: Int? = null
)

@Serializable
data class NextReviewListResponse(
    val success: Boolean = false,
    val items: List<NextReview> = emptyList()
)

@Serializable
data class NextReviewDetailResponse(
    val success: Boolean = false,
    val item: NextReview? = null,
    val message: String? = null
)

@Serializable
data class NextReviewCreateRequest(
    val text: String,
    val frequency: String = "monthly",
    @SerialName("frequency_config") val frequencyConfig: NextFrequencyConfig? = null,
    val notes: String? = null,
    val category: String? = null
)

@Serializable
data class NextReviewUpdateRequest(
    val text: String? = null,
    val frequency: String? = null,
    @SerialName("frequency_config") val frequencyConfig: NextFrequencyConfig? = null,
    val notes: String? = null,
    val category: String? = null,
    val paused: Boolean? = null
)

// ── Expense ──

@Serializable
data class NextExpenseEntry(
    val id: String = "",
    val amount: Double = 0.0,
    val date: String? = null,
    val notes: String? = null,
    val tags: List<String>? = null,
    @SerialName("ai_processed") val aiProcessed: Boolean = false,
    val currency: String = "CAD",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("photo_count") val photoCount: Int = 0,
    @SerialName("item_count") val itemCount: Int = 0,
    val items: List<NextExpenseItem>? = null
)

@Serializable
data class NextExpenseItem(
    val id: String = "",
    @SerialName("entry_id") val entryId: String = "",
    val name: String = "",
    val quantity: Double = 1.0,
    @SerialName("unit_price") val unitPrice: Double? = null,
    val amount: Double = 0.0,
    val specs: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0
)

@Serializable
data class NextExpenseSummary(
    @SerialName("total_amount") val totalAmount: Double = 0.0,
    @SerialName("entry_count") val entryCount: Int = 0,
    val period: String = "",
    val from: String? = null,
    val to: String? = null,
    @SerialName("tag_totals") val tagTotals: List<NextTagTotal>? = null
)

@Serializable
data class NextTagTotal(
    val tag: String = "",
    val amount: Double = 0.0,
    val count: Int = 0
)

@Serializable
data class NextExpenseListResponse(
    val success: Boolean = false,
    val entries: List<NextExpenseEntry> = emptyList()
)

@Serializable
data class NextExpensePhoto(
    val id: String = "",
    @SerialName("entry_id") val entryId: String = "",
    val filename: String = "",
    @SerialName("storage_path") val storagePath: String? = null,
    @SerialName("file_size") val fileSize: Long = 0,
    @SerialName("mime_type") val mimeType: String = "",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class NextParsePreviewItem(
    val name: String = "",
    val quantity: Double = 1.0,
    @SerialName("unit_price") val unitPrice: Double = 0.0,
    val amount: Double = 0.0,
    val specs: String? = null
)

@Serializable
data class NextParsePreview(
    val merchant: String = "",
    val date: String? = null,
    val currency: String = "CAD",
    val tags: List<String>? = null,
    val items: List<NextParsePreviewItem> = emptyList(),
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val tip: Double = 0.0,
    @SerialName("total_amount") val totalAmount: Double = 0.0
)

@Serializable
data class NextExpenseDetailResponse(
    val success: Boolean = false,
    val entry: NextExpenseEntry? = null,
    val photos: List<NextExpensePhoto> = emptyList(),
    val message: String? = null
)

@Serializable
data class NextExpensePhotoResponse(
    val success: Boolean = false,
    val photo: NextExpensePhoto? = null,
    val message: String? = null
)

@Serializable
data class NextParsePreviewResponse(
    val success: Boolean = false,
    val preview: NextParsePreview? = null,
    val message: String? = null
)

@Serializable
data class NextParsePreviewImage(
    val data: String,
    @SerialName("mime_type") val mimeType: String = "image/jpeg"
)

@Serializable
data class NextParsePreviewRequest(
    val images: List<NextParsePreviewImage> = emptyList(),
    val text: String? = null
)

@Serializable
data class NextExpenseSummaryResponse(
    val success: Boolean = false,
    val summary: NextExpenseSummary? = null
)

@Serializable
data class NextExpenseTagsResponse(
    val success: Boolean = false,
    val tags: List<String> = emptyList()
)

@Serializable
data class NextExpenseCreateRequest(
    val amount: Double,
    val date: String? = null,
    val notes: String? = null,
    val tags: List<String>? = null,
    val currency: String? = null,
    val items: List<NextExpenseItemRequest>? = null
)

@Serializable
data class NextExpenseItemRequest(
    val name: String,
    val quantity: Double = 1.0,
    @SerialName("unit_price") val unitPrice: Double? = null,
    val amount: Double = 0.0,
    val specs: String? = null
)

@Serializable
data class NextExpenseUpdateRequest(
    val amount: Double? = null,
    val date: String? = null,
    val notes: String? = null,
    val tags: List<String>? = null,
    val currency: String? = null
)

// ── English ──

@Serializable
data class NextEnglishScenario(
    val id: String = "",
    val title: String = "",
    @SerialName("title_en") val titleEn: String? = null,
    val description: String? = null,
    val icon: String? = null,
    val content: String? = null,
    val status: String = "draft",
    val archived: Boolean = false,
    val category: String = "\u82F1\u8BED",
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class NextEnglishListResponse(
    val success: Boolean = false,
    val items: List<NextEnglishScenario> = emptyList()
)

@Serializable
data class NextEnglishDetailResponse(
    val success: Boolean = false,
    val item: NextEnglishScenario? = null,
    val message: String? = null
)

@Serializable
data class NextEnglishCreateRequest(
    val title: String,
    @SerialName("title_en") val titleEn: String? = null,
    val description: String? = null,
    val icon: String? = null,
    val category: String = "\u82F1\u8BED",
    val notes: String? = null
)

@Serializable
data class NextEnglishUpdateRequest(
    val title: String? = null,
    @SerialName("title_en") val titleEn: String? = null,
    val description: String? = null,
    val icon: String? = null,
    val content: String? = null,
    val category: String? = null,
    val notes: String? = null,
    val status: String? = null
)

// ── Trip ──

@Serializable
data class NextTrip(
    val id: String = "",
    val title: String = "",
    val destination: String? = null,
    @SerialName("date_from") val dateFrom: String? = null,
    @SerialName("date_to") val dateTo: String? = null,
    val purpose: String? = null,
    val notes: String? = null,
    val currency: String = "CAD",
    @SerialName("item_count") val itemCount: Int = 0,
    @SerialName("total_amount") val totalAmount: Double = 0.0,
    @SerialName("reimburse_summary") val reimburseSummary: NextReimburseSummary? = null,
    val items: List<NextTripItem>? = null,
    @SerialName("is_owner") val isOwner: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class NextReimburseSummary(
    val total: Int = 0,
    val pending: Int = 0,
    val submitted: Int = 0,
    val approved: Int = 0,
    val rejected: Int = 0,
    val na: Int = 0
)

@Serializable
data class NextTripItem(
    val id: String = "",
    @SerialName("trip_id") val tripId: String = "",
    val type: String = "misc",
    val date: String? = null,
    val description: String = "",
    val amount: Double = 0.0,
    val currency: String = "CAD",
    @SerialName("reimburse_status") val reimburseStatus: String = "pending",
    val notes: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("photo_count") val photoCount: Int = 0,
    val photos: List<NextTripItemPhoto> = emptyList(),
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class NextTripListResponse(
    val success: Boolean = false,
    val trips: List<NextTrip> = emptyList()
)

@Serializable
data class NextTripDetailResponse(
    val success: Boolean = false,
    val trip: NextTrip? = null,
    val message: String? = null
)

@Serializable
data class NextTripCreateRequest(
    val title: String,
    val destination: String? = null,
    @SerialName("date_from") val dateFrom: String? = null,
    @SerialName("date_to") val dateTo: String? = null,
    val purpose: String? = null,
    val notes: String? = null,
    val currency: String? = null
)

@Serializable
data class NextTripUpdateRequest(
    val title: String? = null,
    val destination: String? = null,
    @SerialName("date_from") val dateFrom: String? = null,
    @SerialName("date_to") val dateTo: String? = null,
    val purpose: String? = null,
    val notes: String? = null,
    val currency: String? = null
)

@Serializable
data class NextTripItemCreateRequest(
    val type: String = "misc",
    val date: String? = null,
    val description: String,
    val amount: Double,
    val currency: String? = null,
    @SerialName("reimburse_status") val reimburseStatus: String? = null,
    val notes: String? = null
)

@Serializable
data class NextTripItemUpdateRequest(
    val type: String? = null,
    val date: String? = null,
    val description: String? = null,
    val amount: Double? = null,
    val currency: String? = null,
    @SerialName("reimburse_status") val reimburseStatus: String? = null,
    val notes: String? = null
)

@Serializable
data class NextTripItemDetailResponse(
    val success: Boolean = false,
    val item: NextTripItem? = null,
    val message: String? = null
)

@Serializable
data class NextTripItemPhoto(
    val id: String = "",
    @SerialName("item_id") val itemId: String = "",
    val filename: String = "",
    @SerialName("storage_path") val storagePath: String? = null,
    @SerialName("file_size") val fileSize: Long = 0,
    @SerialName("mime_type") val mimeType: String = "",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class NextTripItemPhotoResponse(
    val success: Boolean = false,
    val photo: NextTripItemPhoto? = null,
    val message: String? = null
)

// ── Health ──

@Serializable
data class NextHealthCategory(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val intro: String = ""
)

@Serializable
data class NextHealthMeridianDetail(
    val name: String = "",
    val intensity: String = "primary",
    val note: String = ""
)

@Serializable
data class NextHealthInsight(
    val label: String = "",
    val content: String = ""
)

@Serializable
data class NextHealthItem(
    val id: String = "",
    @SerialName("category_id") val categoryId: String = "",
    val name: String = "",
    val description: String = "",
    val benefits: String = "",
    val meridians: List<String> = emptyList(),
    @SerialName("key_points") val keyPoints: List<String> = emptyList(),
    @SerialName("benefits_list") val benefitsList: List<String> = emptyList(),
    @SerialName("meridian_details") val meridianDetails: List<NextHealthMeridianDetail> = emptyList(),
    val insights: List<NextHealthInsight> = emptyList(),
    @SerialName("video_url") val videoUrl: String = ""
)

@Serializable
data class NextHealthCategoryListResponse(
    val success: Boolean = false,
    val categories: List<NextHealthCategory> = emptyList()
)

@Serializable
data class NextHealthItemListResponse(
    val success: Boolean = false,
    val items: List<NextHealthItem> = emptyList()
)

// ── Quadrant 映射 ──

object QuadrantMapper {
    private val toLabel = mapOf(
        "urgent-important" to "重要紧急",
        "not-urgent-important" to "重要不紧急",
        "urgent-not-important" to "不重要紧急",
        "not-urgent-not-important" to "不重要不紧急"
    )
    private val fromLabel = toLabel.entries.associate { (k, v) -> v to k }

    fun toChineseLabel(quadrant: String?): String =
        quadrant?.let { toLabel[it] } ?: ""

    fun fromChineseLabel(label: String): String? =
        fromLabel[label]

    fun fromPriority(priority: Int): String = when (priority) {
        1 -> "urgent-important"
        2 -> "not-urgent-important"
        3 -> "urgent-not-important"
        else -> "not-urgent-not-important"
    }
}
