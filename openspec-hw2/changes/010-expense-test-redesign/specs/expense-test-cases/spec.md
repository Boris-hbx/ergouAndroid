## ADDED Requirements

### Requirement: E2E 手动新建账单
系统 SHALL 支持用户从记账列表页手动新建一条账单，填写金额/备注/标签/币种/日期后保存，并在列表和详情页中验证数据一致性。

#### Scenario: 新建最小必填账单
- **WHEN** 用户点击 FAB 打开新建对话框，填写金额 25.50，点击保存
- **THEN** 系统调用 POST /api/expenses 创建条目
- **THEN** 列表刷新后出现该条目，金额显示 $25.50

#### Scenario: 新建完整字段账单
- **WHEN** 用户填写金额 88.00、备注"超市采购"、标签"超市"、币种 CAD、日期 2026-03-01，点击保存
- **THEN** 条目创建成功
- **THEN** 点击该条目进入详情页，所有字段与输入一致

#### Scenario: 新建 CNY 币种账单
- **WHEN** 用户填写金额 150.00、币种选择 CNY，保存
- **THEN** 列表中该条目显示 ¥150.00

#### Scenario: 新建多标签账单
- **WHEN** 用户选择标签"超市"和"日用"，保存
- **THEN** 详情页显示两个标签

#### Scenario: 金额为空时无法保存
- **WHEN** 用户不填金额直接点保存
- **THEN** 系统提示金额必填，不发送请求

#### Scenario: 网络失败时保留输入
- **WHEN** 网络不可用时用户点击保存
- **THEN** 系统显示错误提示，对话框保持打开，已填内容不丢失

### Requirement: E2E 单张照片拍照记账
系统 SHALL 支持用户选择/拍摄一张收据照片，通过 Claude AI 自动解析收据信息，预览编辑后保存为新账单。测试数据使用 group2 中的单张照片。

#### Scenario: 相册选择 Food Basics 收据并保存（group2/429565025.jpg）
- **WHEN** 用户进入拍照记账模式，从相册选择 Food Basics 收据照片
- **THEN** 系统压缩图片后发送到 Claude API
- **THEN** 解析预览显示：商家 Food Basics、金额 $19.56、明细含 Honeycrisp Apple/Premium Banana/Mango
- **THEN** 用户确认保存后，列表出现新条目，详情页含 1 张照片和完整明细

#### Scenario: 相册选择 Costco Gas 收据（group2/2110669981.jpg）
- **WHEN** 用户选择 Costco 加油收据
- **THEN** 解析预览显示：商家 Costco、金额 $84.63、标签建议"加油"
- **THEN** 保存后详情页显示正确信息

#### Scenario: 相册选择兰州拉面收据（group2/724722102.jpg）
- **WHEN** 用户选择兰州拉面收据
- **THEN** 解析预览显示：商家 Gol's Lanzhou Noodle、金额 $75.77（含 tip $6.89）、标签建议"餐饮"
- **THEN** 保存后详情页正确

#### Scenario: 相册选择 Structube 收据（group2/2024458997.jpg）
- **WHEN** 用户选择 Structube 家具店收据
- **THEN** 解析预览显示：商家 Structube、金额 $607.94、标签建议"购物"
- **THEN** 保存后详情页正确

#### Scenario: 相册选择 IKEA 收据（group2/671489765.jpg）
- **WHEN** 用户选择 IKEA 收据
- **THEN** 解析预览显示：商家 IKEA、金额 $44.89、明细含 KALAS/MARIUS stool 等
- **THEN** 保存后详情页正确

#### Scenario: 用户修改 AI 解析结果后保存
- **WHEN** AI 解析出金额 $19.56，用户修改为 $20.00，修改标签为"餐饮"
- **THEN** 保存后详情页显示修改后的值 $20.00 和标签"餐饮"

#### Scenario: AI 解析超时降级为手动输入
- **WHEN** Claude API 超过 120 秒未返回
- **THEN** 系统提示"分析超时，请手动输入"
- **THEN** 用户可手动填写金额等信息并保存

#### Scenario: AI 返回格式异常
- **WHEN** Claude API 返回非预期 JSON 格式
- **THEN** 系统提示"AI 返回格式异常，请重试"

#### Scenario: 照片超过 10MB
- **WHEN** 用户选择一张超过 10MB 的照片
- **THEN** 系统提示"照片不能超过 10MB"，不发送分析请求

### Requirement: E2E 多张照片合并分析（同一账单）
系统 SHALL 支持用户选择多张同一收据的照片，选择"同一单据"模式后合并拼接分析为一条记录。测试数据使用 group1（T&T 超市 4 张照片）。

#### Scenario: group1 四张照片合并分析为一条 T&T 记录
- **WHEN** 用户在批量扫描模式选择 group1 的 4 张照片，选择"同一单据"，点击"二狗分析"
- **THEN** 系统将 4 张照片垂直拼接为一张长图（≤1024px 宽，≤8000px 高）
- **THEN** Claude 返回 1 条解析结果：T&T Supermarket、$235.76
- **THEN** 明细包含肉类（Pork Loin）、海鲜（Salmon/Abalone）、蔬菜（Carrot/Pumpkin/Tomato）、点心等
- **THEN** 用户确认保存后，列表出现 1 条记录
- **THEN** 详情页显示 4 张照片、完整明细、总金额 $235.76

#### Scenario: 合并分析后用户编辑总金额
- **WHEN** AI 解析出 $235.76，用户修改为 $236.00
- **THEN** 保存后详情页显示 $236.00

#### Scenario: 拼接后超过 8000px 高度自动缩放
- **WHEN** 多张高分辨率照片拼接后超过 8000px
- **THEN** 系统自动缩放整张拼接图至 8000px 以内
- **THEN** 分析仍正常进行

### Requirement: E2E 多张照片独立分析（不同账单）
系统 SHALL 支持用户选择多张不同收据的照片，选择"不同单据"模式后逐张独立分析，生成多条记录。测试数据使用 group2（5 张不同收据）。

#### Scenario: group2 五张照片独立分析为五条记录
- **WHEN** 用户在批量扫描模式选择 group2 的 5 张照片，选择"不同单据"，点击"二狗分析"
- **THEN** 系统逐张分析，显示进度（1/5...5/5）
- **THEN** 展示 5 条解析结果卡片
- **THEN** 结果包含：Structube $607.94、Costco $84.63、Food Basics $19.56、IKEA $44.89、兰州拉面 $75.77
- **THEN** 用户确认保存全部后，列表出现 5 条新记录
- **THEN** 逐一进入详情验证金额、商家、照片均正确

#### Scenario: 批量分析中某张失败
- **WHEN** 5 张照片中第 3 张分析超时
- **THEN** 系统标记第 3 张为失败，继续分析第 4、5 张
- **THEN** 结果列表显示 4 条成功结果
- **THEN** 用户可保存 4 条，失败的可手动处理

#### Scenario: 用户删除部分分析结果后保存
- **WHEN** 5 条结果中用户删除 2 条
- **THEN** 点击保存全部后只创建 3 条记录

#### Scenario: 批量保存部分失败
- **WHEN** 5 条结果保存时第 2 条 API 失败
- **THEN** 系统提示"4 条成功，1 条失败"
- **THEN** 失败条目保留在扫描结果中可重试

### Requirement: E2E 混合照片批量分析
系统 SHALL 支持用户选择包含同一收据和不同收据的混合照片集。测试数据使用 group3（9 张混合照片）。

#### Scenario: group3 九张混合照片选"不同单据"模式
- **WHEN** 用户选择 group3 全部 9 张照片，选择"不同单据"
- **THEN** 系统逐张分析生成 9 条结果
- **THEN** 其中 4 条是 T&T 收据的部分片段（金额不完整/重复）
- **THEN** 用户需手动删除不完整的 T&T 条目，保留 5 条独立收据的结果

#### Scenario: group3 九张混合照片选"同一单据"模式
- **WHEN** 用户选择 group3 全部 9 张照片，选择"同一单据"
- **THEN** 系统拼接 9 张图后发送分析
- **THEN** AI 解析结果可能混乱（不同商家混合在一张图中）
- **THEN** 用户需取消，改为分批处理

#### Scenario: 分批处理混合照片（推荐流程）
- **WHEN** 用户先选 group1 的 4 张照片用"同一单据"分析，保存 T&T 记录
- **WHEN** 再选 group2 的 5 张照片用"不同单据"分析，保存 5 条记录
- **THEN** 最终得到 6 条正确记录（1 条 T&T + 5 条独立）

### Requirement: E2E 编辑已有账单基本信息
系统 SHALL 支持用户进入已有账单详情页，修改金额/备注/日期/标签/币种后保存，验证变更持久化。

#### Scenario: 修改金额和备注
- **WHEN** 用户进入一条 $19.56 的账单详情，将金额改为 $20.00、备注改为"含小费"
- **THEN** 保存后列表页和详情页均显示 $20.00 和"含小费"

#### Scenario: 修改日期
- **WHEN** 用户将日期从 2026-02-12 改为 2026-02-15
- **THEN** 保存后列表页按新日期分组显示

#### Scenario: 修改标签
- **WHEN** 用户将标签从"超市"改为"餐饮"+"日用"
- **THEN** 保存后详情页显示两个新标签

#### Scenario: 切换币种 CAD→CNY
- **WHEN** 用户将币种从 CAD 切换为 CNY
- **THEN** 保存后列表和详情页显示 ¥ 符号

#### Scenario: 未修改直接保存
- **WHEN** 用户进入详情页不做任何修改就保存
- **THEN** 系统不发送无意义的 PUT 请求或正常处理空更新

#### Scenario: 编辑时网络断开
- **WHEN** 用户修改了字段后保存时网络不可用
- **THEN** 系统提示保存失败，修改内容保留在页面中不丢失

### Requirement: E2E 管理已有账单照片
系统 SHALL 支持用户在详情页对已有账单的照片进行新增、删除、全屏查看操作。

#### Scenario: 为无照片账单添加一张照片
- **WHEN** 用户进入一条无照片的账单详情，点击添加照片，选择一张图
- **THEN** 照片上传成功，照片区域出现缩略图

#### Scenario: 为已有照片账单追加照片
- **WHEN** 用户进入一条已有 1 张照片的账单，追加第 2 张照片
- **THEN** 照片列表显示 2 张缩略图

#### Scenario: 删除已有照片
- **WHEN** 用户点击某张照片的删除按钮并确认
- **THEN** 系统调用 DELETE /api/expenses/photos/:photoId
- **THEN** 照片从列表中消失

#### Scenario: 替换照片（删旧加新）
- **WHEN** 用户删除一张旧照片，再添加一张新照片
- **THEN** 详情页只显示新照片

#### Scenario: 全屏查看照片
- **WHEN** 用户点击照片缩略图
- **THEN** 系统以全屏模式显示照片（带 session cookie 认证加载）

#### Scenario: 上传超过 10MB 照片
- **WHEN** 用户尝试上传超过 10MB 的照片
- **THEN** 系统提示"照片不能超过 10MB"，不上传

### Requirement: E2E 添加照片后重新 AI 分析
系统 SHALL 支持用户为已有账单添加收据照片后触发 AI 重新分析，更新账单信息。

#### Scenario: 手动账单补充照片后 AI 分析
- **WHEN** 用户有一条手动创建的"午饭 $25"账单，添加一张收据照片并触发 AI 分析
- **THEN** AI 返回实际金额和明细
- **THEN** 用户确认后账单字段更新

#### Scenario: AI 分析超时时原数据不变
- **WHEN** 用户触发 AI 分析但超时
- **THEN** 账单原有数据不受影响

### Requirement: E2E 滑动删除账单
系统 SHALL 支持用户在列表页左滑删除账单，提供 5 秒撤销窗口，超时后永久删除。

#### Scenario: 滑动删除并等待自动确认
- **WHEN** 用户左滑一条账单，不点撤销等待 5 秒
- **THEN** 条目立即从列表消失（乐观删除）
- **THEN** Snackbar 显示"已删除" + 撤销按钮
- **THEN** 5 秒后系统调用 DELETE API 永久删除
- **THEN** 刷新列表确认条目不存在

#### Scenario: 滑动删除后立即撤销
- **WHEN** 用户左滑一条账单，在 5 秒内点击"撤销"
- **THEN** 条目恢复到列表原位
- **THEN** 系统不调用 DELETE API
- **THEN** 进入详情页确认数据完整

#### Scenario: 删除 API 失败时恢复
- **WHEN** 5 秒后系统调用 DELETE API 但网络失败
- **THEN** 条目恢复到列表中，显示错误提示

### Requirement: E2E 详情页删除账单
系统 SHALL 支持用户在详情页删除账单，包括确认对话框、成功后返回列表。

#### Scenario: 详情页确认删除
- **WHEN** 用户在详情页点击删除并确认
- **THEN** 系统调用 DELETE API
- **THEN** 导航回列表页，该条目消失

#### Scenario: 详情页取消删除
- **WHEN** 用户点击删除后取消确认
- **THEN** 不发生任何操作，停留在详情页

### Requirement: E2E 聊天工具记账
系统 SHALL 支持通过聊天中的 tool use 完成记账 CRUD 和查询统计操作。

#### Scenario: 通过 add_expense 工具记账
- **WHEN** 用户在聊天中说"记一笔午饭 25.5 加币"
- **THEN** 二狗调用 add_expense(amount=25.5, notes="午饭", currency="CAD")
- **THEN** 工具返回成功，切换到记账页面可见新条目

#### Scenario: 通过 query_expenses 查询
- **WHEN** 用户说"这个月花了多少"
- **THEN** 二狗调用 expense_summary(period="month")
- **THEN** 返回总金额、条目数、标签分布

#### Scenario: 通过 delete_expense 删除
- **WHEN** 用户说"删掉那笔午饭"
- **THEN** 二狗调用 delete_expense(expense_id=xxx)
- **THEN** 条目在列表页消失
