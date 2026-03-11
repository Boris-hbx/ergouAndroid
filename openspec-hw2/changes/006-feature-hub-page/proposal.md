## Why

当前功能入口分散：快捷条显示 4-5 个常用功能，但二狗共有 7 个功能模块（任务/习惯/回顾/记账/差旅/学习/养生）。用户没有一个统一的地方浏览和进入所有功能。参考豆包的"发现"页和小艺的"技能广场"，需要一个功能广场作为完整的功能入口。

## What Changes

- 顶栏左上角新增**汉堡菜单按钮**（三横线图标）
- 点击进入独立的**功能广场页面** (`FeatureHubScreen`)
- 功能广场以网格卡片形式展示所有功能模块
- 快捷条保留不变（高频入口），功能广场是完整入口

## Capabilities

### New Capabilities
- `feature-hub`: 功能广场独立页面，网格展示所有功能模块入口

### Modified Capabilities

## Impact

- **UI**: 新增 `FeatureHubScreen.kt`，`ChatScreen.kt` 顶栏新增按钮
- **导航**: `ErgouNavigation.kt` 新增 `feature-hub` 路由
- **无数据层变更**: 纯 UI/导航改造
