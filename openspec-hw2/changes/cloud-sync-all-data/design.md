## Context

二狗当前数据分两层：功能广场数据（Todo/记账/差旅等）已通过 NextApiService 存储在 fly.dev 后端；聊天记录、记忆、人物、灵魂状态、提醒仍仅存 Room 本地。需要将后者全部上云，Room 降级为缓存层。

现有 NextApiService 已建立了与后端交互的模式（Ktor + session cookie auth），新增端点遵循相同模式即可。

## Goals / Non-Goals

**Goals:**
- 全部用户数据存储在 Next.js 后端，支持多设备同步
- Room 作为本地缓存，读操作从本地走，保证性能
- 写操作 write-through：先写后端 → 再更新本地
- 后端不可用时降级为本地写入，恢复后同步
- 登录时全量拉取，登出时清空本地

**Non-Goals:**
- 实时双向同步（WebSocket push）— 当前单用户单设备场景不需要
- 端到端加密 — 后续再做
- 增量同步（delta sync）— 数据量小，全量拉取即可
- 冲突合并策略 — 后端数据优先，本地未同步数据 push 补全

## Decisions

### 1. Write-through + local fallback 模式
**选择**: 写操作先调后端 API，成功后更新本地 Room；失败时写本地并标记 `remoteId = null`。
**备选**: Offline-first (先写本地再异步推后端) — 更复杂，需要冲突解决，当前场景过度设计。
**理由**: 二狗始终在线使用，网络断开是异常情况而非常态。write-through 简单可靠。

### 2. 后端 API 设计跟随现有 Next.js 模式
**选择**: REST endpoints 在 Next.js route handlers，Prisma ORM，与现有 todo/expense 等 API 风格一致。
**理由**: 不引入新技术栈，后端开发成本最低。

### 3. Room Entity 添加 `remoteId` + `syncedAt` 字段
**选择**: 所有需要同步的 Entity 添加 `remoteId: String?`（后端 MongoDB ObjectId）和 `syncedAt: Long`（最后同步时间戳）。
**备选**: 单独的 sync status 表 — 增加复杂度，不值得。
**理由**: 直接在 Entity 上标记同步状态最简单，查询 `WHERE remoteId IS NULL` 即可找到未同步记录。

### 4. SyncManager 作为同步协调器
**选择**: 新建 `SyncManager` 类，注入 NextApiService + 所有 DAO，提供 `fullSync()` 和 `pushUnsynced()` 方法。
**理由**: 集中管理同步逻辑，避免散落在各 Repository 中。

### 5. 全量同步而非增量
**选择**: 登录时拉取全部数据覆盖本地。
**备选**: 基于 timestamp 的增量同步 — 需要后端支持 `?since=timestamp`，复杂度高。
**理由**: 个人助手数据量小（预计 <10MB），全量拉取足够快。

### 6. Room migration v8→v9
**选择**: 单次 migration 为所有 Entity 添加 `remoteId` 和 `syncedAt` 列。
**理由**: 一次 migration 比多次简单，所有 Entity 同时升级。

### 7. 聊天消息的同步时机
**选择**: 用户消息发送时立即同步；AI 回复在流式结束后同步完整内容。
**理由**: 流式过程中逐 token 同步无意义，等完整回复后一次写入。

## Risks / Trade-offs

- **[风险] 后端 API 开发量大（6 组 API ~20 端点）** → 后端 API 模式已成熟，可快速复制
- **[风险] 全量同步在数据量增长后变慢** → 当前可接受；未来可加 `?since=` 增量同步
- **[风险] 本地未同步数据在切换账号时丢失** → 登出前提示用户
- **[权衡] write-through 增加每次写操作延迟** → 后端在同一区域（fly.dev），延迟可接受（<200ms）
- **[权衡] 后端优先冲突策略可能丢失本地修改** → 单用户单设备场景几乎不会冲突

## Migration Plan

1. **后端先行**: 先在 Next.js 部署全部 API 端点
2. **Android Room migration**: v8→v9 添加同步字段
3. **NextApiService 扩展**: 添加新端点调用方法
4. **SyncManager**: 实现全量同步和 push unsynced
5. **Repository 重构**: ChatRepository / MemoryRepository / SoulRepository 改为 write-through
6. **登录流程集成**: 登录成功后触发 fullSync，登出清空 Room
7. **测试**: 全流程验证（新设备登录 → 使用 → 换设备 → 数据完整）

回滚：Android 端可回退到纯本地模式（remoteId 字段不影响现有逻辑），后端 API 可独立下线。
