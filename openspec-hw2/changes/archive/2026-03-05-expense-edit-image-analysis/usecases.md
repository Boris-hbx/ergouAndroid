## Use Cases

### Use Case: Edit expense record

**Primary Actor:** User
**Scope:** Ergou App (记账模块)
**Level:** User goal

**Stakeholders and Interests:**
- User — 修正记错的金额、备注、标签等信息，无需删除重建

**Preconditions:**
- User 已登录 Next 账号
- 至少存在一条记账记录

**Success Guarantee (Postconditions):**
- 记账条目已更新（金额/日期/备注/标签/币种）
- 列表和汇总自动刷新反映新数据

**Trigger:** User 在记账列表点击某条记录

**Main Success Scenario:**
1. User 点击记账列表中的一条记录。
2. System 打开详情页，展示该条目的完整信息（金额、日期、备注、标签、币种、明细行、照片）。
3. User 修改需要更改的字段（如金额、备注）。
4. User 确认保存。
5. System 调用 Next API 更新条目，展示成功提示。
6. System 返回列表页，数据已刷新。

**Extensions:**
- 2a. 网络请求失败：System 展示错误提示，User 可重试。
- 4a. 金额为空或无效：保存按钮禁用，提示输入有效金额。
- 5a. API 返回错误：System 展示错误信息，保留用户编辑内容不丢失。

---

### Use Case: Upload photo to expense

**Primary Actor:** User
**Scope:** Ergou App (记账模块)
**Level:** User goal

**Stakeholders and Interests:**
- User — 为记账记录附加票据/收据照片，便于日后查阅

**Preconditions:**
- User 已登录 Next 账号
- 已在记账详情页

**Success Guarantee (Postconditions):**
- 照片已上传至 Next 后端并关联到该记账条目
- 详情页展示已上传的照片

**Trigger:** User 在详情页点击"添加照片"

**Main Success Scenario:**
1. User 在详情页点击添加照片按钮。
2. System 弹出选择方式：从相册选择 / 拍照。
3. User 选择照片来源并选取一张图片。
4. System 将照片上传至 Next 后端 (`POST /api/expenses/:id/photos`)。
5. System 上传成功后刷新照片列表，展示缩略图。

**Extensions:**
- 3a. User 取消选择：流程结束，无操作。
- 4a. 照片超过 10MB：System 提示文件过大，请选择其他照片。
- 4b. 网络上传失败：System 展示错误提示，User 可重试。
- 5a. User 点击已上传照片：System 全屏展示照片。
- 5b. User 长按或点击删除按钮：System 确认后调用 API 删除照片。

---

### Use Case: Scan receipt with AI analysis

**Primary Actor:** User
**Scope:** Ergou App (记账模块)
**Level:** User goal

**Stakeholders and Interests:**
- User — 拍照/选照片自动识别收据内容，减少手动输入

**Preconditions:**
- User 已登录 Next 账号

**Success Guarantee (Postconditions):**
- AI 解析结果被用户确认并保存为一条新的记账条目（含明细行）
- 照片关联到新建的条目

**Trigger:** User 在记账页面发起"拍照记账"

**Main Success Scenario:**
1. User 在记账页面点击"拍照记账"（或新增对话框中选择拍照）。
2. System 弹出选择方式：从相册选择 / 拍照。
3. User 选取一张收据照片。
4. System 将照片发送至 Next 后端 (`POST /api/expenses/parse-preview`)，展示加载状态。
5. System 收到 AI 解析结果，展示预览页面：商家、日期、总金额、币种、标签、明细行列表。
6. User 检查并可修改任意字段（金额、日期、标签等）。
7. User 确认保存。
8. System 调用创建 API 保存条目（含 items），然后上传照片关联到新条目。
9. System 跳转到记账列表，数据已刷新。

**Extensions:**
- 3a. User 取消选择：流程结束。
- 4a. AI 解析超时（>120s）：System 提示超时，建议手动输入。
- 5a. AI 解析失败或返回空结果：System 提示解析失败，提供手动输入入口。
- 6a. AI 解析的金额明显有误：User 手动修正后继续。
- 8a. 保存失败：System 展示错误，保留预览数据不丢失，User 可重试。

---

### Use Case: View expense detail with items and photos

**Primary Actor:** User
**Scope:** Ergou App (记账模块)
**Level:** Subfunction

**Stakeholders and Interests:**
- User — 查看记账详情，包括 AI 解析的明细行和附加的照片

**Preconditions:**
- User 已登录 Next 账号
- 记账条目存在

**Success Guarantee (Postconditions):**
- 详情页展示完整信息：基本字段 + 明细行 + 照片列表

**Trigger:** User 点击记账列表中的条目

**Main Success Scenario:**
1. User 点击列表中的某条记录。
2. System 调用 `GET /api/expenses/:id` 获取完整详情（含 items 和 photos）。
3. System 展示详情页：基本信息区（金额、日期、备注、标签、币种）+ 明细行列表 + 照片网格。

**Extensions:**
- 2a. 加载失败：System 展示错误提示，提供重试按钮。
- 3a. 无明细行：隐藏明细区域。
- 3b. 无照片：隐藏照片区域，只显示添加照片按钮。

**Open Questions:**
- 详情页和编辑页是否合并为一个页面（查看模式 + 编辑模式），还是直接做成可编辑的表单页？建议直接做成可编辑表单，减少页面数量。
