## Why

ReviewScreen 有两个 UX 短板：1) 删除按钮无确认直接删除，与其他页面不一致；2) 条目单击无响应，用户看不到详情。

## What Changes

- 删除操作改为左滑 → 确认对话框 → 乐观删除 + Snackbar 撤销
- 单击条目展开详情（频率、上次完成、到期状态、笔记、分类）
- 去掉 ReviewItem 中的删除 IconButton

## Capabilities

### New Capabilities
- `review-screen-ux`: ReviewScreen 的删除确认和展开详情交互优化

### Modified Capabilities

## Impact

- **UI 层**: 仅修改 `ReviewScreen.kt`，不改 ViewModel 或其他文件
