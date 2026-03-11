## ADDED Requirements

### Requirement: 编辑后照片操作组合测试
系统 SHALL 支持用户在编辑已有账单时同时修改字段和操作照片，所有变更 SHALL 正确持久化。

#### Scenario: 编辑字段 + 添加照片
- **WHEN** 用户修改金额并添加一张新照片后保存
- **THEN** 金额更新成功，新照片上传成功

#### Scenario: 编辑字段 + 删除照片
- **WHEN** 用户修改备注并删除一张已有照片后保存
- **THEN** 备注更新成功，照片已删除

#### Scenario: 编辑字段 + 替换照片
- **WHEN** 用户删除旧照片、添加新照片、修改标签后保存
- **THEN** 所有变更均正确持久化

#### Scenario: 只操作照片不修改字段
- **WHEN** 用户只添加照片不修改任何字段
- **THEN** 照片上传成功，其余字段不变

### Requirement: 编辑后重新 AI 分析组合测试
系统 SHALL 支持用户在编辑已有账单时添加收据照片并触发 AI 重新分析。

#### Scenario: 手动账单添加照片后 AI 分析更新
- **WHEN** 用户对一条手动创建的账单添加收据照片并触发 AI 分析
- **THEN** AI 返回新的金额和明细
- **THEN** 用户确认后字段更新为 AI 解析值

#### Scenario: AI 分析后用户只采纳部分结果
- **WHEN** AI 解析出新金额和标签，用户只更新金额保留原标签
- **THEN** 保存后金额为 AI 值，标签为原始值

#### Scenario: AI 分析失败后原数据不变
- **WHEN** 用户触发 AI 分析但超时
- **THEN** 账单原有字段不受影响，用户可继续手动编辑

### Requirement: 多次编辑一致性测试
系统 SHALL 在用户对同一账单进行多次编辑后保持数据一致性。

#### Scenario: 连续两次编辑同一账单
- **WHEN** 用户编辑账单 A 将金额从 $20 改为 $25 并保存
- **WHEN** 用户再次进入账单 A 将备注从"午饭"改为"午饭+饮料"并保存
- **THEN** 详情页显示金额 $25、备注"午饭+饮料"

#### Scenario: 编辑保存后列表页一致性
- **WHEN** 用户修改金额并保存后返回列表
- **THEN** 列表中该条目金额与详情页一致
- **THEN** 再次进入详情页数据一致

## MODIFIED Requirements

### Requirement: Edit expense fields
The system SHALL allow editing amount, date, notes, tags, and currency on the detail page. The system SHALL call `PUT /api/expenses/:id` with only the changed fields. Text input fields SHALL use `TextFieldValue` to support Chinese IME.

#### Scenario: Edit amount and notes
- **WHEN** user modifies the amount and notes fields and taps save
- **THEN** system calls `PUT /api/expenses/:id` with updated amount and notes
- **THEN** system displays success feedback and refreshes the detail

#### Scenario: Edit date
- **WHEN** user taps the date field and selects a new date from the date picker
- **THEN** the date field updates to the selected date (YYYY-MM-DD format)

#### Scenario: Edit tags
- **WHEN** user adds or removes tags (via chips or custom input)
- **THEN** the tags array updates accordingly and is sent on save

#### Scenario: Edit currency
- **WHEN** user selects a different currency (CAD/CNY/USD)
- **THEN** the currency field updates and the amount display reformats

#### Scenario: Save with invalid amount
- **WHEN** user clears the amount field or enters non-numeric text
- **THEN** the save button is disabled

#### Scenario: Save API failure
- **WHEN** the PUT request fails
- **THEN** system displays an error message and preserves the user's edits (no data loss)

#### Scenario: Edit with Chinese IME input
- **WHEN** user types Chinese text in notes field using pinyin IME
- **THEN** TextFieldValue correctly handles composition without interruption
- **THEN** saved notes contain the correct Chinese text
