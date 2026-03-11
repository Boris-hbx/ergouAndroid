## 1. 数据层微调

- [x] 1.1 将 `ClaudeService.MODEL` 从 `claude-sonnet-4-20250514` 改为 `claude-opus-4-6`

## 2. UI — 模型选择弹窗

- [x] 2.1 在 `SettingsScreen.kt` 新增 `ModelOption` data class 和 `MODEL_OPTIONS` 列表
- [x] 2.2 新增 `ModelSelectorDialog` Composable：RadioButton 列表，每项显示名称 + 型号 + Key 状态 + 设置/修改链接
- [x] 2.3 弹窗内点击「设置/修改」打开对应的 API Key 输入弹窗（层叠弹窗状态管理）
- [x] 2.4 选中即保存：调用 `viewModel.onModelProviderChanged()`，Key 未设置时先弹 Key 输入框

## 3. UI — 设置页布局调整

- [x] 3.1 「账号」section 移除 DeepSeek API Key 和 Claude API Key 两个 SettingsItem
- [x] 3.2 新增「模型」section + 「模型设置」SettingsItem，副标题格式 `{Provider} · {modelId}`
- [x] 3.3 点击「模型设置」打开 ModelSelectorDialog

## 4. 手动测试

- [ ] 4.1 验证首次安装默认选中 DeepSeek，副标题显示 `DeepSeek · deepseek-chat`
- [ ] 4.2 验证切换到 Claude（Key 已设置）：弹窗关闭，副标题更新，发消息走 Claude API
- [ ] 4.3 验证切换到 Claude（Key 未设置）：自动弹出 Key 输入框，保存后切换生效
- [ ] 4.4 验证修改已有 Key：点击「修改」，输入新 Key，保存成功
- [ ] 4.5 验证 App 重启后模型选择保持不变
