## Use Cases

### Use Case: 左滑删除任务

**Primary Actor:** 用户
**Scope:** 二狗 App - 任务页面
**Level:** User goal

**Stakeholders and Interests:**
- 用户 - 快速删除不需要的任务，减少操作步骤

**Preconditions:**
- 用户已登录且任务列表中有至少一个任务

**Success Guarantee (Postconditions):**
- 任务被软删除（deleted=true），从列表中消失
- 可通过后端 restore API 恢复

**Trigger:** 用户在任务卡片上向左滑动

**Main Success Scenario:**
1. 用户在任务卡片上向左滑动
2. 系统露出红色删除背景和删除图标
3. 用户滑动超过阈值后释放
4. 系统调用 DELETE /api/todos/:id 执行软删除
5. 任务从列表中移除，显示 Snackbar 提示"已删除"并附带"撤销"按钮

**Extensions:**
- 3a. 用户滑动未超过阈值后释放：卡片弹回原位，不执行删除
- 4a. 网络请求失败：卡片弹回原位，显示错误 Snackbar
- 5a. 用户点击"撤销"：调用 POST /api/todos/:id/restore 恢复任务

---

### Use Case: 长按编辑任务

**Primary Actor:** 用户
**Scope:** 二狗 App - 任务页面
**Level:** User goal

**Stakeholders and Interests:**
- 用户 - 修改已有任务的内容、标签、到期日、象限等属性

**Preconditions:**
- 用户已登录且任务列表中有至少一个任务

**Success Guarantee (Postconditions):**
- 任务属性已通过 PUT /api/todos/:id 更新
- 列表即时刷新显示更新后的内容

**Trigger:** 用户长按任务卡片

**Main Success Scenario:**
1. 用户长按任务卡片
2. 系统弹出编辑对话框，预填当前任务的标题、描述、标签、到期日、象限
3. 用户修改所需字段
4. 用户确认提交
5. 系统调用 PUT /api/todos/:id 更新任务
6. 对话框关闭，列表刷新显示更新后的内容

**Extensions:**
- 2a. 任务数据加载失败：显示错误提示，不弹出对话框
- 4a. 用户取消编辑：对话框关闭，不做任何修改
- 5a. 网络请求失败：显示错误 Snackbar，对话框保持打开以便重试

---

### Use Case: 拖拽调整任务顺序

**Primary Actor:** 用户
**Scope:** 二狗 App - 任务页面
**Level:** User goal

**Stakeholders and Interests:**
- 用户 - 按个人优先级自定义任务排列顺序

**Preconditions:**
- 用户已登录且当前 Tab 下有至少两个未完成任务

**Success Guarantee (Postconditions):**
- 任务的 sort_order 已更新，新顺序持久化到后端
- 下次加载列表时保持用户排列的顺序

**Trigger:** 用户长按任务卡片并开始拖动

**Main Success Scenario:**
1. 用户长按任务卡片触发拖拽模式
2. 系统高亮被拖拽的卡片（阴影/缩放效果）
3. 用户将卡片拖到目标位置
4. 系统实时显示插入位置的占位指示
5. 用户释放卡片
6. 系统计算新的 sort_order（取目标位置前后两项的中间值）
7. 系统调用 PUT /api/todos/:id 更新 sort_order
8. 列表以新顺序显示

**Extensions:**
- 5a. 用户拖回原位释放：不发起 API 请求
- 7a. 网络请求失败：回退到拖拽前的顺序，显示错误提示

**Open Questions:**
- 拖拽手势与长按编辑是否冲突？需要区分短长按（编辑）和长按+移动（拖拽）
- 已完成任务折叠区是否允许拖入/拖出？

---

### Use Case: 按标签筛选任务

**Primary Actor:** 用户
**Scope:** 二狗 App - 任务页面
**Level:** User goal

**Stakeholders and Interests:**
- 用户 - 快速查看某个标签下的所有任务

**Preconditions:**
- 用户已登录且任务列表中有带标签的任务

**Success Guarantee (Postconditions):**
- 列表仅显示包含所选标签的任务
- 筛选状态可视化呈现（选中的标签高亮）

**Trigger:** 用户点击任务卡片上的标签

**Main Success Scenario:**
1. 用户点击某个任务卡片上的标签
2. 系统将该标签设为筛选条件，高亮显示
3. 列表过滤为仅包含该标签的任务
4. 用户再次点击已选中的标签
5. 系统清除筛选，恢复显示全部任务

**Extensions:**
- 2a. 筛选后无匹配任务：显示空状态提示"该标签下没有任务"
- 3a. 用户点击另一个标签：切换筛选到新标签（单选模式）

---

### Use Case: 查看快捷栏待办数量

**Primary Actor:** 用户
**Scope:** 二狗 App - 聊天页面快捷栏
**Level:** Subfunction

**Stakeholders and Interests:**
- 用户 - 无需进入任务页面即可了解今日待办情况

**Preconditions:**
- 用户已登录 Next 账号

**Success Guarantee (Postconditions):**
- 快捷栏显示实时的今日未完成待办数量

**Trigger:** 聊天页面加载或从任务页面返回时

**Main Success Scenario:**
1. 系统从 GET /api/todos/counts?tab=today 获取今日待办计数
2. 快捷栏的"待办"按钮显示"待办 N 项"（N 为未完成数）
3. 用户完成或添加任务后返回聊天页面
4. 系统重新拉取计数，快捷栏数字更新

**Extensions:**
- 1a. 网络请求失败：快捷栏显示"待办"（不带数字），不阻塞其他功能
- 1b. 用户未登录：快捷栏不显示数量
