## Use Cases

### Use Case: 左滑删除 Trip

**Primary Actor:** 用户
**Scope:** 二狗差旅模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 快速删除不需要的差旅记录，操作可撤销避免误删

**Preconditions:**
- 用户已登录，差旅列表已加载

**Success Guarantee (Postconditions):**
- 目标 Trip 已从服务端删除
- 列表已更新

**Trigger:** 用户在差旅列表中左滑某个 Trip 项

**Main Success Scenario:**
1. 用户在差旅列表中左滑某个 Trip 项
2. 系统弹出确认对话框，询问是否删除
3. 用户确认删除
4. 系统乐观地从列表中移除该 Trip
5. 系统显示 Snackbar，包含"撤销"按钮（持续约 5 秒）
6. 超时后系统调用 API 真正删除该 Trip

**Extensions:**
- 3a. 用户取消：列表恢复原样，不做任何操作
- 5a. 用户点击"撤销"：系统将 Trip 恢复到列表中，取消删除
- 6a. API 删除失败：系统显示错误 Snackbar，将 Trip 恢复到列表中

---

### Use Case: 左滑删除费用项

**Primary Actor:** 用户
**Scope:** 二狗差旅模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 在差旅详情页快速删除某个费用项

**Preconditions:**
- 用户已进入某个 Trip 的详情页，费用项列表已展示

**Success Guarantee (Postconditions):**
- 目标费用项已从服务端删除
- 详情页费用列表已更新

**Trigger:** 用户在费用项列表中左滑某个费用项

**Main Success Scenario:**
1. 用户在费用项列表中左滑某个费用项
2. 系统弹出确认对话框
3. 用户确认删除
4. 系统乐观移除该费用项
5. 系统显示 Snackbar + 撤销按钮
6. 超时后系统调用 API 真正删除

**Extensions:**
- 3a. 用户取消：费用项恢复原位
- 5a. 用户点击"撤销"：费用项恢复，取消删除
- 6a. API 失败：显示错误，恢复费用项
- *a. 编辑对话框中的删除按钮：同样弹出确认对话框，流程与左滑一致

---

### Use Case: 为费用项上传照片

**Primary Actor:** 用户
**Scope:** 二狗差旅模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 为报销凭证（发票、收据）拍照存档

**Preconditions:**
- 用户已进入某个 Trip 的详情页，费用项已展开

**Success Guarantee (Postconditions):**
- 照片已上传到服务端，费用项照片列表已更新

**Trigger:** 用户点击费用项照片区域的添加按钮

**Main Success Scenario:**
1. 用户点击费用项下方的"添加照片"按钮
2. 系统弹出选择器（相册/拍照）
3. 用户选择照片来源并完成选取
4. 系统检查照片大小（≤ 10MB）
5. 系统上传照片到服务端
6. 系统刷新费用项，照片缩略图显示在照片行中

**Extensions:**
- 3a. 用户取消选择：无操作
- 4a. 照片超过 10MB：系统显示错误提示，不上传
- 5a. 上传失败：系统显示 Snackbar 错误信息

---

### Use Case: 删除费用项照片

**Primary Actor:** 用户
**Scope:** 二狗差旅模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 删除错误上传的照片

**Preconditions:**
- 费用项已有至少一张照片

**Success Guarantee (Postconditions):**
- 照片已从服务端删除，照片行已更新

**Trigger:** 用户点击照片缩略图右上角的删除按钮

**Main Success Scenario:**
1. 用户点击照片缩略图右上角的 X 按钮
2. 系统调用 API 删除该照片
3. 系统刷新费用项，照片从列表中移除

**Extensions:**
- 2a. API 删除失败：系统显示 Snackbar 错误信息，照片保留

**Open Questions:**
- 无
