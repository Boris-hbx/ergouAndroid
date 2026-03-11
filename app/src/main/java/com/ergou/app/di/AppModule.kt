package com.ergou.app.di

import androidx.room.Room
import com.ergou.app.data.local.database.ErgouDatabase
import com.ergou.app.data.remote.api.ClaudeService
import com.ergou.app.data.remote.api.DeepSeekService
import com.ergou.app.data.remote.api.LLMService
import com.ergou.app.data.remote.api.LLMServiceProvider
import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.repository.ChatRepository
import com.ergou.app.data.repository.ChatRepositoryImpl
import com.ergou.app.data.repository.MemoryExtractor
import com.ergou.app.data.repository.ShortcutBarRepository
import com.ergou.app.data.repository.MemoryRepository
import com.ergou.app.data.repository.MemoryRepositoryImpl
import com.ergou.app.data.repository.SoulRepository
import com.ergou.app.data.repository.SoulRepositoryImpl
import com.ergou.app.data.repository.SuggestionEngine
import com.ergou.app.data.tool.ToolExecutor
import com.ergou.app.data.tool.ToolRegistry
import com.ergou.app.data.tool.tools.AddExpenseTool
import com.ergou.app.data.tool.tools.AddReviewTool
import com.ergou.app.data.tool.tools.AddTaskTool
import com.ergou.app.data.tool.tools.AddTripItemTool
import com.ergou.app.data.tool.tools.CompleteTaskTool
import com.ergou.app.data.tool.tools.CreateTripTool
import com.ergou.app.data.tool.tools.ExpenseSummaryTool
import com.ergou.app.data.tool.tools.GenerateEnglishScenarioTool
import com.ergou.app.data.tool.tools.GetDateTimeTool
import com.ergou.app.data.tool.tools.ListTasksTool
import com.ergou.app.data.tool.tools.QueryReviewsTool
import com.ergou.app.data.tool.tools.QueryTripsTool
import com.ergou.app.data.tool.tools.SaveMemoryTool
import com.ergou.app.data.tool.tools.SearchMemoryTool
import com.ergou.app.data.tool.tools.SetReminderTool
import com.ergou.app.data.tool.tools.SimpleCalculateTool
import com.ergou.app.data.tool.tools.TranslateTool
import com.ergou.app.data.tool.tools.DeleteExpenseTool
import com.ergou.app.data.tool.tools.DeleteTaskTool
import com.ergou.app.data.tool.tools.QueryExpensesTool
import com.ergou.app.data.tool.tools.QueryHealthTool
import com.ergou.app.data.tool.tools.QueryScenariosTool
import com.ergou.app.data.tool.tools.RecommendHealthTool
import com.ergou.app.data.tool.tools.TripSummaryTool
import com.ergou.app.data.tool.tools.UpdateTaskTool
import com.ergou.app.data.tool.tools.UpdateTripTool
import com.ergou.app.data.tool.tools.DeleteTripTool
import com.ergou.app.data.tool.tools.UpdateTripItemTool
import com.ergou.app.data.tool.tools.DeleteTripItemTool
import com.ergou.app.data.tool.tools.UpdateScenarioTool
import com.ergou.app.data.tool.tools.DeleteScenarioTool
import com.ergou.app.data.tool.tools.WebSearchTool
import com.ergou.app.ui.chat.ChatViewModel
import com.ergou.app.ui.english.EnglishViewModel
import com.ergou.app.ui.expense.ExpenseDetailViewModel
import com.ergou.app.ui.expense.ExpenseViewModel
import com.ergou.app.ui.health.HealthViewModel
import com.ergou.app.ui.memory.MemoryViewModel
import com.ergou.app.ui.routine.RoutineViewModel
import com.ergou.app.ui.settings.SettingsViewModel
import com.ergou.app.ui.soul.SoulViewModel
import com.ergou.app.ui.task.TaskViewModel
import com.ergou.app.ui.trip.TripViewModel
import com.ergou.app.util.ApiKeyProvider
import com.ergou.app.util.NextAuthProvider
import androidx.datastore.preferences.preferencesDataStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import android.content.Context
import timber.log.Timber

private val Context.healthDataStore by preferencesDataStore(name = "health_prefs")

val appModule = module {
    // ApiKey Provider
    single { ApiKeyProvider(androidContext()) }

    // Next Auth Provider
    single { NextAuthProvider(androidContext()) }

    // Room Database
    single {
        Room.databaseBuilder(
            androidContext(),
            ErgouDatabase::class.java,
            "ergou.db"
        ).addMigrations(ErgouDatabase.MIGRATION_4_5, ErgouDatabase.MIGRATION_5_6, ErgouDatabase.MIGRATION_6_7, ErgouDatabase.MIGRATION_7_8).build()
    }
    single { get<ErgouDatabase>().sessionDao() }
    single { get<ErgouDatabase>().messageDao() }
    single { get<ErgouDatabase>().memoryDao() }
    single { get<ErgouDatabase>().personDao() }
    single { get<ErgouDatabase>().reminderDao() }
    single { get<ErgouDatabase>().soulDao() }

    // Ktor HttpClient
    single {
        HttpClient(OkHttp) {
            install(HttpTimeout) {
                requestTimeoutMillis = 120_000  // 2分钟，LLM非流式请求需要更长时间
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 120_000
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = false
                    explicitNulls = false   // 不序列化null字段，避免API拒绝
                    encodeDefaults = true
                })
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Timber.tag("Ktor").d(message)
                    }
                }
                level = LogLevel.HEADERS
            }
        }
    }

    // LLM Services
    single { DeepSeekService(httpClient = get(), apiKeyProvider = get()) }
    single { ClaudeService(httpClient = get(), apiKeyProvider = get()) }
    single<LLMService> { LLMServiceProvider(deepSeekService = get(), claudeService = get(), apiKeyProvider = get()) }

    // Next API Service
    single { NextApiService(httpClient = get(), authProvider = get()) }

    // Tool System
    single {
        ToolRegistry().apply {
            register(GetDateTimeTool())
            register(SimpleCalculateTool())
            register(SaveMemoryTool(get()))
            register(SearchMemoryTool(get()))
            register(SetReminderTool(get(), androidContext()))
            register(TranslateTool(get()))
            register(WebSearchTool())
            // 功能广场工具
            register(AddTaskTool(get(), get()))
            register(ListTasksTool(get(), get()))
            register(CompleteTaskTool(get(), get()))
            register(AddReviewTool(get(), get()))
            register(QueryReviewsTool(get(), get()))
            register(AddExpenseTool(get(), get()))
            register(ExpenseSummaryTool(get(), get()))
            register(GenerateEnglishScenarioTool(get(), get()))
            register(CreateTripTool(get(), get()))
            register(QueryTripsTool(get(), get()))
            register(AddTripItemTool(get(), get()))
            // 新增工具
            register(UpdateTaskTool(get(), get()))
            register(DeleteTaskTool(get(), get()))
            register(DeleteExpenseTool(get(), get()))
            register(QueryExpensesTool(get(), get()))
            register(TripSummaryTool(get(), get()))
            register(UpdateTripTool(get(), get()))
            register(DeleteTripTool(get(), get()))
            register(UpdateTripItemTool(get(), get()))
            register(DeleteTripItemTool(get(), get()))
            register(QueryScenariosTool(get(), get()))
            register(UpdateScenarioTool(get(), get()))
            register(DeleteScenarioTool(get(), get()))
            register(QueryHealthTool(get()))
            register(RecommendHealthTool(get()))
        }
    }
    single { ToolExecutor(llmService = get(), toolRegistry = get()) }

    // Repositories
    single { ShortcutBarRepository(androidContext(), get(), get()) }
    single<ChatRepository> { ChatRepositoryImpl(sessionDao = get(), messageDao = get(), llmService = get()) }
    single<MemoryRepository> { MemoryRepositoryImpl(memoryDao = get(), personDao = get()) }
    single { MemoryExtractor(llmService = get(), memoryRepository = get()) }
    single<SoulRepository> { SoulRepositoryImpl(soulDao = get(), nextApiService = get(), nextAuthProvider = get()) }
    single { SuggestionEngine(memoryRepository = get(), nextApiService = get(), nextAuthProvider = get()) }
    single { com.ergou.app.data.repository.ReceiptAnalyzer(llmService = get()) }

    // ViewModels
    viewModel { ChatViewModel(chatRepository = get(), memoryRepository = get(), toolExecutor = get(), apiKeyProvider = get(), memoryExtractor = get(), soulRepository = get(), shortcutBarRepository = get(), llmService = get(), suggestionEngine = get()) }
    viewModel { MemoryViewModel(memoryRepository = get()) }
    viewModel { SettingsViewModel(apiKeyProvider = get(), memoryRepository = get(), nextAuthProvider = get(), nextApiService = get(), soulRepository = get()) }
    viewModel { SoulViewModel(soulRepository = get()) }
    viewModel { TaskViewModel(nextApiService = get(), authProvider = get()) }
    viewModel { RoutineViewModel(nextApiService = get(), authProvider = get()) }
    viewModel { ExpenseViewModel(nextApiService = get(), authProvider = get(), appContext = get(), receiptAnalyzer = get()) }
    viewModel { params -> ExpenseDetailViewModel(expenseId = params.getOrNull(), nextApiService = get(), appContext = androidContext(), receiptAnalyzer = get()) }
    viewModel { EnglishViewModel(nextApiService = get(), authProvider = get()) }
    viewModel { TripViewModel(nextApiService = get(), authProvider = get()) }
    viewModel { HealthViewModel(dataStore = androidContext().healthDataStore, nextApiService = get(), appContext = get()) }

    // Proactive Notification
    single { com.ergou.app.data.repository.ProactiveNotifier(nextApiService = get(), authProvider = get(), soulRepository = get(), llmService = get()) }
}
