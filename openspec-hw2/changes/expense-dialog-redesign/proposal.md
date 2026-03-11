## Why

当前记账编辑界面（detail page）布局不够紧凑，字段顺序不合理，缺少"描述"字段和"让二狗分析"功能。用户希望将编辑界面改为弹窗 Dialog 形式，优化字段布局（标题在最上面、金额+币种同行、描述可折叠、票据时间精确到时分），并在所有状态下都保留"让二狗分析"按钮。已有 HTML mockup 确认设计方案（`docs/demo-html/expense-dialog-redesign.html`）。

## What Changes

- 将记账编辑从独立页面改为 BottomSheet / Dialog 弹窗
- 字段重新排序：标题 → 金额+币种（同行） → 日期/时间 → 明细 → 描述（可折叠） → 票据照片
- 新增"描述"字段：AI 分析后自动填充概述（可带古文点缀），用户也可手动编辑
- "让二狗分析"按钮在所有状态（新建空白、AI分析后、编辑已有）都显示，单独一行全宽
- 明细区域重新设计：双语显示（原始+翻译），带 tag 标识语言
- 票据时间精确到时分，AI 可从票据中提取

## Capabilities

### New Capabilities
- `expense-dialog-ui`: 记账弹窗 UI 重构（布局、字段顺序、描述折叠、按钮排列）
- `expense-ai-description`: AI 分析生成描述摘要，填充到描述字段

### Modified Capabilities
- `expense-edit`: 编辑流程从独立页面改为弹窗，新增描述字段，时间精确到时分

## Impact

- `ExpenseScreen.kt` — 新增/编辑改为 Dialog/BottomSheet 弹窗
- `ExpenseViewModel.kt` — 新增描述字段状态、AI分析触发逻辑
- `NextApiService.kt` / `NextModels.kt` — 可能需要扩展 description 字段
- 导航：移除 expense detail 独立路由（如当前存在），改为弹窗内操作
