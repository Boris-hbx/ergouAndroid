# Next-Ergou GAP Report: Routine (例行)

## Status: RESOLVED

通过 routine-redesign 变更，例行功能已统一到 Review API，原有 GAP 不再适用。

## 变更摘要

- 不再使用 Routine API (`/api/routines`)
- 全部使用 Review API (`/api/reviews`)，frequency="daily" 替代原 Routine
- 删除打卡游戏化功能（热力图、连续天数、完成率）

## 原 GAP 处置

| GAP | 原状态 | 处置 |
|-----|--------|------|
| GAP 1: 无频率字段 | Critical | 已解决 — Review 有 frequency 字段 |
| GAP 2: 无历史打卡记录 API | 降级 | 不再需要 — 已移除热力图功能 |
| GAP 3: 无连续打卡统计接口 | 降级 | 不再需要 — 已移除连续天数功能 |
| GAP 4: 协作例行信息有限 | 降级 | 保持 — Review 无协作功能，暂不需要 |
