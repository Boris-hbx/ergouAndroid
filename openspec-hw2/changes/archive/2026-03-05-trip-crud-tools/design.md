## Context

差旅功能已有 4 个 Tool（CreateTripTool, AddTripItemTool, QueryTripsTool, TripSummaryTool），NextApiService 中也已实现 updateTrip/deleteTrip/updateTripItem/deleteTripItem 方法。本次只需新增 4 个 Tool 类来桥接 LLM 调用与已有 API。

## Goals / Non-Goals

**Goals:**
- 新增 UpdateTripTool、DeleteTripTool、UpdateTripItemTool、DeleteTripItemTool 4 个文件
- 完全复用现有 Tool 模式和 NextApiService 接口

**Non-Goals:**
- 不修改任何现有文件（AppModule、ErgouPrompt、NextApiService 等由总管统一修改）
- 不涉及 UI 变更、数据库变更、新依赖引入

## Decisions

### 1. 完全复用现有 Tool 模式
沿用 CreateTripTool/AddTripItemTool 的代码模式：构造函数注入 NextApiService + NextAuthProvider，parameters 用 buildJsonObject 构建，execute 中 authProvider.isLoggedIn.first() 检查登录，result.fold 处理结果。

**理由**：保持一致性，无需设计新模式。

### 2. 只创建文件、不注册
Tool 注册（Koin module、prompt 描述）由总管在其他变更中统一处理，本变更只负责 Tool 类文件。

**理由**：避免多人同时修改 AppModule.kt 造成冲突。

## Risks / Trade-offs

- [风险] 4 个 Tool 创建后无法直接运行测试（未注册到 Koin）→ 待总管注册后统一验证
- [风险] NextApiService 的 update/delete 方法签名可能与预期不一致 → 创建前先阅读 CreateTripTool 和 AddTripItemTool 确认模式
