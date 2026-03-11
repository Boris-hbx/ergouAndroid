## Why

二狗目前聊天记录、记忆、人物、灵魂状态、提醒等核心数据仅存本地 Room，换手机或重装后数据全部丢失。功能广场（Todo/记账/差旅等）已迁移 Next API，但对话和人格系统仍是本地孤岛。需要将全部数据上云，实现多设备同步，本地 Room 仅作缓存。

## What Changes

- 后端新增 6 组 REST API：sessions、messages、memories、people、reminders、soul
- NextApiService 新增对应端点调用
- ChatRepository / MemoryRepository / SoulRepository 改为「先写后端 → 同步本地缓存」模式
- Room 保留为离线缓存，新增 `syncedAt` 字段追踪同步状态
- 新增 SyncManager：登录后拉取全量数据写入本地缓存，后续增量同步
- ReminderDao 对应数据也迁移后端
- **BREAKING**: 本地 Room 数据库 schema 变更（新增 `remoteId`/`syncedAt` 字段），需要 migration

## Capabilities

### New Capabilities
- `cloud-sessions`: 会话和消息的后端存储与同步（CRUD + 历史拉取）
- `cloud-memories`: 记忆和人物的后端存储与同步（CRUD + 搜索 + 记忆强度）
- `cloud-soul`: 灵魂状态和演化日志的后端存储与同步（读写 + 日志追加）
- `cloud-reminders`: 提醒的后端存储与同步（CRUD + 完成标记）
- `data-sync`: 数据同步引擎（登录全量拉取 + 写操作同步 + 冲突处理）

### Modified Capabilities
（无已有 spec 需修改）

## Impact

- **后端**: Next.js 需新增 6 组 API 路由（sessions/messages/memories/people/reminders/soul）
- **NextApiService**: 新增 ~20 个端点方法
- **Repository 层**: ChatRepositoryImpl、MemoryRepositoryImpl、SoulRepositoryImpl 重构为云优先
- **Room**: 所有 Entity 新增 `remoteId: String?` 和 `syncedAt: Long` 字段，DB version 8→9
- **DAO**: 新增按 remoteId 查询/更新方法
- **新增**: SyncManager 类，处理登录同步和写操作同步
- **DI**: Koin 模块更新注入关系
