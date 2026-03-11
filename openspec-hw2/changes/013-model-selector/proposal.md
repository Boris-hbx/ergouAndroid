## Why

设置页面没有模型选择功能，用户看不出当前对话用的是哪个模型。系统已经收集了 Claude API Key，但没有对应的 Service 实现，也没有切换入口。需要一个全局模型选择器，让用户选定后默认不变。

## What Changes

- 设置页面新增「当前模型」选择项，显示模型型号（如 `deepseek-chat`、`claude-opus-4-6`），选定后持久化
- 新增 `ClaudeService: LLMService`，对接 Claude API（流式 SSE），复用已有的 Claude API Key
- `ChatRequest.model` 不再硬编码，由选中的模型决定
- Koin 注入改为根据当前选择动态提供对应的 `LLMService` 实现
- ApiKeyProvider 新增 `selectedModel` 偏好存储

## Capabilities

### New Capabilities
- `model-selector`: 全局模型选择与持久化，设置页 UI，ClaudeService 实现，动态 LLMService 路由

### Modified Capabilities
（无现有 spec 需要修改）

## Impact

- **数据层**: ApiKeyProvider 新增 `selectedModel` DataStore 字段；新增 `ClaudeService` 类
- **DI**: AppModule 从 `single<LLMService>` 改为动态工厂模式
- **UI**: SettingsScreen 新增模型选择卡片；SettingsViewModel 新增选择/切换逻辑
- **DTO**: ChatRequest 默认 model 参数需由外部传入而非硬编码
- **依赖**: 无新增第三方依赖，Claude API 用现有 Ktor HttpClient
