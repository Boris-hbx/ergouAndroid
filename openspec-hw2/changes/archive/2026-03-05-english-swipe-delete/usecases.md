## Use Cases

### Use Case: 左滑删除学习场景

**Primary Actor:** 用户
**Scope:** EnglishScreen
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 安全地删除不需要的学习场景，误操作可撤销

**Preconditions:**
- 用户已登录，场景列表中有至少一个场景

**Success Guarantee (Postconditions):**
- 场景从列表和后端删除，或用户撤销后场景恢复

**Trigger:** 用户左滑某个场景卡片

**Main Success Scenario:**
1. 用户左滑场景卡片，露出红色删除背景
2. 松手后系统弹出确认对话框
3. 用户点击"删除"
4. 场景从列表中消失（乐观更新）
5. 系统显示 Snackbar "已删除" + "撤销"按钮
6. Snackbar 超时，系统调用后端删除 API

**Extensions:**
- 2a. 用户点击"取消"：SwipeToDismissBox 重置到原位，场景保留
- 5a. 用户点击"撤销"：场景恢复到列表，不调用后端删除
