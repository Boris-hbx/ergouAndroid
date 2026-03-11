## Use Cases

### Use Case: 查看例行项列表

**Primary Actor:** 用户
**Scope:** 二狗 App 例行模块
**Level:** User goal

**Preconditions:**
- 用户已登录 Next 账号

**Success Guarantee (Postconditions):**
- 用户看到按频率分组的例行项列表，包含到期状态

**Trigger:** 用户从快捷栏或导航进入例行页面

**Main Success Scenario:**
1. 用户进入例行页面，默认显示"日"频率 tab
2. 系统从 Review API 获取所有例行项，按频率分组展示
3. 每项显示名称、到期状态标签（逾期/今天到期/即将到期/未到期）、分类
4. 用户切换 tab（日/周/月/年）查看不同频率的例行项
5. 系统筛选并显示对应频率的例行项，按到期紧急度排序

**Extensions:**
- 2a. 网络请求失败: 系统显示错误 Snackbar，用户可下拉重试
- 2b. 用户未登录: 系统显示登录引导，提供"前往设置"按钮
- 5a. 该频率下无例行项: 系统显示空状态提示

---

### Use Case: 添加例行项

**Primary Actor:** 用户
**Scope:** 二狗 App 例行模块
**Level:** User goal

**Preconditions:**
- 用户已登录 Next 账号

**Success Guarantee (Postconditions):**
- 新例行项已创建并出现在列表中

**Trigger:** 用户点击 FAB 添加按钮，或通过对话告诉二狗

**Main Success Scenario:**
1. 用户点击添加按钮
2. 系统弹出添加对话框，包含：名称输入框、频率选择（日/周/月/年）、分类输入框（可选）
3. 频率默认跟随当前选中的 tab
4. 用户填写名称，选择频率，可选填分类
5. 用户确认添加
6. 系统调用 Review API 创建例行项，刷新列表

**Extensions:**
- 5a. 名称为空: 确认按钮禁用，无法提交
- 6a. 创建失败: 系统显示错误 Snackbar

---

### Use Case: 标记例行项完成

**Primary Actor:** 用户
**Scope:** 二狗 App 例行模块
**Level:** User goal

**Preconditions:**
- 例行项存在且未暂停

**Success Guarantee (Postconditions):**
- 例行项的 last_completed 更新为当前时间，到期状态重新计算

**Trigger:** 用户在详情页点击"标记完成"

**Main Success Scenario:**
1. 用户点击列表中的例行项，进入详情页
2. 用户点击"标记完成"按钮
3. 系统调用 Review complete API
4. 系统刷新列表，该项到期状态更新（变为 completed 或重新计算下次到期）

**Extensions:**
- 3a. API 调用失败: 系统显示错误 Snackbar
- 1a. 该项已暂停: 详情页不显示"标记完成"按钮，仅显示"恢复"

---

### Use Case: 暂停/恢复例行项

**Primary Actor:** 用户
**Scope:** 二狗 App 例行模块
**Level:** User goal

**Preconditions:**
- 例行项存在

**Success Guarantee (Postconditions):**
- 例行项暂停状态切换，暂停后不再计算到期

**Trigger:** 用户在详情页点击"暂停"或"恢复"

**Main Success Scenario:**
1. 用户进入例行项详情页
2. 用户点击"暂停"按钮
3. 系统调用 Review update API 设置 paused=true
4. 该项在列表中显示"已暂停"标记，不再显示到期状态

**Extensions:**
- 2a. 当前已暂停: 按钮显示"恢复"，点击后设置 paused=false，重新计算到期状态

---

### Use Case: 删除例行项

**Primary Actor:** 用户
**Scope:** 二狗 App 例行模块
**Level:** User goal

**Preconditions:**
- 例行项存在

**Success Guarantee (Postconditions):**
- 例行项从后端删除，列表中消失

**Trigger:** 用户在详情页点击删除

**Main Success Scenario:**
1. 用户进入详情页，点击删除按钮
2. 系统弹出确认对话框
3. 用户确认删除
4. 系统调用 Review delete API，返回列表页，刷新数据

**Extensions:**
- 3a. 用户取消: 关闭对话框，不执行任何操作
- 4a. 删除失败: 系统显示错误 Snackbar

---

### Use Case: 通过对话管理例行项

**Primary Actor:** 用户
**Scope:** 二狗 App 对话模块 + 例行模块
**Level:** User goal

**Preconditions:**
- 用户已登录 Next 账号

**Success Guarantee (Postconditions):**
- 二狗通过工具调用完成例行项的增/查操作

**Trigger:** 用户在对话中提到例行相关意图（如"帮我加个每周例行"、"我的例行有哪些"）

**Main Success Scenario:**
1. 用户在对话中描述例行意图
2. 二狗识别意图，调用对应 Review 工具（create_review / query_reviews）
3. 工具执行成功，二狗回复操作结果
4. 用户进入例行页面可以看到变化

**Extensions:**
- 2a. 用户意图不明确（如没说频率）: 二狗追问补全信息
- 3a. 工具执行失败: 二狗告知用户失败原因

---

### Use Case: 快捷栏查看例行状态

**Primary Actor:** 用户
**Scope:** 二狗 App 快捷栏
**Level:** Subfunction

**Preconditions:**
- 用户已登录 Next 账号

**Success Guarantee (Postconditions):**
- 快捷栏显示当前到期例行项数量

**Trigger:** 快捷栏数据刷新（页面加载/返回时）

**Main Success Scenario:**
1. 系统获取所有 Review 数据
2. 系统统计 due_status 为 overdue / due_today / due_soon 的项数
3. 快捷栏显示 "例行: N 项到期"（N > 0 时）或 "例行" （N = 0 时）

**Extensions:**
- 1a. 获取失败: 快捷栏显示"例行"不带数量

**Open Questions:**
- 现有 Routine 数据迁移: 用户在 Routine API 中已有的每日习惯，是否需要引导用户在 Review 中重建？还是通过后端脚本批量迁移？
