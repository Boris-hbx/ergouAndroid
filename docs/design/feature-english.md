# 学习（English）功能设计文档

> 遵循 [功能页面通用设计规范](feature-screen-conventions.md)

## 现状

### UI
- Scaffold + TopAppBar("学习") + FAB
- FilterChip 按类别筛选：全部/英语/编程/职场/生活
- LazyColumn 展示场景卡片：emoji 图标 + 标题 + 英文标题 + 类别 + 状态徽章(draft/generating/generated)
- 操作：AI 生成（仅 draft 状态）+ 归档 + 删除
- 描述文本预览 + AI 生成内容预览（最多 6 行）
- 添加对话框：标题（必填）+ 类别选择 + 描述（可选）
- 登录门控 + 空状态 + 错误 Snackbar

### 工具
- `generate_english_scenario` — 创建并生成场景（title/category/description）

### API
- NextApiService: getScenarios, createScenario, getScenarioById, updateScenario, deleteScenario, generateScenario, archiveScenario

---

## 改进计划

### UI 改进

#### 1. 场景详情页
- 当前：卡片内直接预览内容（截断 6 行）
- 改进：点击卡片进入全屏详情页，完整显示 AI 生成内容
- 详情页支持 Markdown 渲染（复用 MarkdownText 组件）
- 底部操作栏：重新生成 / 归档 / 分享

#### 2. 生成状态优化
- "generating" 状态显示加载动画（脉冲效果或骨架屏）
- 生成完成后自动刷新卡片内容
- 生成失败显示重试按钮

#### 3. 卡片布局优化
- 类别用彩色标签区分（英语=蓝、编程=绿、职场=橙、生活=紫）
- 已生成的场景显示内容摘要第一行
- 归档场景移到单独的"已归档"标签页

#### 4. 学习进度
- 每个场景添加"已学习"标记
- 顶部统计：本周学习 N 个场景
- 按学习状态筛选：未学习 / 已学习 / 全部

#### 5. 添加对话框优化
- 增加热门场景推荐（"商务邮件"、"面试对话"等预设模板）
- 类别选择改为图标 + 文字的横向卡片

### 工具改进

#### 6. generate_english_scenario 增强
- 新增参数：`difficulty`（beginner/intermediate/advanced）
- 新增参数：`focus`（vocabulary/grammar/conversation/writing）
- 返回值包含生成的内容摘要

#### 7. 新增工具：`query_scenarios`
- 参数：category（可选）、status（draft/generated/archived，可选）、limit（默认 5）
- 返回场景列表
- 用途：二狗回答"我最近学了什么"

#### 8. 新增工具：`mark_scenario_learned`
- 参数：scenario_id（必填）
- 标记场景为已学习
- 用途：二狗根据对话判断用户已练习过该场景

### 集成

#### 9. 快捷栏推送
- 显示："学习·N 个场景"（已生成未学习的数量）

#### 10. 通知预留
- `getPendingReminders()`: 返回长时间未学习的提醒
- 二狗可据此提醒："你有 5 个英语场景还没学，要不要今天练一个？"

---

## 文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `ui/english/EnglishScreen.kt` | 修改 | 详情页、生成状态、类别颜色 |
| `ui/english/EnglishDetailScreen.kt` | 新增 | 场景详情全屏页（可选，也可内嵌） |
| `ui/english/EnglishViewModel.kt` | 修改 | 学习进度、详情加载 |
| `data/tool/tools/GenerateEnglishScenarioTool.kt` | 修改 | 增加 difficulty/focus 参数 |
| `data/tool/tools/QueryScenariosTool.kt` | 新增 | 查询场景列表工具 |
| `data/tool/tools/MarkScenarioLearnedTool.kt` | 新增 | 标记已学习工具 |
| `di/AppModule.kt` | 修改 | 注册新 Tool |
| `data/remote/ErgouPrompt.kt` | 修改 | 更新工具使用说明 |

## 依赖

- 学习进度标记需要 Next API 支持（可能需要 `PUT /api/english/scenarios/{id}` 添加 learned 字段）
- 场景详情页需要确认 API 返回的 content 字段是否为完整 Markdown
