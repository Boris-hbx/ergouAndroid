## Why

当前 Todo 页面（TaskScreen）功能基本完整，但视觉体验和交互设计较粗糙：Tab 只有文字标签没有计数，没有已完成任务的折叠区，添加任务的弹窗过于复杂。设计参考文档 3.3 给出了明确的 Todo 重设计方案——今日/本周/30天视图 + 已完成折叠 + FAB 添加，需要按设计稿落地。

## What Changes

- **Tab 栏增加计数徽标**：`[Today 4] [This Week] [30D]`，显示各时间段未完成任务数
- **任务卡片重设计**：更紧凑的布局，左侧勾选框 + 标题 + 四象限标签，去掉进度条（Todo 不需要）
- **已完成任务折叠区**：底部可展开/收起的"已完成 (N)"区域，默认收起
- **添加任务简化**：FAB 点击后弹出轻量输入框（仅标题），高级选项可展开
- **Tab 改为 today/week/month 对应 今天/本周/30天**：与设计稿对齐

## Capabilities

### New Capabilities
- `todo-tab-counts`：Tab 栏显示各时间段未完成任务计数
- `todo-completed-section`：已完成任务折叠区，支持展开/收起
- `todo-card-redesign`：任务卡片视觉重设计，更紧凑高效

### Modified Capabilities
（无现有 spec 需要修改）

## Impact

- **UI 层**：`TaskScreen.kt` 主要改动（Tab、卡片、已完成区、添加弹窗）
- **ViewModel**：`TaskViewModel.kt` 需要分离已完成/未完成列表，增加计数逻辑
- **无数据层改动**：API 接口和 DTO 保持不变
- **无 breaking change**：纯 UI 重构
