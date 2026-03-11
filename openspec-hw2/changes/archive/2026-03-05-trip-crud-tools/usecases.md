## Use Cases

### Use Case: 更新出差信息

**Primary Actor:** 用户（通过对话）
**Scope:** 二狗 AI 助手 — Tool 调用系统
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 修正或补充出差记录的标题、目的地、日期、目的等信息

**Preconditions:**
- 用户已登录 Next 后端
- 目标出差记录已存在

**Success Guarantee (Postconditions):**
- 出差记录已按用户指示更新

**Trigger:** 用户在对话中表达更新出差信息的意图（如"把上次北京出差的目的地改成上海"）

**Main Success Scenario:**
1. LLM 识别用户意图，调用 `update_trip` 工具并传入 trip_id 及要更新的字段
2. Tool 验证用户登录状态
3. Tool 调用 NextApiService.updateTrip 提交更新
4. API 返回更新后的出差记录
5. Tool 返回确认信息，LLM 向用户展示更新结果

**Extensions:**
- 2a. 用户未登录：Tool 返回"请先登录"提示
- 3a. API 调用失败（网络错误、记录不存在）：Tool 返回错误信息

---

### Use Case: 删除出差记录

**Primary Actor:** 用户（通过对话）
**Scope:** 二狗 AI 助手 — Tool 调用系统
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 删除不需要的出差记录及其所有费用明细

**Preconditions:**
- 用户已登录 Next 后端
- 目标出差记录已存在

**Success Guarantee (Postconditions):**
- 出差记录及其所有费用明细已删除

**Trigger:** 用户在对话中要求删除某次出差记录

**Main Success Scenario:**
1. LLM 识别用户意图，调用 `delete_trip` 工具并传入 trip_id
2. Tool 验证用户登录状态
3. Tool 调用 NextApiService.deleteTrip 执行删除
4. Tool 返回"出差已删除"确认

**Extensions:**
- 2a. 用户未登录：Tool 返回"请先登录"提示
- 3a. API 调用失败：Tool 返回错误信息

---

### Use Case: 更新费用明细

**Primary Actor:** 用户（通过对话）
**Scope:** 二狗 AI 助手 — Tool 调用系统
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 修改某笔费用的类型、金额、描述、报销状态等

**Preconditions:**
- 用户已登录 Next 后端
- 目标费用明细已存在于某次出差下

**Success Guarantee (Postconditions):**
- 费用明细已按用户指示更新

**Trigger:** 用户在对话中要求修改某笔差旅费用（如"把那笔打车费改成 150 元"）

**Main Success Scenario:**
1. LLM 识别用户意图，调用 `update_trip_item` 工具并传入 trip_id、item_id 及要更新的字段
2. Tool 验证用户登录状态
3. Tool 调用 NextApiService.updateTripItem 提交更新（金额做 toDoubleOrNull 转换）
4. API 返回更新后的费用明细
5. Tool 返回确认信息，展示更新后的描述、金额、币种

**Extensions:**
- 2a. 用户未登录：Tool 返回"请先登录"提示
- 3a. 金额格式无效：toDoubleOrNull 返回 null，保持原金额不变
- 3b. API 调用失败：Tool 返回错误信息

---

### Use Case: 删除费用明细

**Primary Actor:** 用户（通过对话）
**Scope:** 二狗 AI 助手 — Tool 调用系统
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 删除某次出差下误录或不需要的费用明细

**Preconditions:**
- 用户已登录 Next 后端
- 目标费用明细已存在

**Success Guarantee (Postconditions):**
- 该笔费用明细已删除

**Trigger:** 用户在对话中要求删除某笔差旅费用

**Main Success Scenario:**
1. LLM 识别用户意图，调用 `delete_trip_item` 工具并传入 trip_id 和 item_id
2. Tool 验证用户登录状态
3. Tool 调用 NextApiService.deleteTripItem 执行删除
4. Tool 返回"费用已删除"确认

**Extensions:**
- 2a. 用户未登录：Tool 返回"请先登录"提示
- 3a. API 调用失败：Tool 返回错误信息
