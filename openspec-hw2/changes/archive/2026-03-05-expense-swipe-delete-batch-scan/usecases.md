## Use Cases

### Use Case: 左滑删除记账条目

**Primary Actor:** 用户
**Scope:** 二狗记账模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 快速删除不需要的记账条目，与其他模块操作习惯一致

**Preconditions:**
- 用户在记账列表页面，列表中有至少一条记账记录

**Success Guarantee (Postconditions):**
- 条目从列表中移除
- 若用户未撤销，条目从后端永久删除
- 若用户撤销，条目恢复到列表中

**Trigger:** 用户在记账条目上左滑

**Main Success Scenario:**
1. 用户在某条记账记录上向左滑动
2. 系统显示红色删除背景
3. 系统弹出确认对话框："确认删除此记录？"
4. 用户点击确认
5. 系统乐观移除该条目，显示 Snackbar "已删除" + 撤销按钮
6. Snackbar 超时后系统调用后端删除接口

**Extensions:**
- 3a. 用户取消滑动或点击取消：条目恢复原位，无操作
- 5a. 用户点击"撤销"：条目恢复到列表中，不调用删除接口
- 6a. 后端删除失败：条目恢复到列表中，Snackbar 提示错误

---

### Use Case: 批量扫描收据并记账

**Primary Actor:** 用户
**Scope:** 二狗记账模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 一次性扫描多张收据，减少重复操作

**Preconditions:**
- 用户在记账页面

**Success Guarantee (Postconditions):**
- 所有确认的收据分析结果被保存为记账条目
- 对应的原始照片被上传

**Trigger:** 用户点击扫描/相机入口按钮

**Main Success Scenario:**
1. 用户点击扫描入口，进入批量扫描模式
2. 系统打开照片选择器，用户选择多张收据照片
3. 系统在 LazyRow 中展示照片缩略图预览
4. 用户点击"二狗分析"按钮
5. 系统逐张将照片转 base64 调用 AI 分析，显示进度（"分析中 1/3..."）
6. 系统展示分析结果列表（每条显示商家、金额、明细）
7. 用户审查结果，按需编辑金额/备注或删除不需要的条目
8. 用户点击"确认保存全部"
9. 系统逐条保存记账条目并上传照片，完成后返回列表并刷新

**Extensions:**
- 2a. 用户只选了 1 张照片：正常流程，与多张相同
- 3a. 用户点击照片右上角 X：删除该张照片，更新计数
- 3b. 用户点击 + 按钮：重新打开选择器添加更多照片
- 4a. 照片为空：按钮禁用，无法点击
- 5a. 某张照片超过 10MB：跳过并提示"图片过大"
- 5b. AI 分析失败（网络/API 错误）：标记该张为失败，其他继续
- 7a. 用户删除所有结果：保存按钮隐藏
- 9a. 部分保存失败：已成功的保留，失败的提示错误

---

### Use Case: 编辑扫描分析结果

**Primary Actor:** 用户
**Scope:** 二狗记账模块
**Level:** Subfunction

**Stakeholders and Interests:**
- 用户 — 修正 AI 分析的错误识别（金额、商家名等）

**Preconditions:**
- AI 分析已完成，结果列表中有至少一条结果

**Success Guarantee (Postconditions):**
- 用户修改的内容反映在结果列表中，后续保存使用修改后的值

**Trigger:** 用户点击某条分析结果的"编辑"按钮

**Main Success Scenario:**
1. 用户点击"编辑"
2. 系统展示可编辑的字段（金额、商家、备注、标签）
3. 用户修改后确认
4. 系统更新结果列表中的对应条目
