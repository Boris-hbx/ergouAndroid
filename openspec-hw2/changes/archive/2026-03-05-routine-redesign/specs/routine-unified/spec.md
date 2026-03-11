## ADDED Requirements

### Requirement: 例行项数据模型统一为 Review
所有例行项 SHALL 使用 Next Review API (`/api/reviews`) 作为唯一数据源。系统 SHALL NOT 调用 Routine API (`/api/routines`)。例行项支持四种频率：daily、weekly、monthly、yearly。

#### Scenario: 加载例行项列表
- **WHEN** 用户进入例行页面且已登录
- **THEN** 系统调用 `GET /api/reviews` 获取全部例行项，按 frequency 字段分组到对应 tab

#### Scenario: 每日频率例行项
- **WHEN** 用户在"日" tab 查看例行项
- **THEN** 系统仅显示 frequency="daily" 的 Review 项，不再使用 Routine API 的 toggle/completed_today 机制

#### Scenario: 未登录状态
- **WHEN** 用户未登录 Next 账号
- **THEN** 系统显示登录引导页面，包含"前往设置"按钮

### Requirement: 频率 Tab 导航
例行页面 SHALL 提供 TabRow 包含四个 tab：日、周、月、年。切换 tab SHALL 筛选显示对应频率的例行项。

#### Scenario: 默认 tab
- **WHEN** 用户首次进入例行页面
- **THEN** 默认选中"日" tab

#### Scenario: 切换到周 tab
- **WHEN** 用户点击"周" tab
- **THEN** 系统显示 frequency="weekly" 的例行项

#### Scenario: 月 tab 不再合并季度
- **WHEN** 用户点击"月" tab
- **THEN** 系统仅显示 frequency="monthly" 的例行项，不包含 quarterly

### Requirement: 例行项列表卡片
每个例行项卡片 SHALL 显示：名称、到期状态标签、分类标签（如有）。已暂停的项 SHALL 使用降低对比度的样式。列表 SHALL 按到期紧急度排序：overdue > due_today > due_soon > upcoming > completed > paused。

#### Scenario: 显示到期状态
- **WHEN** 例行项 due_status 为 "overdue"
- **THEN** 卡片显示 due_label 文本，使用 error 颜色

#### Scenario: 显示即将到期
- **WHEN** 例行项 due_status 为 "due_soon"
- **THEN** 卡片显示 due_label 文本，使用 tertiary 颜色

#### Scenario: 已暂停的项
- **WHEN** 例行项 paused=true
- **THEN** 卡片使用 surfaceVariant 背景，显示"已暂停"标签

#### Scenario: 空列表
- **WHEN** 当前 tab 下没有例行项
- **THEN** 显示空状态提示（标题 + 副标题引导）

### Requirement: 添加例行项
系统 SHALL 提供统一的添加对话框，包含：名称输入框（必填）、频率选择（日/周/月/年，默认跟随当前 tab）、分类输入框（可选）。添加 SHALL 调用 `POST /api/reviews`。

#### Scenario: 添加每日例行
- **WHEN** 用户在"日" tab 点击 FAB，输入名称"晨跑"，保持频率默认
- **THEN** 系统调用 Review API 创建 frequency="daily" 的项，刷新列表

#### Scenario: 添加每周例行并指定分类
- **WHEN** 用户在"周" tab 点击 FAB，输入名称"周报"，填分类"工作"
- **THEN** 系统创建 frequency="weekly"、category="工作" 的项

#### Scenario: 名称为空时禁止提交
- **WHEN** 用户打开添加对话框但未输入名称
- **THEN** 确认按钮禁用

#### Scenario: 创建失败
- **WHEN** Review API 创建请求失败（网络错误等）
- **THEN** 系统显示错误 Snackbar，对话框保持打开

### Requirement: 例行项详情页
系统 SHALL 提供统一的详情页，所有频率共用。详情页 SHALL 显示：名称、频率标签、分类（如有）、到期状态、上次完成时间、创建时间。详情页 SHALL 提供操作按钮：标记完成（未暂停时）、暂停/恢复、删除。

#### Scenario: 查看详情
- **WHEN** 用户点击列表中的例行项
- **THEN** 系统导航到详情页，显示完整信息

#### Scenario: 标记完成
- **WHEN** 用户在详情页点击"标记完成"
- **THEN** 系统调用 `POST /api/reviews/:id/complete`，刷新数据，到期状态重新计算

#### Scenario: 暂停例行项
- **WHEN** 用户在详情页点击"暂停"
- **THEN** 系统调用 `PUT /api/reviews/:id` 设置 paused=true，到期状态不再计算

#### Scenario: 恢复例行项
- **WHEN** 用户在详情页点击"恢复"（当前 paused=true）
- **THEN** 系统调用 `PUT /api/reviews/:id` 设置 paused=false，重新计算到期状态

#### Scenario: 删除例行项
- **WHEN** 用户在详情页点击删除并确认
- **THEN** 系统调用 `DELETE /api/reviews/:id`，返回列表页，刷新数据

#### Scenario: 取消删除
- **WHEN** 用户点击删除后在确认对话框点击"取消"
- **THEN** 不执行任何操作，关闭对话框

### Requirement: 网络错误处理
所有 Review API 调用失败时 SHALL 显示错误 Snackbar，包含简短错误信息。系统 SHALL NOT 崩溃或显示空白页面。

#### Scenario: 列表加载失败
- **WHEN** GET /api/reviews 请求失败
- **THEN** 系统显示错误 Snackbar，列表保持上次成功的数据（如有）或显示空状态

#### Scenario: 操作失败
- **WHEN** 完成/暂停/删除操作的 API 调用失败
- **THEN** 系统显示错误 Snackbar "操作失败：{错误信息}"，UI 状态不变

#### Scenario: Activity 重建后恢复
- **WHEN** 系统因配置变化（如旋转屏幕）重建 Activity
- **THEN** ViewModel 保持状态，列表数据和当前 tab 不丢失

### Requirement: 二狗工具调用支持
系统 SHALL 提供工具让二狗通过对话管理例行项。原有 add_routine / check_routine / routine_stats 工具 SHALL 替换为基于 Review API 的工具。

#### Scenario: 通过对话添加例行项
- **WHEN** 用户在对话中说"帮我加个每周例行：整理桌面"
- **THEN** 二狗调用 create_review 工具，创建 frequency="weekly" 的 Review

#### Scenario: 通过对话查询例行项
- **WHEN** 用户在对话中说"我有哪些例行"
- **THEN** 二狗调用 query_reviews 工具，返回例行项列表

#### Scenario: 工具执行失败
- **WHEN** Review API 调用失败
- **THEN** 工具返回 ToolException，二狗告知用户失败原因

### Requirement: 快捷栏例行状态
快捷栏 SHALL 显示当前到期的例行项数量。到期定义为 due_status 属于 overdue / due_today / due_soon 的未暂停项。

#### Scenario: 有到期项
- **WHEN** 3 个例行项到期（overdue + due_today + due_soon）
- **THEN** 快捷栏显示 "例行: 3 项到期"

#### Scenario: 无到期项
- **WHEN** 没有到期的例行项
- **THEN** 快捷栏仅显示 "例行"

#### Scenario: 获取失败
- **WHEN** Review API 请求失败
- **THEN** 快捷栏显示 "例行"，不带数量

### Requirement: 移除打卡游戏化功能
系统 SHALL 移除以下功能：热力图 (HeatmapCard)、连续天数 (RoutineStreak)、每日完成率 (monthlyCompletionRate)、每日汇总卡片 (RoutineSummaryCard)、打卡圆圈按钮 (CheckCircleButton)。

#### Scenario: 日 tab 不显示热力图
- **WHEN** 用户在"日" tab 查看例行项
- **THEN** 列表直接展示例行项卡片，无热力图和汇总卡片

#### Scenario: 卡片不显示连续天数
- **WHEN** 任何例行项卡片渲染时
- **THEN** 卡片不包含连续天数信息和火焰图标
