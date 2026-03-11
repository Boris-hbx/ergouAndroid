## Overview

将记账编辑从全屏 `ExpenseDetailScreen` 改为 Dialog 弹窗形式。复用现有 `ExpenseDetailViewModel` 逻辑，UI 层从 Screen 改为 Dialog Composable。参考已确认的 HTML mockup (`docs/demo-html/expense-dialog-redesign.html`)。

## Approach

### 核心改动：Screen → Dialog

将 `ExpenseDetailScreen.kt` 重构为 `ExpenseEditDialog.kt`，使用 `AlertDialog` 或自定义 `Dialog(onDismissRequest)` 包裹。Dialog 内容为可滚动的 Column。

**不使用 ModalBottomSheet** — Dialog 居中弹窗更适合编辑表单，且内容较多时不需要处理 sheet 高度问题。

### UI 结构

```
Dialog
├── Header: "记一笔" / "编辑账单" + × 关闭
├── 标题: OutlinedTextField
├── 金额行: Row { label + OutlinedTextField + CurrencyChips }
├── 日期/时间行: Row { DateBar(clickable) + TimeBar(clickable) }
├── 明细卡片: ItemsCard (如有)
│   └── 每项: Row { Column(原始名+tag, 翻译名+tag) + qty + amount }
├── 描述: CollapsibleDescription
│   └── TextField + 展开/收起 toggle
├── 票据照片: PhotoRow
├── [让二狗分析] 全宽按钮
└── 操作行: [删除?] [取消] [保存]
```

### 关键组件

#### CollapsibleDescription
- 使用 `TextField` + `maxLines` 控制折叠
- 折叠态: `maxLines = 2`，展开态: `maxLines = Int.MAX_VALUE`
- `expanded: Boolean` 状态由 `remember` 本地管理
- 通过 `onValueChange` 回调更新 ViewModel 中的描述值

#### 金额+币种同行
- `Row { Text("金额") + OutlinedTextField(modifier = Modifier.weight(1f)) + CurrencyChipRow }`
- 币种 chips 使用 `FilterChip` / `AssistChip`，支持 CAD/CNY

#### 日期/时间并排
- 复用现有 DatePicker 逻辑
- 新增 TimePicker: `TimePickerDialog` (Material3) 或自定义
- 时间格式 HH:mm，未设置时显示 placeholder "票据时间"

#### 明细双语显示
- 复用现有 items 数据结构
- 每项: `Column { Row(原始名 + TagBadge("原始")), Row(翻译名 + TagBadge("中/英文翻译")) }`
- Tag badge: `Surface(color = primaryContainer, shape = RoundedCornerShape(4.dp))`

### ViewModel 改动

在 `ExpenseDetailUiState` 中新增:
- `description: TextFieldValue` — 描述字段（中文 IME 兼容）
- `time: String?` — 票据时间 (HH:mm)，null 表示未设置

在 `ExpenseDetailViewModel` 中新增:
- `updateDescription(TextFieldValue)` 方法
- `updateTime(String)` 方法
- AI 分析回调中填充 description 和 time 字段

### 导航改动

- `ExpenseScreen` 中: 点击列表项不再 navigate 到 detail route，改为设置 `editingExpenseId` 状态触发 Dialog
- 点击 "+" 设置 `editingExpenseId = "new"` 触发新建 Dialog
- Dialog 关闭后清空 `editingExpenseId`

### 文件变更

| 文件 | 操作 |
|------|------|
| `ExpenseDetailScreen.kt` | 重构为 `ExpenseEditDialog.kt` |
| `ExpenseDetailViewModel.kt` | 新增 description/time 字段和方法 |
| `ExpenseScreen.kt` | 改用 Dialog 替代导航，管理 editingExpenseId |
| `ExpenseViewModel.kt` | 新增 editingExpenseId 状态 |
| `ErgouNavigation.kt` | 移除 expense detail 路由（如存在） |

## Risks

- Dialog 内容较多可能超出屏幕：使用 `verticalScroll` 确保可滚动
- TimePicker 在 Material3 中可能需要 `@ExperimentalMaterial3Api`
- AI 分析填充描述需要后端返回 description 字段或前端拼接
