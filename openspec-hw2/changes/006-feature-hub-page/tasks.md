## 1. FeatureHubScreen

- [x] 1.1 创建 `ui/hub/FeatureHubScreen.kt`：Scaffold + TopAppBar（返回+标题"功能广场"）+ LazyVerticalGrid 两列网格
- [x] 1.2 定义功能列表数据（route, emoji, name, description），包含 7 个功能模块
- [x] 1.3 实现功能卡片 composable：Card + emoji + 名称 + 简述，点击调用 onNavigateToFeature

## 2. ChatScreen 顶栏

- [x] 2.1 ChatScreen 新增 `onNavigateToFeatureHub` 回调参数
- [x] 2.2 TopAppBar 新增 navigationIcon：汉堡菜单按钮（Icons.Default.Menu），点击调用 onNavigateToFeatureHub

## 3. 导航

- [x] 3.1 ErgouNavigation 新增 `feature-hub` 路由，绑定 FeatureHubScreen
- [x] 3.2 ChatScreen 的 onNavigateToFeatureHub 绑定 `navController.navigate("feature-hub")`

## 4. 验证

- [ ] 4.1 手动测试：左上角汉堡菜单按钮可见，点击进入功能广场
- [ ] 4.2 手动测试：功能广场 7 个卡片全部显示，点击跳转正确
- [ ] 4.3 手动测试：返回按钮回到聊天页
