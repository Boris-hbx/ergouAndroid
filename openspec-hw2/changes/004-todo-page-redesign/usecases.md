## Use Cases

### Use Case: 按时间维度查看待办任务

**Primary Actor:** 用户
**Scope:** Todo 页面
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 快速了解不同时间范围内有多少待办，聚焦当下最紧迫的任务

**Preconditions:**
- 用户已登录 Next 账号，有至少一个任务

**Success Guarantee (Postconditions):**
- 用户看到对应时间段的未完成任务列表和计数

**Trigger:** 用户进入 Todo 页面或切换 Tab

**Main Success Scenario:**
1. 用户进入 Todo 页面，默认显示"今天"Tab
2. 系统显示 Tab 栏，每个 Tab 带有未完成任务计数（如 `今天 4`）
3. 系统在 Tab 下方显示该时间段的未完成任务列表
4. 用户切换到"本周"或"30天"Tab
5. 系统刷新列表，显示对应时间段的任务

**Extensions:**
- 2a. 某个 Tab 下没有任务：显示计数为 0，列表区显示空态
- 5a. 网络请求失败：显示错误提示，保留上次数据

---

### Use Case: 查看已完成任务

**Primary Actor:** 用户
**Scope:** Todo 页面
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 回顾已完成的工作，确认遗漏

**Preconditions:**
- 当前 Tab 下有已完成的任务

**Success Guarantee (Postconditions):**
- 用户看到已完成任务列表

**Trigger:** 用户点击"已完成 (N)"折叠区

**Main Success Scenario:**
1. 用户在任务列表底部看到"已完成 (N)"折叠条
2. 用户点击折叠条
3. 系统展开已完成任务列表，任务标题有删除线效果
4. 用户再次点击折叠条
5. 系统收起已完成任务列表

**Extensions:**
- 1a. 当前 Tab 下没有已完成任务：不显示折叠条

---

### Use Case: 快速添加任务

**Primary Actor:** 用户
**Scope:** Todo 页面
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 快速记录待办，不被复杂表单打断思路

**Preconditions:**
- 用户已登录 Next 账号

**Success Guarantee (Postconditions):**
- 新任务创建成功并出现在列表中

**Trigger:** 用户点击 FAB（+）按钮

**Main Success Scenario:**
1. 用户点击右下角 FAB
2. 系统弹出添加任务对话框，焦点在标题输入框
3. 用户输入任务标题
4. 用户点击确认
5. 系统创建任务并刷新列表

**Extensions:**
- 3a. 用户需要设置更多信息：展开高级选项（四象限、描述、标签、截止日期）
- 4a. 标题为空：确认按钮禁用
