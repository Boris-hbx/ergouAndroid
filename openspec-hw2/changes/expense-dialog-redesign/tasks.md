## Tasks

### 1. ViewModel 扩展 — 新增编辑状态和方法
- [x] 1.1 在 `ExpenseUiState` 新增 `editingExpenseId`, `editingEntry`, `editingPhotos` 等编辑状态
- [x] 1.2 在 `ExpenseViewModel` 新增 `openEditDialog()`, `openNewDialog()`, `saveEditChanges()`, `deleteEditExpense()` 等方法
- [x] 1.3 AI 分析完成后自动填充 title/amount/currency/date（通过 dialogPreview LaunchedEffect）
- [x] 1.4 保存时将 description 拼入 notes（用 `\n---\n` 分隔符）

### 2. ExpenseEditDialog — 核心 UI 重构
- [x] 2.1 创建 `ExpenseEditDialog.kt`，使用 `Dialog` + 可滚动 Column
- [x] 2.2 Header 区域：标题（"记一笔"/"编辑账单"）+ × 关闭按钮
- [x] 2.3 标题输入框：`OutlinedTextField`，TextFieldValue
- [x] 2.4 金额+币种同行：`Row { label + TextField(weight) + CurrencyChips }`
- [x] 2.5 日期/时间并排：DatePicker + TimePicker（HH:mm，Material3 TimePicker）
- [x] 2.6 明细卡片：双语显示（原始+翻译），带语言 tag badge（原始/中文翻译/英文翻译）
- [x] 2.7 CollapsibleDescription 组件：默认2行 maxLines，animateContentSize，展开/收起 toggle
- [x] 2.8 票据照片区域：支持已有照片（edit模式）和新照片（new模式）
- [x] 2.9 按钮布局：[让二狗分析] 全宽单独一行 + [删除?][取消][保存] 一行

### 3. ExpenseScreen 集成 — Dialog 触发
- [x] 3.1 在 `ExpenseUiState` 新增 `editingExpenseId: String?` 状态
- [x] 3.2 列表项点击改为调用 `viewModel.openEditDialog(id)` 而非导航
- [x] 3.3 "+" 按钮改为调用 `viewModel.openNewDialog()`
- [x] 3.4 在 ExpenseScreen 中根据 editingExpenseId 显示 ExpenseEditDialog
- [x] 3.5 Dialog 关闭后通过 clearDialogState + refreshAll 刷新列表

### 4. 导航清理
- [x] 4.1 移除 `ErgouNavigation.kt` 中的 `expense/{expenseId}` 和 `expense-scan` 路由
- [ ] 4.2 ExpenseDetailScreen/ViewModel 保留（暂不删除，避免破坏性变更）
- [x] 4.3 清理不再使用的 import（NavType, navArgument, ExpenseDetailScreen, DatePicker 等）

### 5. 手动测试
- [ ] 5.1 新建空白账单：Dialog 弹出，字段顺序正确，保存成功
- [ ] 5.2 让二狗分析：AI 回填标题/金额/时间/明细/描述
- [ ] 5.3 编辑已有账单：数据预填正确，修改后保存成功
- [ ] 5.4 描述折叠：默认2行无滚动条，展开/收起正常
- [ ] 5.5 明细双语：中英文 tag 正确显示
- [ ] 5.6 删除账单：确认弹窗 → 删除 → 列表刷新
- [ ] 5.7 让二狗分析在所有状态都可用（新建/已分析/编辑）
