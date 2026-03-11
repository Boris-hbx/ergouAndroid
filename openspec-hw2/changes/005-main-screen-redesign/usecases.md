## Use Cases

### Use Case: 通过快捷条跳转功能页面

**Primary Actor:** 用户
**Scope:** 二狗主界面
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 快速访问常用功能（任务、记账、习惯等），不想每次都打开菜单翻找

**Preconditions:**
- 用户处于主界面（ChatScreen）

**Success Guarantee (Postconditions):**
- 用户进入目标功能页面，可以查看和操作对应功能数据

**Trigger:** 用户看到输入框上方的快捷条，点击某个条目

**Main Success Scenario:**
1. 用户打开二狗 App，进入主界面
2. 系统在输入框上方显示快捷条，展示 4-5 个最近/常用的功能条目（如 `[Todo]例行` `[记账]差旅`）
3. 用户横向滑动快捷条浏览条目
4. 用户点击目标条目
5. 系统导航到对应功能的详情页面
6. 用户在功能页面完成操作后，点击返回按钮回到主界面

**Extensions:**
- 2a. 用户从未使用过任何功能：系统显示默认推荐条目（任务、习惯、记账、差旅）
- 4a. 快捷条条目超出屏幕宽度：用户可横向滚动查看更多条目

**Open Questions:**
- 快捷条排序策略：纯按最近使用时间，还是综合使用频率？

---

### Use Case: 查看和切换历史会话

**Primary Actor:** 用户
**Scope:** 二狗主界面
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 回到之前的对话继续聊，或查看历史对话内容

**Preconditions:**
- 用户处于主界面，至少存在一个历史会话

**Success Guarantee (Postconditions):**
- 用户切换到目标会话，对话消息流显示该会话的历史消息

**Trigger:** 用户点击顶栏的 [历史] 按钮

**Main Success Scenario:**
1. 用户点击顶栏的 [历史] 按钮
2. 系统显示会话历史页面，按时间倒序列出所有会话（标题 + 最后消息摘要 + 时间）
3. 用户浏览会话列表，找到目标会话
4. 用户点击目标会话
5. 系统切换到该会话，返回主界面并显示该会话的消息流

**Extensions:**
- 2a. 没有任何历史会话：系统显示空态提示"还没有历史对话"
- 3a. 用户想删除某个会话：用户长按或左滑会话条目，系统弹出删除确认，确认后删除
- 3b. 用户想新建会话：用户点击"新建对话"按钮，系统创建新会话并返回主界面

**Open Questions:**
- 会话历史是全屏页面还是底部弹出面板？

---

### Use Case: 在新会话中获取引导

**Primary Actor:** 用户
**Scope:** 二狗主界面
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 打开新对话时不知道说什么，希望有话题引导

**Preconditions:**
- 用户处于新建会话或空会话中，对话消息流为空

**Success Guarantee (Postconditions):**
- 用户通过点击建议话题开始一轮新对话

**Trigger:** 用户进入一个空会话

**Main Success Scenario:**
1. 用户新建会话或打开一个空会话
2. 系统在对话区域中央显示二狗问候语和 2-3 个建议话题卡片
3. 用户浏览建议话题
4. 用户点击某个建议话题卡片
5. 系统将该话题作为用户消息发送，二狗开始回复

**Extensions:**
- 4a. 用户不想用建议话题：用户直接在输入框输入自己的消息，问候语和建议卡片消失
- 2a. 建议话题可以基于用户的使用习惯动态生成（如"查看今日待办"、"记一笔账"）

**Open Questions:**
- 建议话题是固定的还是动态生成的？第一版先用固定话题即可
