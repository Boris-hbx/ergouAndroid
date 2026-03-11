# 二狗 (Ergou) 工程规范

> **你是二狗A**（Android 端 Agent）。二狗令在 `C:\Project\ergouPM\docs\agent-board.md`，看到 `@二狗A` 就是叫你接令。任务看板在 `C:\Project\ergouPM\docs\taskboard.md`。

Claude Code 每次对话自动加载此文件，所有代码修改必须遵循以下规范。

## 项目概览
- Android 个人 AI 助手，包名 `com.ergou.app`
- 跨端任务见 `C:\Project\ergouPM\sync-log.md`
- 架构：MVVM (ViewModel → Repository → DAO/LLMService)
- 技术栈：Compose + Material 3, Room, Ktor, Koin, KSP, DataStore, Timber
- LLM：DeepSeek API（可插拔 LLMService 接口）

### 项目目录结构
```
ergou/
├── app/                # Android 源码
├── docs/               # 项目文档
│   ├── design/         #   设计文档（活文档，持续更新）
│   └── reference/      #   参考资料（只读归档）
├── workflow/           # 开发流程 (openspec)
│   ├── backlog.md      #   功能 Backlog
│   ├── changes/        #   进行中的功能变更
│   └── templates/      #   流程模板
└── pic/                # 截图
```

## 1. 代码风格与命名

### 包结构
```
com.ergou.app/
├── data/           # 数据层
│   ├── local/      # Room (dao/, entity/, database/)
│   ├── remote/     # API (dto/, service/)
│   └── tool/       # Tool Use (tools/)
├── ui/             # 界面层 (各 feature 子包)
├── di/             # Koin 模块
└── util/           # 工具类
```

### 命名规则
| 类型 | 后缀 | 示例 |
|------|------|------|
| 数据库实体 | Entity | `MemoryEntity` |
| DAO | Dao | `MemoryDao` |
| ViewModel | ViewModel | `ChatViewModel` |
| 接口实现 | Impl / 具体名 | `DeepSeekService` |
| Compose 页面 | Screen | `ChatScreen` |
| 工具 | Tool | `TranslateTool` |
| 自定义异常 | Exception | `ApiException` |

### 文件组织
- 一个公开类一个文件，文件名 = 类名
- 常量放 `companion object`
- Kotlin 官方代码风格（`kotlin.code.style=official` 已配置）

## 2. 日志规范

- **统一使用 Timber**，禁止 `android.util.Log`
- 日志格式：`[模块] 操作描述 key=value`
  ```kotlin
  Timber.d("[Chat] 发送消息 sessionId=%s len=%d", sessionId, text.length)
  ```
- 分级标准：
  - `Timber.d` — 调试信息（流程跟踪、状态变化）
  - `Timber.w` — 可恢复的异常（网络超时重试、数据降级）
  - `Timber.e(e, "...")` — 不可恢复的错误，**必须附带异常对象**
- **禁止日志输出敏感信息**：API Key、用户隐私数据、完整 prompt 内容

## 3. 错误处理规范

### 自定义异常体系
位于 `util/Exceptions.kt`：
- `ErgouException` — 基类
- `ApiException(message, cause, statusCode)` — API/网络错误
- `ToolException(toolName, message, cause)` — 工具执行错误
- `StorageException(message, cause)` — 数据库/DataStore 错误

### 分层处理
- **Repository 层**：catch 底层异常，转换为业务异常（ErgouException 子类）
- **ViewModel 层**：catch 业务异常，更新 UI 状态（错误提示）
- **UI 层**：只展示状态，不做异常处理

### 网络重试
- 网络请求失败时指数退避重试，最多 3 次
- 退避间隔：1s → 2s → 4s
- 非幂等操作（如发消息）不重试

## 4. API/网络规范

- LLMService 接口 + 具体实现，保持可插拔
- 超时配置集中管理（当前 120s），在 HttpClient 配置中统一设置
- DTO 类用 `@Serializable`，Json 配置：
  ```kotlin
  Json {
      ignoreUnknownKeys = true
      explicitNulls = false  // DeepSeek 不接受 null 字段
  }
  ```
- SSE 流式解析使用统一模式，逐行读取 `data:` 前缀

## 5. 安全规范

- API Key 存 DataStore，**禁止硬编码**
- 用户数据全部本地存储，不上传任何服务器
- 所有用户输入注入 prompt 前，必须经过 `PromptSanitizer.sanitize()`
- 禁止日志输出敏感信息（同日志规范）
- Room 数据库未来计划加密（SQLCipher）

## 6. 测试规范

- ViewModel：JUnit + MockK 单元测试
- Repository：Room in-memory 数据库测试
- Tool：单元测试验证输入输出和异常处理
- 测试文件与源文件同包名，放 `test/` 或 `androidTest/`
- 测试方法命名：`methodName_condition_expectedResult`

## 7. 性能规范

- Compose：用 `remember` / `derivedStateOf` 避免不必要的 recomposition
- 列表使用 `LazyColumn` + `key` 参数
- 数据库操作在 `Dispatchers.IO`
- 流式响应 UI 更新做节流，避免逐字符触发 recomposition
- 图片加载使用 Coil，配置内存和磁盘缓存

## 8. 状态管理规范

- ViewModel 内部 `MutableStateFlow`，对外暴露 `StateFlow`
- UI 层使用 `collectAsStateWithLifecycle()` 收集状态
- 输入框状态用 `TextFieldValue` 本地管理，**不同步到 StateFlow**（避免中文 IME 冲突）
- 一次性事件（导航、Toast）用 `Channel` / `SharedFlow`

## 9. 依赖注入规范

- 使用 Koin，模块按功能划分
- ViewModel 用 `viewModel { }` 或 `viewModelOf(::XxxViewModel)`
- Repository / Service 用 `single { }` 或 `singleOf(::XxxRepository)`
- 禁止在 Compose 函数中直接创建 ViewModel 或 Repository

## 10. Git 规范

- 提交信息格式：`type: 简短描述`
- type：feat / fix / refactor / docs / chore / test
- 中文描述，简洁明了
- 示例：`feat: 添加翻译工具`、`fix: 修复中文输入法冲突`
