## ADDED Requirements

### Requirement: UpdateTripTool 更新出差记录
系统 SHALL 提供 `update_trip` 工具，允许 LLM 调用 NextApiService 更新出差记录的标题、目的地、日期、目的、备注、币种等字段。

- 工具参数：trip_id（必填 string）、title、destination、date_from、date_to、purpose、notes、currency（均为可选 string）
- 执行前 SHALL 通过 authProvider.isLoggedIn.first() 检查登录状态
- 成功时 SHALL 返回 "出差已更新：${trip.title}"
- 失败时 SHALL 返回 "更新出差失败：${e.message}"

#### Scenario: 成功更新出差标题
- **WHEN** 用户已登录，LLM 调用 update_trip 传入有效 trip_id 和新 title
- **THEN** Tool 调用 nextApiService.updateTrip 并返回 "出差已更新：{新标题}"

#### Scenario: 未登录时更新出差
- **WHEN** 用户未登录，LLM 调用 update_trip
- **THEN** Tool 返回 "请先登录 Next 账号"

#### Scenario: API 调用失败
- **WHEN** nextApiService.updateTrip 抛出异常
- **THEN** Tool 返回 "更新出差失败：{错误信息}"

---

### Requirement: DeleteTripTool 删除出差记录
系统 SHALL 提供 `delete_trip` 工具，允许 LLM 调用 NextApiService 删除出差记录及其所有费用明细。

- 工具参数：trip_id（必填 string）
- 执行前 SHALL 检查登录状态
- 成功时 SHALL 返回 "出差已删除"
- 失败时 SHALL 返回 "删除出差失败：${e.message}"

#### Scenario: 成功删除出差
- **WHEN** 用户已登录，LLM 调用 delete_trip 传入有效 trip_id
- **THEN** Tool 调用 nextApiService.deleteTrip 并返回 "出差已删除"

#### Scenario: 未登录时删除出差
- **WHEN** 用户未登录，LLM 调用 delete_trip
- **THEN** Tool 返回 "请先登录 Next 账号"

#### Scenario: 删除不存在的出差
- **WHEN** trip_id 对应的记录不存在，API 返回错误
- **THEN** Tool 返回 "删除出差失败：{错误信息}"

---

### Requirement: UpdateTripItemTool 更新费用明细
系统 SHALL 提供 `update_trip_item` 工具，允许 LLM 调用 NextApiService 更新差旅费用明细的类型、日期、描述、金额、币种、报销状态、备注。

- 工具参数：item_id（必填）、trip_id（必填）、type、date、description、amount、currency、reimburse_status、notes（均为可选 string）
- amount 参数 SHALL 使用 toDoubleOrNull() 转换
- 执行前 SHALL 检查登录状态
- 成功时 SHALL 返回 "费用已更新：${item.description} ${item.amount} ${item.currency}"
- 失败时 SHALL 返回 "更新费用失败：${e.message}"

#### Scenario: 成功更新费用金额
- **WHEN** 用户已登录，LLM 调用 update_trip_item 传入有效 trip_id、item_id 和新 amount
- **THEN** Tool 调用 nextApiService.updateTripItem 并返回包含更新后描述、金额、币种的确认信息

#### Scenario: 未登录时更新费用
- **WHEN** 用户未登录，LLM 调用 update_trip_item
- **THEN** Tool 返回 "请先登录 Next 账号"

#### Scenario: API 调用失败
- **WHEN** nextApiService.updateTripItem 抛出异常
- **THEN** Tool 返回 "更新费用失败：{错误信息}"

---

### Requirement: DeleteTripItemTool 删除费用明细
系统 SHALL 提供 `delete_trip_item` 工具，允许 LLM 调用 NextApiService 删除差旅的一笔费用明细。

- 工具参数：item_id（必填）、trip_id（必填）
- 执行前 SHALL 检查登录状态
- 成功时 SHALL 返回 "费用已删除"
- 失败时 SHALL 返回 "删除费用失败：${e.message}"

#### Scenario: 成功删除费用
- **WHEN** 用户已登录，LLM 调用 delete_trip_item 传入有效 trip_id 和 item_id
- **THEN** Tool 调用 nextApiService.deleteTripItem 并返回 "费用已删除"

#### Scenario: 未登录时删除费用
- **WHEN** 用户未登录，LLM 调用 delete_trip_item
- **THEN** Tool 返回 "请先登录 Next 账号"

#### Scenario: 删除不存在的费用
- **WHEN** item_id 对应的记录不存在，API 返回错误
- **THEN** Tool 返回 "删除费用失败：{错误信息}"
