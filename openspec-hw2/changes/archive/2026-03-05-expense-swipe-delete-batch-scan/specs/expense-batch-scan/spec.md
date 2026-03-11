## ADDED Requirements

### Requirement: 批量选择照片
系统 SHALL 使用 `ActivityResultContracts.PickMultipleVisualMedia()` 支持一次选择多张照片。选中的照片 SHALL 在 LazyRow 中展示缩略图预览（Coil AsyncImage），每张照片右上角 SHALL 有删除按钮。用户 SHALL 能通过 + 按钮追加更多照片。

#### Scenario: 选择多张照片
- **WHEN** 用户点击扫描入口并选择 3 张照片
- **THEN** 系统在 LazyRow 中展示 3 张缩略图
- **THEN** 显示"已选择 3 张照片"

#### Scenario: 删除单张已选照片
- **WHEN** 用户点击某张缩略图右上角的 X
- **THEN** 该照片从预览列表移除，计数更新

#### Scenario: 追加更多照片
- **WHEN** 用户点击 LazyRow 末尾的 + 按钮
- **THEN** 系统再次打开照片选择器，新选照片追加到现有列表

### Requirement: AI 批量分析
系统 SHALL 逐张将照片转 base64 后调用 `parseReceiptPreview` API。系统 SHALL 显示分析进度（"分析中 1/3..."）。单张照片超过 10MB SHALL 被跳过并提示。

#### Scenario: 批量分析成功
- **WHEN** 用户选了 3 张照片并点击"二狗分析"
- **THEN** 系统显示 loading 状态和进度文字
- **THEN** 系统逐张调用 API，每完成一张更新进度
- **THEN** 全部完成后展示分析结果列表

#### Scenario: 照片为空时按钮禁用
- **WHEN** 预览列表中没有照片
- **THEN** "二狗分析"按钮为禁用状态（灰色）

#### Scenario: 单张照片超过 10MB
- **WHEN** 某张照片文件大小超过 10MB
- **THEN** 系统跳过该张，Snackbar 提示"第 N 张照片超过 10MB，已跳过"
- **THEN** 继续分析剩余照片

#### Scenario: 单张分析失败
- **WHEN** 某张照片的 API 调用失败
- **THEN** 系统标记该张为"分析失败"并继续处理其余照片
- **THEN** 结果列表中不包含失败的条目

### Requirement: 分析结果展示与编辑
系统 SHALL 将 AI 分析结果以列表形式展示，每条显示商家名、总金额和明细行。用户 SHALL 能编辑金额、商家、备注、标签，也 SHALL 能删除不需要的条目。

#### Scenario: 展示分析结果
- **WHEN** AI 分析完成
- **THEN** 每条结果显示：商家名、总金额（¥格式）、明细行列表
- **THEN** 每条结果有"编辑"和"删除"按钮

#### Scenario: 编辑分析结果
- **WHEN** 用户点击某条结果的"编辑"
- **THEN** 系统展示可编辑字段：金额、商家、备注、标签、币种
- **THEN** 用户修改后确认，结果列表更新

#### Scenario: 删除分析结果
- **WHEN** 用户点击某条结果的"删除"
- **THEN** 该条目从结果列表移除

#### Scenario: 所有结果被删除
- **WHEN** 用户删除了所有分析结果
- **THEN** "确认保存全部"按钮隐藏

### Requirement: 批量保存
系统 SHALL 在用户点击"确认保存全部"后，逐条创建记账条目并上传对应原始照片。保存完成后 SHALL 返回列表页并刷新。

#### Scenario: 全部保存成功
- **WHEN** 用户点击"确认保存全部"
- **THEN** 系统逐条调用创建 API + 上传照片
- **THEN** 全部完成后返回记账列表，列表刷新

#### Scenario: 部分保存失败
- **WHEN** 某条创建 API 调用失败
- **THEN** 已成功的条目保留，失败的条目提示错误
- **THEN** 用户可重试失败的条目

#### Scenario: 照片上传失败但条目已创建
- **WHEN** 记账条目创建成功但照片上传失败
- **THEN** 条目仍保存（无照片），系统提示照片上传失败
