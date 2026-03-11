## Use Cases

### Use Case: 左滑删除例行项

**Primary Actor:** 用户
**Scope:** 二狗 App 例行模块
**Level:** User goal

**Preconditions:**
- 用户已登录，例行列表有数据

**Success Guarantee (Postconditions):**
- 例行项从后端删除，列表不再显示

**Trigger:** 用户在列表中对某项从右往左滑动

**Main Success Scenario:**
1. 用户左滑列表项，露出红色背景和删除图标
2. 用户松手，系统弹出确认对话框
3. 用户点击"删除"确认
4. 系统立即从列表中隐藏该项，显示 Snackbar "已删除" + "撤销"按钮
5. Snackbar 自然消失，系统调用后端删除 API

**Extensions:**
- 2a. 用户未完全滑到位，松手后自动回弹，无事发生
- 3a. 用户点击"取消"：对话框关闭，滑动状态重置，列表不变
- 4a. 用户点击 Snackbar "撤销"：条目恢复显示，不调用删除 API

### Use Case: 详情页删除例行项

**Primary Actor:** 用户
**Scope:** 二狗 App 例行模块
**Level:** User goal

**Preconditions:**
- 用户在例行项详情页

**Success Guarantee (Postconditions):**
- 例行项从后端删除，返回列表

**Trigger:** 用户点击详情页删除按钮

**Main Success Scenario:**
1. 用户点击详情页右上角删除按钮
2. 系统弹出确认对话框
3. 用户确认删除
4. 系统返回列表页，从列表隐藏该项，显示 Snackbar + 撤销
5. Snackbar 消失后调用后端删除

**Extensions:**
- 3a. 用户取消：关闭对话框，留在详情页
- 4a. 用户撤销：条目恢复显示
