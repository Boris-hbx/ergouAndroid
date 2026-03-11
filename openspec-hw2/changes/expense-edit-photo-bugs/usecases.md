## Use Cases

### Use Case: Analyze existing expense photos via AI

**Primary Actor:** User
**Scope:** Ergou expense module
**Level:** User goal

**Preconditions:**
- User has opened edit dialog for an existing expense with uploaded photos

**Success Guarantee (Postconditions):**
- AI analyzes the server-hosted photos and auto-fills amount, title, date, currency, tags

**Trigger:** User taps "让二狗分析" button in edit dialog

**Main Success Scenario:**
1. User opens edit dialog for an existing expense that has server-uploaded photos.
2. User taps "让二狗分析".
3. System downloads the existing photos from server, converts to base64.
4. System sends images to ReceiptAnalyzer for AI analysis.
5. System receives structured preview and auto-fills form fields (amount, title, date, currency, tags).

**Extensions:**
- 3a. Photo download fails (network error): System shows error message, analysis proceeds with any available photos or text.
- 4a. Analysis timeout: System shows "分析超时，请手动输入".

### Use Case: Notify user of photo upload failure

**Primary Actor:** User
**Scope:** Ergou expense module
**Level:** Subfunction

**Preconditions:**
- User creates a new expense with photos attached

**Success Guarantee (Postconditions):**
- User is informed that photo upload failed

**Trigger:** Photo upload fails during expense creation

**Main Success Scenario:**
1. User saves new expense with photos.
2. System creates expense successfully.
3. System attempts to upload photos but one or more fail.
4. System shows a Snackbar/error message: "记账已保存，但N张照片上传失败".
