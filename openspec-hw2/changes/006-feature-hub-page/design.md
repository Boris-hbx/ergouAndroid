## 背景

ChatScreen 顶栏当前：标题"二狗" + 右侧 [历史] [设置]。快捷条已有 4-5 个常用功能入口。需要在左上角加汉堡菜单按钮进入完整功能广场。

## 目标 / 非目标

**目标：**
- 顶栏左上角加汉堡菜单按钮
- 新建 FeatureHubScreen，网格展示 7 个功能模块
- 导航路由注册

**非目标：**
- 功能广场内的搜索/推荐
- 自定义排序

## 设计决策

### 1. ChatScreen 顶栏改造

```kotlin
TopAppBar(
    title = { Text("二狗") },
    navigationIcon = {
        IconButton(onClick = onNavigateToFeatureHub) {
            Icon(Icons.Default.Menu, contentDescription = "功能广场")
        }
    },
    actions = {
        IconButton(onClick = onNavigateToSessionHistory) { ... }
        IconButton(onClick = onNavigateToSettings) { ... }
    }
)
```

ChatScreen 新增 `onNavigateToFeatureHub` 回调参数。

### 2. FeatureHubScreen

新文件 `ui/hub/FeatureHubScreen.kt`：

```
┌──────────────────────────────┐
│  ← 返回    功能广场            │
├──────────────────────────────┤
│                              │
│  ┌────────┐  ┌────────┐     │
│  │ 📋 任务 │  │ 🔄 习惯 │     │
│  └────────┘  └────────┘     │
│  ┌────────┐  ┌────────┐     │
│  │ 🔁 回顾 │  │ 💰 记账 │     │
│  └────────┘  └────────┘     │
│  ┌────────┐  ┌────────┐     │
│  │ ✈️ 差旅 │  │ 📖 学习 │     │
│  └────────┘  └────────┘     │
│  ┌────────┐                 │
│  │ 🧘 养生 │                 │
│  └────────┘                 │
│                              │
└──────────────────────────────┘
```

- 使用 `LazyVerticalGrid(columns = GridCells.Fixed(2))` 两列网格
- 每个卡片：`Card` + emoji + 名称 + 简述
- 点击调用 `onNavigateToFeature(route)`

### 3. 导航

`ErgouNavigation.kt` 新增：
```kotlin
composable("feature-hub") {
    FeatureHubScreen(
        onBack = { navController.popBackStack() },
        onNavigateToFeature = { route -> navController.navigate(route) }
    )
}
```

ChatScreen 的 `onNavigateToFeatureHub` 绑定：
```kotlin
onNavigateToFeatureHub = { navController.navigate("feature-hub") }
```

## 风险 / 权衡

无显著风险。纯 UI 新增页面，不影响现有功能。
