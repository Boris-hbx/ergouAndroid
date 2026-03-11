## Use Cases

### Use Case: Delete review with confirmation and undo

**Primary Actor:** User
**Scope:** Ergou App (审视模块)
**Level:** User goal

**Main Success Scenario:**
1. User 左滑某条审视条目。
2. System 显示红色删除背景。
3. User 松手，System 弹出确认对话框。
4. User 确认删除。
5. System 乐观移除条目，显示 Snackbar "已删除" + "撤销"按钮。
6. Snackbar 超时后，System 调用 API 真正删除。

**Extensions:**
- 4a. User 取消删除：SwipeToDismissBox 重置，条目恢复原位。
- 5a. User 点击撤销：条目恢复显示，不调用删除 API。

---

### Use Case: Expand review item to see details

**Primary Actor:** User
**Scope:** Ergou App (审视模块)
**Level:** User goal

**Main Success Scenario:**
1. User 单击某条审视条目。
2. System 用 AnimatedVisibility 展开详情区域，显示频率、上次完成时间、到期状态、笔记、分类。
3. User 再次单击条目。
4. System 收起详情区域。
