## Use Cases

### Use Case: 调整任务进度

**Primary Actor:** 用户
**Scope:** 待办事项模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 精确调整任务完成进度，避免误操作完成任务

**Preconditions:**
- 用户已登录 Next 账号
- 存在至少一个未完成的待办事项

**Success Guarantee (Postconditions):**
- 任务进度已更新为用户设定的值
- 列表中的进度指示器反映新进度

**Trigger:** 用户打开任务详情

**Main Success Scenario:**
1. 用户点击某个待办事项，打开详情面板
2. 系统显示当前进度滑块和百分比
3. 用户拖动滑块调整进度到目标值（如 60%）
4. 系统即时显示新百分比，后台提交更新
5. 用户关闭详情面板，列表中该任务的进度指示器已更新

**Extensions:**
- 3a. 用户将进度滑到 100%：系统弹出确认对话框"确认完成该任务？"；用户确认 → 任务标记为已完成并移入已完成区；用户取消 → 进度回退到之前的值
- 4a. API 更新失败：系统回滚进度到操作前的值，显示错误提示"更新失败，请重试"

---

### Use Case: 编辑任务详情

**Primary Actor:** 用户
**Scope:** 待办事项模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 快速修改任务标题、描述等字段，操作直观无阻碍

**Preconditions:**
- 用户已打开任务详情底部面板

**Success Guarantee (Postconditions):**
- 修改的字段已保存
- 列表中对应任务显示更新后的内容

**Trigger:** 用户想要修改标题或描述

**Main Success Scenario:**
1. 用户点击标题区域（带有编辑提示图标）
2. 系统将标题切换为可编辑输入框，自动聚焦
3. 用户修改标题文本，按完成键确认
4. 系统即时保存修改，后台提交更新
5. 标题恢复为展示状态，显示新内容

**Extensions:**
- 1a. 用户点击描述区域：同理切换为可编辑输入框
- 4a. API 更新失败：系统回滚文本到修改前的值，显示错误提示

---

### Use Case: 操作任务后列表保持稳定

**Primary Actor:** 用户
**Scope:** 待办事项模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 操作单个任务后列表不闪烁，其余任务位置不变

**Preconditions:**
- 用户已登录，列表中有多个待办事项

**Success Guarantee (Postconditions):**
- 被操作的任务状态已更新
- 列表中其余任务未被重新加载，无闪烁

**Trigger:** 用户对任意任务执行操作（完成、删除、更新进度等）

**Main Success Scenario:**
1. 用户勾选某个任务为已完成
2. 系统立即在本地将该任务移至已完成区域
3. 系统在后台发送 API 请求
4. API 成功返回，本地状态与远端一致
5. 列表中其余任务未被打断或重绘

**Extensions:**
- 3a. API 请求失败：系统将任务移回未完成列表，显示错误提示"操作失败，请重试"
- 1a. 用户删除任务：系统立即从列表移除该任务（带 Snackbar 撤销选项），后台发送删除请求
- 1b. 用户更新进度：系统立即更新进度显示值，后台发送更新请求

---

### Use Case: 添加任务时展开高级选项

**Primary Actor:** 用户
**Scope:** 待办事项模块
**Level:** Subfunction

**Stakeholders and Interests:**
- 用户 — 展开/收起高级选项时过渡平滑，无卡顿感

**Preconditions:**
- 添加/编辑任务对话框已打开

**Success Guarantee (Postconditions):**
- 高级选项区域平滑展开或收起

**Trigger:** 用户点击"更多选项"/"收起选项"按钮

**Main Success Scenario:**
1. 用户点击"更多选项"按钮
2. 系统以平滑动画渐进展开高级选项区域（描述、优先级、标签、截止日期）
3. 用户填写需要的字段
4. 用户点击"收起选项"，系统以平滑动画收起区域

**Extensions:**
_(无异常流)_
