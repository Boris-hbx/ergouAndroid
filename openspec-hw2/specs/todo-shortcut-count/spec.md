## ADDED Requirements

### Requirement: 快捷栏显示动态待办数量
聊天页面快捷栏的"待办"按钮 SHALL 显示今日未完成待办数量，格式为"待办 N 项"。

#### Scenario: 有未完成待办
- **WHEN** 用户今日有 5 个未完成待办
- **THEN** 快捷栏按钮显示"待办 5 项"

#### Scenario: 所有待办已完成
- **WHEN** 用户今日所有待办都已完成
- **THEN** 快捷栏按钮显示"待办"（不带数字）

#### Scenario: 无任何待办
- **WHEN** 用户今日没有任何待办任务
- **THEN** 快捷栏按钮显示"待办"（不带数字）

### Requirement: 从 Next API 获取待办计数
ShortcutBarRepository SHALL 调用 GET /api/todos/counts?tab=today 获取实时计数。

#### Scenario: 页面加载时拉取
- **WHEN** 聊天页面加载或从其他页面返回时
- **THEN** 系统发起 GET /api/todos/counts?tab=today 请求，更新快捷栏数字

#### Scenario: 网络请求失败
- **WHEN** 计数请求返回错误或网络不可用
- **THEN** 快捷栏显示"待办"（不带数字），不阻塞其他功能

#### Scenario: 用户未登录
- **WHEN** 用户未登录 Next 账号
- **THEN** 快捷栏不显示待办数量

### Requirement: NextApiService 新增计数接口
NextApiService SHALL 新增 getTodoCounts(tab: String) 方法。

#### Scenario: 调用计数 API
- **WHEN** ShortcutBarRepository 请求今日计数
- **THEN** NextApiService 发送 GET /api/todos/counts?tab=today，解析返回的各象限计数，求和未完成数
