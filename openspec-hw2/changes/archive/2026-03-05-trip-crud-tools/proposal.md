## Why

差旅(Trip)功能目前只有创建和查询类的 4 个 Tool（CreateTripTool, AddTripItemTool, QueryTripsTool, TripSummaryTool），缺少更新和删除操作。NextApiService 中已实现 updateTrip, deleteTrip, updateTripItem, deleteTripItem 四个 API 方法，但没有对应的 Tool 暴露给 LLM，导致用户无法通过对话修改或删除差旅记录和费用明细。

## What Changes

- 新增 `UpdateTripTool`：允许 LLM 调用更新出差信息（标题、目的地、日期、目的等）
- 新增 `DeleteTripTool`：允许 LLM 调用删除出差记录及其所有费用明细
- 新增 `UpdateTripItemTool`：允许 LLM 调用更新费用明细（类型、描述、金额、报销状态等）
- 新增 `DeleteTripItemTool`：允许 LLM 调用删除单笔费用明细

## Capabilities

### New Capabilities
- `trip-crud-tools`: 差旅的更新/删除 Tool，补齐 CRUD 操作闭环

### Modified Capabilities

（无，仅新增 Tool 文件，不修改现有代码）

## Impact

- 新增 4 个文件于 `app/src/main/java/com/ergou/app/data/tool/tools/`
- 依赖已有的 `NextApiService` 和 `NextAuthProvider`，无需修改
- 后续需在 `AppModule.kt` 注册新 Tool、在 `ErgouPrompt.kt` 添加工具描述（由总管统一修改，不在本变更范围内）
