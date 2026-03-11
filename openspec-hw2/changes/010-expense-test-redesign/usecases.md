## Use Cases

### Use Case: 手动新建账单并保存

**Primary Actor:** 用户
**Scope:** 二狗记账模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 快速记录一笔消费，确保金额、标签、币种、日期等信息正确持久化

**Preconditions:**
- 用户已登录 Next 后端
- 记账页面已加载

**Success Guarantee (Postconditions):**
- Next 后端创建了一条新的 expense entry
- 列表页刷新后显示新条目，金额/标签/日期/币种均正确
- 条目详情页可查看完整信息

**Trigger:** 用户点击记账页面的 FAB（+）按钮

**Main Success Scenario:**
1. 用户点击 FAB，弹出新建账单对话框
2. 用户填写金额（如 25.50）、备注（如"午饭"）、选择标签（如"餐饮"）、选择币种（CAD）、选择日期
3. 用户点击"保存"
4. 系统调用 POST /api/expenses 创建条目
5. 系统关闭对话框，刷新列表
6. 用户在列表中看到新条目，金额/标签/日期均正确
7. 用户点击该条目进入详情页，确认所有字段与输入一致

**Extensions:**
- 2a. 用户不填金额直接保存：系统提示金额为必填项，不发送请求
- 2b. 用户切换币种为 CNY：保存后条目显示 ¥ 符号
- 4a. 网络请求失败：系统显示错误提示，对话框保持打开，用户可重试
- 2c. 用户选择多个标签：保存后条目显示所有已选标签

---

### Use Case: 拍照新建单张收据账单

**Primary Actor:** 用户
**Scope:** 二狗记账模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 通过拍照/选照片自动识别收据信息，减少手动输入

**Preconditions:**
- 用户已登录 Next 后端
- Claude API Key 已配置

**Success Guarantee (Postconditions):**
- 系统创建了一条 expense entry，金额与收据一致
- 原始照片已上传并关联到该条目
- 解析出的明细（items）正确记录

**Trigger:** 用户进入新建账单详情页（拍照记账模式）

**Main Success Scenario:**
1. 用户点击"拍照记账"入口，进入详情页的扫描预览模式
2. 用户从相册选择一张收据照片（如 group2 中的 Food Basics $19.56）
3. 系统压缩图片（≤1920px, 80% JPEG），发送 base64 到 Claude API
4. Claude 返回解析结果：商家名、日期、金额、明细项、税费
5. 系统展示解析预览，用户可查看并编辑各字段
6. 用户确认无误，点击"保存"
7. 系统调用 POST /api/expenses 创建条目，再调用 POST /api/expenses/:id/photos 上传原图
8. 系统返回列表页，新条目出现在列表中
9. 用户点击条目进入详情，确认金额 $19.56、商家 Food Basics、明细项（Honeycrisp Apple/Banana/Mango）、照片均正确

**Extensions:**
- 3a. 照片超过 10MB：系统提示"照片不能超过 10MB"，不发送分析请求
- 4a. Claude API 超时：系统提示"分析超时，请手动输入"，切换到手动输入模式
- 4b. Claude 返回格式异常：系统提示"AI 返回格式异常，请重试"
- 5a. 用户修改解析出的金额/标签/备注：修改后的值在保存时生效
- 7a. 创建成功但照片上传失败：条目已创建，提示照片上传失败

---

### Use Case: 多张照片识别为同一账单（合并分析）

**Primary Actor:** 用户
**Scope:** 二狗记账模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 长收据需多张照片拍摄，系统应合并识别为一条记录

**Preconditions:**
- 用户已登录 Next 后端
- Claude API Key 已配置

**Success Guarantee (Postconditions):**
- 系统创建了 1 条 expense entry（非多条）
- 总金额与实际收据一致（$235.76）
- 所有原始照片（4 张）均已上传关联

**Trigger:** 用户在批量扫描模式选择多张照片并选择"同一单据"

**Main Success Scenario:**
1. 用户点击批量扫描按钮，进入批量扫描模式
2. 用户选择 group1 的 4 张照片（T&T 超市收据的不同部分）
3. 用户选择"同一单据"模式
4. 用户点击"二狗分析"
5. 系统将 4 张照片拼接为一张长图（垂直拼接，≤1024px 宽，≤8000px 高）
6. 系统发送拼接后的 base64 到 Claude API
7. Claude 返回 1 条解析结果：T&T Supermarket, $235.76, 包含完整明细项
8. 系统展示 1 条解析预览卡片，用户可编辑
9. 用户确认，点击"保存"
10. 系统创建 1 条 expense entry，逐张上传 4 张原始照片
11. 用户在列表中看到 1 条 T&T 记录（$235.76），进入详情确认 4 张照片和完整明细

**Extensions:**
- 5a. 拼接后图片超过 8000px 高度：系统自动缩放整张拼接图
- 6a. Claude 分析超时：提示超时，用户可重试或手动输入
- 8a. 用户修改总金额或标签：保存时使用修改后的值
- 10a. 部分照片上传失败：条目已创建，提示哪些照片上传失败

---

### Use Case: 多张照片识别为多个不同账单（批量独立分析）

**Primary Actor:** 用户
**Scope:** 二狗记账模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 一次选多张不同收据，系统分别识别并创建多条记录

**Preconditions:**
- 用户已登录 Next 后端
- Claude API Key 已配置

**Success Guarantee (Postconditions):**
- 系统创建了 N 条 expense entry（每张照片对应一条）
- 每条记录的金额/商家与对应收据一致
- 每张原始照片关联到对应条目

**Trigger:** 用户在批量扫描模式选择多张照片并选择"不同单据"

**Main Success Scenario:**
1. 用户点击批量扫描按钮
2. 用户选择 group2 的 5 张照片（Structube/Costco/Food Basics/IKEA/兰州拉面）
3. 用户选择"不同单据"模式
4. 用户点击"二狗分析"
5. 系统逐张压缩并发送到 Claude API，显示进度（1/5, 2/5...5/5）
6. 系统展示 5 条解析结果卡片，每条显示商家名和金额
7. 用户逐一检查：Structube $607.94, Costco $84.63, Food Basics $19.56, IKEA $44.89, 兰州拉面 $75.77
8. 用户可编辑任意一条的金额/标签/备注
9. 用户点击"确认保存全部"
10. 系统逐条创建 expense entry + 上传对应照片，显示保存进度
11. 返回列表页，5 条新记录出现，用户逐一验证

**Extensions:**
- 5a. 某张照片分析失败：该条标记为失败，其余继续分析，用户可对失败条手动输入
- 9a. 用户删除某条不想保存的结果：只保存剩余条目
- 10a. 部分条目保存失败：提示成功/失败数量，失败条目保留在扫描结果中可重试

---

### Use Case: 混合照片批量分析（同一收据+不同收据混合）

**Primary Actor:** 用户
**Scope:** 二狗记账模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 一次性上传所有照片，系统应智能识别哪些属于同一收据、哪些是独立收据

**Preconditions:**
- 用户已登录 Next 后端
- Claude API Key 已配置

**Success Guarantee (Postconditions):**
- 系统正确区分同一收据的多张照片 vs 不同收据的照片
- 同一收据的照片合并分析为 1 条记录，不同收据各自独立
- 所有条目数据正确，照片关联正确

**Trigger:** 用户选择 group3 的 9 张混合照片进行批量分析

**Main Success Scenario:**
1. 用户点击批量扫描按钮
2. 用户选择 group3 的全部 9 张照片
3. 用户需要选择处理模式（"同一单据"或"不同单据"）
4. **情况 A — 选"不同单据"**：系统逐张分析，生成 9 条结果（其中 4 条是同一 T&T 收据的部分，会出现重复/不完整的问题）
5. **情况 B — 选"同一单据"**：系统拼接 9 张图，尝试合并分析（混合不同收据会导致解析混乱）
6. 用户检查结果，手动合并/拆分/编辑/删除不正确的条目
7. 用户确认保存

**Extensions:**
- 4a. 情况 A 下用户发现 T&T 的 4 条结果不完整：用户删除这 4 条，另外在"同一单据"模式下单独处理 group1 的 4 张照片
- 5a. 情况 B 下解析结果混乱：用户取消，改为分批处理（group1 用合并模式，group2 用独立模式）
- 3a. 用户不确定该选哪个模式：当前系统需用户自行判断，无自动识别功能

**Open Questions:**
- 未来是否支持 AI 自动识别哪些照片属于同一收据？当前需要用户手动选择模式

---

### Use Case: 编辑已有账单的基本信息

**Primary Actor:** 用户
**Scope:** 二狗记账模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 修正账单的金额、备注、日期、币种、标签等信息

**Preconditions:**
- 已有至少一条 expense entry
- 用户已登录

**Success Guarantee (Postconditions):**
- 修改的字段在后端已更新（PUT 请求成功）
- 列表页和详情页均反映最新值
- 未修改的字段保持不变

**Trigger:** 用户点击列表中某条记录进入详情页

**Main Success Scenario:**
1. 用户点击列表中一条已有账单，进入详情页
2. 系统加载完整详情（GET /api/expenses/:id），包括金额、日期、备注、标签、币种、明细、照片
3. 用户修改金额（如从 19.56 改为 20.00）
4. 用户修改备注（如添加"含小费"）
5. 用户修改标签（如从"超市"改为"餐饮"）
6. 用户点击"保存"
7. 系统调用 PUT /api/expenses/:id，只发送变更字段
8. 系统提示保存成功，返回列表页
9. 用户确认列表中该条目显示更新后的金额和标签

**Extensions:**
- 2a. 加载失败：显示错误提示，可重试
- 6a. 用户未修改任何字段就点保存：不发送请求或发送空更新
- 7a. 网络失败：提示保存失败，字段修改保留在本地，用户可重试
- 3a. 用户修改日期：日期选择器限制为合理范围
- 3b. 用户切换币种（CAD↔CNY）：保存后列表显示对应货币符号

---

### Use Case: 为已有账单添加/替换/删除照片

**Primary Actor:** 用户
**Scope:** 二狗记账模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 管理账单关联的收据照片

**Preconditions:**
- 已有一条 expense entry（可以有或没有照片）
- 用户已登录

**Success Guarantee (Postconditions):**
- 照片变更已同步到后端（新增/删除）
- 详情页照片列表反映最新状态
- 照片可全屏查看

**Trigger:** 用户在账单详情页操作照片区域

**Main Success Scenario（新增照片）:**
1. 用户进入一条已有账单的详情页
2. 用户点击照片区域的"添加照片"按钮
3. 用户从相册选择一张新照片
4. 系统调用 POST /api/expenses/:id/photos 上传照片
5. 照片出现在照片列表中，可点击查看全屏

**Extensions（删除照片）:**
- 5a. 用户点击某张照片上的删除按钮
- 5b. 系统弹出确认对话框
- 5c. 用户确认删除，系统调用 DELETE /api/expenses/photos/:photoId
- 5d. 照片从列表中移除

**Extensions（替换照片）:**
- 5e. 用户删除旧照片（步骤 5a-5d），然后添加新照片（步骤 2-5）
- 5f. 替换后详情页只显示新照片

**Extensions（其他边界情况）:**
- 3a. 照片超 10MB：提示"照片不能超过 10MB"，不上传
- 4a. 上传失败：提示错误，照片列表不变
- 1a. 详情页加载时照片列表显示已有照片的缩略图（带 session cookie 认证）

---

### Use Case: 为已有账单新增照片并重新 AI 分析

**Primary Actor:** 用户
**Scope:** 二狗记账模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 已有一条手动创建的账单，补充收据照片后希望 AI 重新分析更新信息

**Preconditions:**
- 已有一条 expense entry
- Claude API Key 已配置

**Success Guarantee (Postconditions):**
- 新照片已上传
- AI 分析结果正确更新了账单字段（金额/明细等）

**Trigger:** 用户在详情页添加照片后触发 AI 分析

**Main Success Scenario:**
1. 用户进入一条已有账单详情页（如手动创建的"午饭 $25"）
2. 用户添加一张收据照片
3. 系统上传照片成功
4. 用户点击"二狗分析"触发 AI 解析
5. Claude 返回解析结果：实际金额 $32.50，商家"兰州拉面"，明细项
6. 系统展示解析预览，用户可查看差异
7. 用户确认使用 AI 结果更新
8. 系统调用 PUT /api/expenses/:id 更新字段
9. 详情页显示更新后的信息

**Extensions:**
- 4a. AI 分析超时：提示超时，原有数据不变
- 6a. 用户只采纳部分 AI 结果（如更新金额但保留原标签）：部分更新

---

### Use Case: 滑动删除账单（带撤销）

**Primary Actor:** 用户
**Scope:** 二狗记账模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 快速删除不需要的账单，但有后悔药

**Preconditions:**
- 列表中存在至少一条 expense entry

**Success Guarantee (Postconditions):**
- 如果用户未撤销：条目在后端被永久删除
- 如果用户撤销：条目恢复原位，后端数据不变

**Trigger:** 用户在列表中左滑某条记录

**Main Success Scenario:**
1. 用户在列表中左滑一条账单
2. 系统显示红色背景 + 删除图标
3. 滑动完成，条目从列表中消失（乐观删除）
4. 底部出现 Snackbar："已删除" + "撤销"按钮（5 秒倒计时）
5. 5 秒内用户未操作，Snackbar 消失
6. 系统调用 DELETE /api/expenses/:id 永久删除
7. 条目彻底从后端移除

**Extensions:**
- 4a. 用户在 5 秒内点击"撤销"：条目恢复到列表原位，不调用删除 API
- 6a. 删除 API 失败：条目恢复到列表，显示错误提示
- 1a. 用户连续滑动删除多条：每条独立处理，各有自己的 Snackbar 和撤销窗口

---

### Use Case: 从详情页删除账单

**Primary Actor:** 用户
**Scope:** 二狗记账模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 在查看详情后决定删除该账单

**Preconditions:**
- 用户在某条 expense entry 的详情页

**Success Guarantee (Postconditions):**
- 条目及其所有关联照片在后端被删除
- 用户回到列表页，该条目不再显示

**Trigger:** 用户在详情页点击删除按钮

**Main Success Scenario:**
1. 用户在详情页点击"删除"按钮
2. 系统弹出确认对话框："确认删除此账单？"
3. 用户点击"确认"
4. 系统调用 DELETE /api/expenses/:id
5. 系统导航回列表页，该条目已消失
6. 用户确认列表中不再显示该条目

**Extensions:**
- 3a. 用户点击"取消"：对话框关闭，不做任何操作
- 4a. 删除失败：提示错误，停留在详情页

---

### Use Case: 通过聊天工具记账和查询

**Primary Actor:** 用户
**Scope:** 二狗聊天模块 + 记账工具
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 通过自然语言对话完成记账和查询，无需进入记账页面

**Preconditions:**
- 用户已登录 Next 后端
- DeepSeek API Key 已配置

**Success Guarantee (Postconditions):**
- 通过 add_expense 工具创建的条目与手动创建效果一致
- 通过 query_expenses 查询结果与列表页一致
- 通过 expense_summary 统计结果正确

**Trigger:** 用户在聊天中发送记账相关指令

**Main Success Scenario:**
1. 用户在聊天中说"帮我记一笔 午饭 25.5 加币 标签餐饮"
2. 二狗调用 add_expense 工具：amount=25.5, notes="午饭", tags="餐饮", currency="CAD"
3. 工具返回确认信息，含条目 ID
4. 用户说"查一下这个月花了多少"
5. 二狗调用 expense_summary 工具：period="month"
6. 工具返回汇总：总金额、条目数、标签分布、同比上月变化
7. 用户说"删掉刚才那笔"
8. 二狗调用 delete_expense 工具：expense_id=<刚才的 ID>
9. 工具返回删除确认

**Extensions:**
- 2a. 用户没说金额：二狗通过对话追问金额
- 5a. 用户说"查一下上周在超市花了多少"：query_expenses period="week" tag="超市"
- 8a. 用户给了错误的 ID：工具返回 404 错误，二狗提示未找到
