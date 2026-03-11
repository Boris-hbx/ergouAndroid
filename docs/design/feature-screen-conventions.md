# 功能页面通用设计规范

> 所有功能页面（任务、习惯、记账、差旅、学习、养生）必须遵循本规范。
> 各功能的独立设计文档应在此基础上扩展，不得与本规范冲突。

---

## 1. 架构分层

每个功能模块包含以下文件：

```
ui/<feature>/
├── <Feature>Screen.kt      # 页面 Composable
└── <Feature>ViewModel.kt   # 状态管理

data/remote/dto/
└── NextModels.kt            # DTO（已有，共用）

data/remote/api/
└── NextApiService.kt        # API 调用（已有，共用）

data/tool/tools/
└── <Action><Feature>Tool.kt # 二狗工具（每个操作一个文件）
```

### 数据流

```
UI (Screen) → ViewModel → NextApiService → Next 后端
                              ↕
Tool (二狗调用) → NextApiService → Next 后端
```

- ViewModel 和 Tool 都通过 `NextApiService` 操作数据，**不直接访问 Room**
- 养生模块例外：使用静态数据，无 API 调用

---

## 2. UI 布局规范

### 2.1 页面骨架

所有功能页面统一使用：

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <Feature>Screen(
    onBack: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: <Feature>ViewModel = koinViewModel()
) {
    Scaffold(
        topBar = { /* TopAppBar */ },
        floatingActionButton = { /* FAB（如需要）*/ },
        snackbarHost = { /* SnackbarHost */ }
    ) { padding ->
        // 内容区
    }
}
```

### 2.2 TopAppBar

```kotlin
TopAppBar(
    title = { Text("<功能名称>") },
    navigationIcon = {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
        }
    }
)
```

- 标题：功能中文名（任务、习惯、记账、差旅、学习、养生）
- 左侧：返回箭头
- 右侧 actions：按需添加（筛选、搜索等）

### 2.3 内容状态

每个页面必须处理 4 种状态：

| 状态 | UI 表现 |
|------|--------|
| **加载中** | 居中 `CircularProgressIndicator` |
| **未登录** | 居中提示 + "去设置" 按钮（养生模块无需登录） |
| **空数据** | 居中图标 + 提示文字 + 引导操作 |
| **有数据** | `LazyColumn` 列表 |

### 2.4 列表项

- 使用 `Card` + `CardDefaults.cardColors(surfaceVariant)`
- 内边距 `16.dp`
- 列表间距 `8.dp`
- 操作按钮（删除等）放右侧，使用 `IconButton`
- 列表必须指定 `key` 参数

### 2.5 FAB

- 有写入操作的页面显示 FAB
- 图标：`Icons.Default.Add`
- 未登录时隐藏 FAB
- 点击弹出 `AlertDialog` 输入表单

### 2.6 对话框（CRUD）

- 使用 `AlertDialog` 进行新增/编辑操作
- 表单字段使用 `OutlinedTextField`
- 文本输入用 `TextFieldValue`（避免中文 IME 冲突）
- 确认按钮文字："添加" / "保存"
- 取消按钮文字："取消"
- 删除确认用单独的 `AlertDialog`

---

## 3. 状态管理规范

### 3.1 UiState

```kotlin
data class <Feature>UiState(
    val items: List<XxxDto> = emptyList(),  // 主数据列表
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = false,
    val error: String? = null
    // ... 功能特有字段
)
```

必须字段：`isLoading`、`isLoggedIn`、`error`

### 3.2 ViewModel

```kotlin
class <Feature>ViewModel(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : ViewModel() {
    private val _uiState = MutableStateFlow(<Feature>UiState())
    val uiState: StateFlow<<Feature>UiState>> = _uiState.asStateFlow()

    init { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val data = nextApiService.getXxx()
                _uiState.update { it.copy(items = data, isLoading = false, isLoggedIn = true) }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun handleError(e: Exception) {
        if (e.message?.contains("401") == true || e.message?.contains("Unauthorized") == true) {
            _uiState.update { it.copy(isLoggedIn = false, isLoading = false) }
        } else {
            _uiState.update { it.copy(error = e.message, isLoading = false) }
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
}
```

### 3.3 Koin 注册

```kotlin
// AppModule.kt
viewModel { <Feature>ViewModel(nextApiService = get(), authProvider = get()) }
```

---

## 4. 工具（Tool）集成规范

> 核心原则：**每个功能页面的主要操作都应暴露为 Tool，让二狗能通过对话调用。**

### 4.1 Tool 命名

```
动作_功能名   →  Tool 类名
add_task     →  AddTaskTool
list_tasks   →  ListTasksTool
complete_task → CompleteTaskTool
```

### 4.2 每个功能至少暴露的 Tool

| 操作 | Tool 名称模式 | 说明 |
|------|-------------|------|
| 查询 | `Query<Feature>Tool` / `List<Feature>sTool` | 查询/列表 |
| 新增 | `Add<Feature>Tool` | 创建新记录 |
| 完成/切换 | `Complete<Feature>Tool` / `Check<Feature>Tool` | 状态变更 |
| 删除 | — | 删除操作暂不暴露为 Tool，避免误操作 |

### 4.3 Tool 实现模板

```kotlin
class Add<Feature>Tool(
    private val nextApiService: NextApiService
) : Tool {
    override val name = "add_<feature>"
    override val description = "添加<功能描述>"
    override val parameters = mapOf(
        "field1" to ToolParam("string", "字段1描述", required = true),
        "field2" to ToolParam("string", "字段2描述", required = false)
    )

    override suspend fun execute(args: Map<String, Any?>): String {
        return try {
            val result = nextApiService.createXxx(...)
            "已添加：${result.title}"
        } catch (e: Exception) {
            throw ToolException("add_<feature>", "添加失败: ${e.message}", e)
        }
    }
}
```

### 4.4 Tool 注册

在 `AppModule.kt` 的 ToolExecutor 构建中注册：

```kotlin
single {
    ToolExecutor(
        tools = listOf(
            // ... 已有工具
            Add<Feature>Tool(get()),
            Query<Feature>Tool(get())
        )
    )
}
```

### 4.5 Prompt 集成

在 `ErgouPrompt.kt` 的系统提示中，描述二狗可以用这些工具做什么，例如：
- "用户说'帮我记一笔'，调用 add_expense"
- "用户说'今天花了多少'，调用 expense_summary"

---

## 5. 快捷栏集成

> 功能页面的关键数据可以推送到主页快捷栏（ShortcutBar）展示。

### 5.1 现有快捷栏机制

`ShortcutBarRepository` 提供快捷条数据，当前支持的 route：
- `task` — 待办
- `routine` — 习惯
- `expense` — 记账
- `trip` — 差旅

### 5.2 快捷栏数据更新

每个功能的 ViewModel 在数据变更后，应通知快捷栏刷新：

```kotlin
// 示例：任务完成后刷新快捷栏显示的待办数量
private val shortcutBarRepository: ShortcutBarRepository
// ... 在数据变更操作后调用
shortcutBarRepository.refresh()
```

### 5.3 扩展点（预留）

未来可扩展：
- 快捷栏显示摘要数据（"今日待办 3 项"、"本月已花 ¥2,340"）
- 长按快捷栏项展开更多操作

---

## 6. 通知/提醒预留

### 6.1 设计原则

- 通知通过二狗的提醒系统（`set_reminder` Tool + AlarmManager）触发
- 功能页面本身不直接发通知
- 但功能数据可以触发二狗主动提醒逻辑

### 6.2 预留接口

每个功能的 ViewModel 可提供 "需要提醒" 的数据：

```kotlin
// 预留：返回需要提醒的项目
fun getPendingReminders(): List<ReminderCandidate> {
    // 例如：今日未完成的任务、即将到期的回顾、连续3天未打卡的习惯
}
```

### 6.3 未来集成方式

```
二狗每日定时检查 → 各功能 getPendingReminders()
→ 生成自然语言提醒 → 推送通知
```

---

## 7. 分享功能预留

### 7.1 设计原则

- 当前不实现分享 UI
- 但数据模型和 ViewModel 预留 `toShareText()` 方法

### 7.2 预留接口

```kotlin
// DTO 扩展函数，预留
fun XxxDto.toShareText(): String {
    // 例如："✅ 今日待办完成 5/7 项\n- 买菜 ✓\n- 开会 ✓\n..."
}
```

### 7.3 未来实现

```kotlin
// 未来在页面中添加分享按钮
val context = LocalContext.current
IconButton(onClick = {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, data.toShareText())
    }
    context.startActivity(Intent.createChooser(intent, "分享"))
})
```

---

## 8. 导航注册

### 8.1 路由命名

路由名使用小写英文，与功能广场中的 `FeatureItem.route` 一致：

| 功能 | 路由 | 功能广场名称 |
|------|------|------------|
| 任务 | `task` | 任务 |
| 习惯 | `routine` | 习惯 |
| 记账 | `expense` | 记账 |
| 差旅 | `trip` | 差旅 |
| 学习 | `english` | 学习 |
| 养生 | `health` | 养生 |
| 回顾 | `review` | 回顾 |

### 8.2 ErgouNavigation 注册

```kotlin
composable("<route>") {
    <Feature>Screen(
        onBack = { navController.popBackStack() },
        onNavigateToSettings = { navController.navigate("settings") }
    )
}
```

---

## 9. 测试规范

### 9.1 ViewModel 单元测试

```kotlin
class <Feature>ViewModelTest {
    // 测试加载成功
    fun loadData_success_updatesState()
    // 测试加载失败（未登录）
    fun loadData_unauthorized_setsNotLoggedIn()
    // 测试 CRUD 操作
    fun addItem_success_refreshesList()
}
```

### 9.2 Tool 单元测试

```kotlin
class Add<Feature>ToolTest {
    // 测试正常执行
    fun execute_validArgs_returnsSuccess()
    // 测试缺少必填参数
    fun execute_missingRequired_throwsToolException()
    // 测试 API 错误
    fun execute_apiError_throwsToolException()
}
```

---

## 10. Checklist

每个功能页面开发完成后，对照此清单验证：

- [ ] Screen：4 种状态（加载/未登录/空数据/有数据）都正确显示
- [ ] Screen：TopAppBar 有返回按钮，标题正确
- [ ] Screen：FAB 在未登录时隐藏
- [ ] Screen：新增/编辑对话框使用 TextFieldValue
- [ ] Screen：列表使用 LazyColumn + key
- [ ] Screen：错误通过 Snackbar 展示
- [ ] ViewModel：UiState 包含 isLoading/isLoggedIn/error
- [ ] ViewModel：401 错误正确处理为未登录状态
- [ ] Tool：主要操作暴露为 Tool（至少查询 + 新增）
- [ ] Tool：在 AppModule 的 ToolExecutor 中注册
- [ ] Tool：在 ErgouPrompt 中添加使用说明
- [ ] Navigation：在 ErgouNavigation 中注册路由
- [ ] Navigation：功能广场可跳转
- [ ] Koin：ViewModel 和 Tool 在 AppModule 中注册
