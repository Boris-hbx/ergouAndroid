## Use Cases

### Use Case: Edit trip expense item in compact form

**Primary Actor:** User
**Scope:** Ergou 差旅费用编辑表单
**Level:** User goal

**Stakeholders and Interests:**
- User — 在一屏内看到所有费用字段，快速填写并保存

**Preconditions:**
- 用户已打开差旅详情页，点击了某个费用项进入编辑，或点击新增按钮

**Success Guarantee (Postconditions):**
- 所有输入字段（类型、描述、金额/币种、日期、票据、备注）在一屏内可见（备注折叠时）
- 字体和输入框尺寸统一协调

**Trigger:** 用户打开费用编辑表单

**Main Success Scenario:**
1. 用户打开费用编辑表单，所有字段在一屏内展示（备注默认折叠）
2. 用户填写描述，输入框文字以紧凑字体显示
3. 用户输入金额并选择币种，金额/币种行以扁平紧凑的输入框呈现
4. 用户选择日期，日期输入框与金额行高度一致
5. 用户保存费用项

**Extensions:**
- 1a. 编辑已有费用项：表单预填已有数据，所有字段仍在一屏内可见

---

### Use Case: Preview and manage receipt photos

**Primary Actor:** User
**Scope:** Ergou 费用编辑表单 — 票据照片区域
**Level:** User goal

**Stakeholders and Interests:**
- User — 查看已拍照片的大图确认内容，删除不需要的照片

**Preconditions:**
- 费用编辑表单已打开，至少有一张票据照片（已上传或待上传）

**Success Guarantee (Postconditions):**
- 用户能看到照片大图预览
- 删除按钮完整可见且可点击
- 删除后照片从列表移除

**Trigger:** 用户点击票据缩略图

**Main Success Scenario:**
1. 用户点击某张票据缩略图
2. 系统展示照片大图预览（全屏或弹窗）
3. 用户确认照片内容后关闭预览，回到编辑表单
4. 用户点击某张照片右上角的删除按钮（×），照片从列表移除

**Extensions:**
- 1a. 照片加载失败：显示占位图和错误提示
- 4a. 删除按钮视觉上完整显示，不被其他元素遮盖

---

### Use Case: Toggle notes field visibility

**Primary Actor:** User
**Scope:** Ergou 费用编辑表单 — 备注区域
**Level:** Subfunction

**Stakeholders and Interests:**
- User — 节省屏幕空间，仅在需要时展开备注输入框

**Preconditions:**
- 费用编辑表单已打开

**Success Guarantee (Postconditions):**
- 备注区域可在折叠/展开之间切换
- 折叠时已有备注内容不丢失
- 折叠状态下，所有其他字段在一屏内可见

**Trigger:** 用户点击备注区域的展开/折叠控件

**Main Success Scenario:**
1. 用户打开编辑表单，备注区域默认折叠，显示为一行可点击的标题
2. 用户点击备注标题，备注输入框展开
3. 用户输入备注内容
4. 用户再次点击备注标题，输入框折叠，内容保留

**Extensions:**
- 1a. 编辑已有费用项且备注已有内容：仍默认折叠，标题旁显示已有备注的摘要提示
- 4a. 折叠后用户滚动页面：由于空间节省，其他字段无需滚动即可全部可见

---

### Use Case: Cancel deletion and stay on edit page

**Primary Actor:** User
**Scope:** Ergou 费用编辑表单 — 删除操作
**Level:** Subfunction

**Stakeholders and Interests:**
- User — 误触删除时可以取消，不丢失正在编辑的内容

**Preconditions:**
- 用户正在编辑一个已有费用项（编辑模式），删除按钮可见

**Success Guarantee (Postconditions):**
- 取消删除后，编辑表单保持打开，所有字段内容不变

**Trigger:** 用户点击删除按钮

**Main Success Scenario:**
1. 用户点击删除按钮
2. 系统弹出确认对话框："确定删除此费用项？"
3. 用户点击"取消"
4. 确认对话框关闭，编辑表单保持打开，所有输入内容不变

**Extensions:**
- 3a. 用户点击"确定"：费用项被删除，编辑表单关闭，回到差旅详情页

**Open Questions:**
- 当前删除是否已有确认对话框？如果没有，需要新增
