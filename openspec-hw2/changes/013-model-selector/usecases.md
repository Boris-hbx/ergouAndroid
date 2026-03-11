## Use Cases

### Use Case: 切换对话模型

**Primary Actor:** 用户
**Scope:** 二狗 Android App — 设置模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 想知道当前用的什么模型，并能切换到另一个模型

**Preconditions:**
- App 已安装并可正常运行

**Success Guarantee (Postconditions):**
- 用户选中的模型已持久化到 DataStore
- 后续所有对话请求使用新选中的模型对应的 LLMService

**Trigger:** 用户在设置页点击「模型设置」

**Main Success Scenario:**
1. 用户打开设置页，看到「模型设置」条目，副标题显示当前模型（如 `Claude · claude-opus-4-6`）
2. 用户点击该条目，系统弹出模型选择对话框
3. 对话框列出所有可用模型，每个显示名称、型号、API Key 状态，当前选中项高亮
4. 用户选择目标模型
5. 系统将选择持久化到 DataStore，关闭对话框
6. 设置页副标题更新为新选中的模型

**Extensions:**
- 4a. 目标模型的 API Key 未设置：系统自动弹出该模型的 API Key 输入对话框，用户输入并保存后回到步骤 5
- 4b. 用户选择的就是当前模型：对话框关闭，无变更

---

### Use Case: 配置模型 API Key

**Primary Actor:** 用户
**Scope:** 二狗 Android App — 设置模块
**Level:** Subfunction

**Stakeholders and Interests:**
- 用户 — 需要为某个模型设置或更新 API Key 才能使用它

**Preconditions:**
- 模型选择对话框已打开

**Success Guarantee (Postconditions):**
- API Key 已安全存储到 DataStore
- 对话框中该模型的 Key 状态更新为「已设置」

**Trigger:** 用户点击模型选项中的「设置」或「修改」链接

**Main Success Scenario:**
1. 用户点击某模型的「设置」/「修改」链接
2. 系统弹出 API Key 输入对话框，标题显示模型厂商名，副标题显示型号
3. 用户输入 API Key
4. 用户点击保存，系统将 Key 写入 DataStore
5. 返回模型选择对话框，该模型 Key 状态变为绿色「已设置」

**Extensions:**
- 3a. 用户点击取消：关闭输入框，回到模型选择对话框，无变更
- 4a. 用户输入为空：保存按钮不可用

---

### Use Case: 使用选中模型进行对话

**Primary Actor:** 用户
**Scope:** 二狗 Android App — 对话模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 发送消息后，系统用选中的模型回复

**Preconditions:**
- 用户已选中一个模型且该模型的 API Key 已设置

**Success Guarantee (Postconditions):**
- 对话请求发送到正确的 LLM API 端点
- 响应正常流式展示

**Trigger:** 用户在对话页发送消息

**Main Success Scenario:**
1. 用户输入消息并发送
2. 系统读取 DataStore 中的 selectedModel
3. 系统将请求路由到对应的 LLMService 实现（DeepSeekService 或 ClaudeService）
4. LLMService 使用正确的 API 端点、Key 和模型参数发送请求
5. 流式响应正常展示给用户

**Extensions:**
- 3a. 选中模型的 API Key 已失效或错误：系统收到 API 错误，展示错误提示，建议用户检查 Key 设置

**Open Questions:**
- 是否需要在对话界面某处显示当前使用的模型名称？（当前方案：不显示，仅在设置页可见）
