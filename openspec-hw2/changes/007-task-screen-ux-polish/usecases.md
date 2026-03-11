## Use Cases

### Use Case: 左滑删除任务（带确认）

**Primary Actor:** 用户
**Scope:** 二狗 App - 任务页面
**Level:** User goal

**Preconditions:**
- 任务列表中有至少一个任务

**Success Guarantee (Postconditions):**
- 用户确认后任务被软删除，Snackbar 提供撤销

**Trigger:** 用户左滑任务卡片

**Main Success Scenario:**
1. 用户左滑任务卡片，露出红色删除区域
2. 用户松手，系统弹出确认对话框"确定要删除「xxx」吗？"
3. 用户点击"删除"
4. 系统调用 deleteTodo，显示 Snackbar "已删除" + "撤销"按钮

**Extensions:**
- 2a. 用户滑动不足阈值松手：卡片回弹，不弹对话框
- 3a. 用户点击"取消"：对话框关闭，SwipeToDismissBox 重置到原位
- 4a. 用户点击"撤销"：调用 restoreTodo 恢复任务

---

### Use Case: 单击查看/编辑任务详情

**Primary Actor:** 用户
**Scope:** 二狗 App - 任务页面
**Level:** User goal

**Preconditions:**
- 任务列表中有至少一个任务

**Success Guarantee (Postconditions):**
- 用户可查看任务完整详情，修改的字段通过 updateTodo 保存

**Trigger:** 用户单击任务卡片

**Main Success Scenario:**
1. 用户单击任务卡片
2. 系统弹出 ModalBottomSheet 显示任务详情
3. 用户点击标题区域进入编辑态，修改后自动保存
4. 用户关闭 BottomSheet

**Extensions:**
- 3a. 用户编辑描述：点击描述区域进入编辑态
- 3b. 用户修改截止日期：点击日期弹出 DatePicker
- 3c. 用户点击"完成"按钮：调用 completeTodo
- 3d. 用户点击"删除"按钮：弹出确认对话框后删除
