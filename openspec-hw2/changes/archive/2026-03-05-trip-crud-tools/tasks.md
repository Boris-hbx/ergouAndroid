## 1. 阅读现有代码，确认模式

- [x] 1.1 阅读 CreateTripTool.kt 和 AddTripItemTool.kt，确认 Tool 接口、参数构建、登录检查、result.fold 模式
- [x] 1.2 阅读 NextApiService.kt 中 updateTrip/deleteTrip/updateTripItem/deleteTripItem 方法签名和 Request/Response DTO

## 2. 创建 4 个 Tool 文件

- [x] 2.1 创建 UpdateTripTool.kt — update_trip 工具（trip_id 必填，title/destination/date_from/date_to/purpose/notes/currency 可选）
- [x] 2.2 创建 DeleteTripTool.kt — delete_trip 工具（trip_id 必填）
- [x] 2.3 创建 UpdateTripItemTool.kt — update_trip_item 工具（item_id/trip_id 必填，type/date/description/amount/currency/reimburse_status/notes 可选，amount 用 toDoubleOrNull）
- [x] 2.4 创建 DeleteTripItemTool.kt — delete_trip_item 工具（item_id/trip_id 必填）

## 3. 验证

- [x] 3.1 确认 4 个文件编译无误（项目 build 通过）
- [ ] 3.2 待总管注册到 Koin 和 Prompt 后，手动测试更新/删除出差和费用明细
