## Why

用户对话后常不知道接下来可以问什么，导致对话中断。豆包等竞品通过"猜你想问"功能引导对话延续。二狗作为个人助手，追问建议可以帮用户更深入地利用已有功能（记忆、工具等）。

## What Changes

- AI 回复完成后，后台调用 LLM 生成 2-3 个建议追问
- 建议以可点击的 Chip 展示在最后一条 AI 消息下方
- 用户点击 Chip 直接作为输入发送
- 新一轮对话开始时清空上一轮建议

## Capabilities

### New Capabilities
- `suggested-followups`: AI 回复后的建议追问生成与展示

### Modified Capabilities

## Impact

- **ViewModel**: ChatViewModel 新增 suggestions 状态和生成逻辑
- **UI**: ChatScreen 在最后一条 AI 消息后展示 suggestion chips
- **LLM**: 额外一次轻量 LLM 调用（非流式，短 prompt）
- **无数据层变更**: 建议不持久化，仅内存中存在
