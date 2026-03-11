## Context

后端基础设施已经完整：
- `ModelProvider` enum（DEEPSEEK/CLAUDE）+ DataStore 持久化 — `ApiKeyProvider`
- `ClaudeService: LLMService` — 完整实现（chat、stream、tool calls、chatWithImages）
- `LLMServiceProvider: LLMService` — 动态路由，每次调用读取当前 provider
- Koin 已注册 DeepSeekService、ClaudeService、LLMServiceProvider

唯一缺失的是 **SettingsScreen UI**：当前页面仍然把 API Key 作为独立条目显示在「账号」section，没有模型选择入口。

## Goals / Non-Goals

**Goals:**
- 设置页新增「模型设置」条目 + 弹窗（选择模型 + 管理 Key）
- 从「账号」section 移除 API Key 条目
- 用户可以看到当前使用的模型名称和型号

**Non-Goals:**
- 不改动 ClaudeService / DeepSeekService / LLMServiceProvider（已实现）
- 不改动 ApiKeyProvider / ModelProvider（已实现）
- 不改动 SettingsViewModel 的 data flow 逻辑（已有 modelProvider、hasApiKey、hasClaudeApiKey）
- 不在对话界面显示当前模型（后续可加）
- 不支持自定义模型 ID 或 API endpoint

## Decisions

### 1. 纯 UI 层改动，不动数据层

**决策**：只改 `SettingsScreen.kt`，不改 ViewModel 和数据层。

**理由**：SettingsViewModel 已有 `modelProvider`、`hasApiKey`、`hasClaudeApiKey`、`onModelProviderChanged()`、`onSaveApiKey()`、`onSaveClaudeApiKey()` — 所有需要的状态和方法都在。

### 2. 模型信息硬编码在 UI 层

**决策**：模型列表（名称 + ID）硬编码为 data class list，不做动态注册。

```kotlin
data class ModelOption(
    val provider: ModelProvider,
    val displayName: String,  // "DeepSeek" / "Claude"
    val modelId: String       // "deepseek-chat" / "claude-opus-4-6"
)

val MODEL_OPTIONS = listOf(
    ModelOption(ModelProvider.DEEPSEEK, "DeepSeek", "deepseek-chat"),
    ModelOption(ModelProvider.CLAUDE, "Claude", "claude-opus-4-6")
)
```

**理由**：只有两个模型，没有动态扩展需求。如果未来加模型，改这个 list 即可。

**备选**：把模型信息放到 `ModelProvider` enum 里。但 enum 在 `util/` 包，加 UI 相关字段不合适。

### 3. 弹窗内直接管理 API Key

**决策**：模型选择弹窗里每个选项都有「设置/修改」链接，点击跳转到 Key 输入弹窗。两个弹窗是层叠关系（Key 弹窗关闭后回到模型弹窗）。

**理由**：把 Key 和模型放在一起，用户心智负担最小。不需要在「账号」section 再单独列 API Key。

### 4. ClaudeService 硬编码模型为 claude-sonnet-4

**现状**：`ClaudeService.MODEL = "claude-sonnet-4-20250514"`，UI 显示 `claude-opus-4-6`。

**决策**：将 `ClaudeService.MODEL` 改为 `"claude-opus-4-6"`，与 UI 保持一致。这是唯一的数据层小改动。

**备选**：让 ClaudeService 从外部接收 model 参数。但目前只用一个模型，过度设计。

## Risks / Trade-offs

- **[风险] 模型切换后正在进行的对话** → 切换只影响新请求，不会中断当前流式响应。LLMServiceProvider 每次调用时读取 provider，所以切换是即时但安全的。
- **[权衡] API Key 弹窗复用** → 复用现有的 `ApiKeyEditDialog` 和 `ClaudeApiKeyEditDialog`，改造参数使其支持从模型弹窗调用。代码简单但弹窗层叠体验需要注意状态管理。
