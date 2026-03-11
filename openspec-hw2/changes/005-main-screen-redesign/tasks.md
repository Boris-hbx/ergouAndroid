## 1. 快捷条数据层

- [x] 1.1 创建 `ShortcutBarRepository`：DataStore Preferences 存储各功能最近使用时间戳（JSON 字符串），提供 `getShortcuts(): Flow<List<ShortcutItem>>` 和 `recordUsage(route: String)`
- [x] 1.2 在 Koin AppModule 中注册 `ShortcutBarRepository` 为 single

## 2. 快捷条 UI 组件

- [x] 2.1 创建 `ShortcutBar` composable：LazyRow + AssistChip，显示功能条目（`[分类]内容` 格式），支持横向滚动
- [x] 2.2 定义默认快捷条目列表（任务、习惯、记账、差旅），首次使用时展示默认排序
- [x] 2.3 点击 chip 时调用 `onNavigateToFeature(route)` 跳转，并调用 `recordUsage` 更新排序

## 3. 顶栏重构

- [x] 3.1 修改 `ChatScreen` TopAppBar：移除左侧 Menu 按钮，右侧改为 [历史] + [设置] 两个 IconButton
- [x] 3.2 [历史] 按钮点击导航到 `session-history` 路由

## 4. 会话历史页面

- [x] 4.1 创建 `SessionHistoryScreen.kt`：全屏页面，顶栏 `← 返回 | 会话历史`，底部 FAB "新建对话"
- [x] 4.2 会话列表用 LazyColumn 展示（标题 + 最后消息摘要 + 时间），按时间倒序
- [x] 4.3 实现会话切换：点击条目 → 切换会话 → popBackStack 返回 chat
- [x] 4.4 实现会话删除：长按或左滑 → 确认对话框 → 删除（删除当前活跃会话时自动新建）
- [x] 4.5 实现空态提示：无会话时显示"还没有历史对话"

## 5. ChatViewModel 共享与导航

- [x] 5.1 调整 ChatViewModel 的 Koin 注入 scope，确保在 NavGraph 级别共享（Activity scope 或 navigation scope）
- [x] 5.2 在 `ErgouNavigation.kt` 中添加 `session-history` 路由，SessionHistoryScreen 接收共享的 ChatViewModel
- [x] 5.3 ChatViewModel 新增 `shortcutBarRepository` 依赖，暴露 `shortcuts: StateFlow<List<ShortcutItem>>`

## 6. 移除侧边抽屉

- [x] 6.1 从 `ChatScreen` 中移除 `ModalNavigationDrawer` 包裹层和 `drawerState`
- [x] 6.2 删除 `SessionDrawer`、`SessionItem`、`FeatureCard` composable（保留在 git 历史）
- [x] 6.3 清理相关 import（DrawerValue, ModalDrawerSheet, ModalNavigationDrawer, rememberDrawerState 等）

## 7. 空态欢迎优化

- [x] 7.1 重写 `WelcomeMessage` composable：居中显示二狗标题 + 随机问候语（从预设列表 `remember` 随机选取）
- [x] 7.2 添加 2-3 个建议话题 `SuggestionChip`，点击时将话题文本作为用户消息发送
- [x] 7.3 定义预设话题列表（如"查看今日待办"、"帮我记一笔账"、"聊聊最近怎么样"）

## 8. 集成与测试

- [x] 8.1 ChatScreen 组装：Scaffold → Column(MessageList + ShortcutBar + InputRow)，验证布局层级正确
- [x] 8.2 手动测试：快捷条显示、横向滚动、点击跳转、返回后排序更新（ADB 验证：快捷条显示正常，横向滚动正常，点击跳转 Todo 页成功，pin 功能锁定前 4 个位置）
- [ ] 8.3 手动测试：顶栏历史按钮 → 会话历史页 → 切换/新建/删除会话
- [x] 8.4 手动测试：新会话空态 → 问候语 + 建议话题 → 点击话题发起对话（ADB 验证：二狗标题 + 问候语 + 3 个建议话题显示正常）
- [ ] 8.5 手动测试：深色模式下所有新组件的显示效果
- [ ] 8.6 手动测试：屏幕旋转后状态恢复（会话、快捷条排序）
