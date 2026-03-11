## Context

当前主界面 `ChatScreen.kt` 使用 `ModalNavigationDrawer` 作为功能入口和会话管理的承载容器。功能卡片（7个）和会话列表都在侧边抽屉 `SessionDrawer` 里，用户需要点击左上角菜单按钮才能看到。

设计参考文档 3.2 要求：快捷条直接显示在输入框上方，顶栏精简为 `二狗 [历史] [设置]`，侧边抽屉移除。

现有代码结构：
- `ChatScreen` → `ModalNavigationDrawer` 包裹 `Scaffold`（TopAppBar + ChatContent）
- `ChatContent` → LazyColumn（消息列表）+ Row（输入框）
- `SessionDrawer` → 功能卡片 LazyRow + 会话列表 LazyColumn
- `ChatViewModel` → `ChatUiState`（sessions, messages, streamingContent 等）
- `ErgouNavigation` → NavHost，chat 为 startDestination，11 个路由

## Goals / Non-Goals

**Goals:**
- 快捷条显示在输入框上方，一眼可见，一键跳转
- 会话历史独立页面，从顶栏 [历史] 按钮进入
- 新会话空态优化：问候语 + 建议话题卡片
- 顶栏精简：移除抽屉菜单按钮，右侧放历史和设置
- 移除 ModalNavigationDrawer 及 SessionDrawer

**Non-Goals:**
- 不做快捷条的使用频率统计（第一版仅按最近使用时间排序）
- 不做动态生成建议话题（第一版用固定话题列表）
- 不改动任何功能子页面（Task/Routine/Expense 等保持不变）
- 不改动 ChatViewModel 的核心对话逻辑

## Decisions

### D1: 快捷条数据存储 — DataStore Preferences

**决策**：用 DataStore Preferences 存储快捷条排序数据（一个 JSON 字符串，记录各功能的最近使用时间戳）。

**备选**：
- Room 新表：过重，只需存一个简单的排序列表
- SharedPreferences：已弃用，项目统一用 DataStore

**理由**：数据量极小（7 个功能的时间戳），DataStore 已在项目中使用（API Key、主题），无需新增依赖。

### D2: 快捷条组件 — LazyRow + AssistChip

**决策**：快捷条用 `LazyRow` + Material 3 `AssistChip` 实现，每个 chip 显示 `[分类]内容` 格式。

**备选**：
- 自定义 `Card` 组件：更灵活但增加代码量
- `FlowRow`：不支持横向滚动

**理由**：`AssistChip` 是 Material 3 标准组件，高度紧凑（约 32dp），天然适合标签式交互。`LazyRow` 支持横向滚动且按需渲染。

### D3: 会话历史 — 独立全屏页面

**决策**：新建 `SessionHistoryScreen.kt`，作为独立的 NavHost 路由（`"session-history"`），全屏显示会话列表。

**备选**：
- BottomSheet 弹出面板：交互体验好但实现复杂，且会话较多时展示空间不足
- 保留侧边抽屉仅展示会话：违背设计方向

**理由**：全屏页面最简单，与现有功能子页面（Task/Expense 等）保持一致的导航模式（左上角返回）。会话切换后自动 popBackStack 回到 chat。

### D4: 会话历史 ViewModel — 复用 ChatViewModel 的会话数据

**决策**：`SessionHistoryScreen` 通过导航参数回调与 `ChatViewModel` 交互，不新建 ViewModel。具体做法是在 `ErgouNavigation` 中用 `navBackStackEntry` 获取共享的 `ChatViewModel`。

**备选**：
- 新建 `SessionHistoryViewModel`：会话数据已经在 ChatViewModel 中管理，新建 ViewModel 需要重复注入 ChatRepository

**理由**：会话的 CRUD 操作都在 ChatViewModel 中，直接复用避免数据不同步。通过 Navigation 的 `sharedViewModel` 模式（`koinViewModel(viewModelStoreOwner = navBackStackEntry)` 在 NavGraph scope）实现共享。

### D5: ChatScreen 布局重构 — 移除 Drawer，Scaffold 直出

**决策**：移除 `ModalNavigationDrawer` 包裹层，`ChatScreen` 直接返回 `Scaffold`。TopAppBar 右侧放两个 `IconButton`（历史、设置）。

**改动点**：
```
之前: ModalNavigationDrawer { Scaffold { ChatContent } }
之后: Scaffold { Column { MessageList + ShortcutBar + InputRow } }
```

### D6: 空态设计 — 替换现有 WelcomeMessage

**决策**：替换现有的 `WelcomeMessage` composable，新版包含：
1. 二狗标题（保留）
2. 随机问候语（从预设列表随机选取）
3. 2-3 个建议话题 `SuggestionChip`，点击直接发送

**理由**：现有 `WelcomeMessage` 只有标题和一句话，缺乏交互性。建议话题降低用户的使用门槛。

## Risks / Trade-offs

- **[移除侧边抽屉是不可逆改动]** → 快捷条 + 会话历史页完全覆盖原有功能，风险可控。SessionDrawer 代码保留在 git 历史中。
- **[快捷条占据垂直空间]** → AssistChip 高度约 32dp + padding ≈ 40dp，对消息区域影响极小。在键盘弹出时可考虑隐藏快捷条（后续优化）。
- **[共享 ViewModel 的生命周期]** → 需确保 ChatViewModel 在 NavGraph scope 下创建，而不是单个 composable scope，否则离开 chat 路由后 ViewModel 会被销毁。使用 Koin 的 `navigation()` scope 或在 Activity scope 注入。
