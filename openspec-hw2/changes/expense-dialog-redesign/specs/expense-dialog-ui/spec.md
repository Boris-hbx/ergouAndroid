## Requirements

### Requirement: Dialog-based expense editing
The system SHALL present expense creation and editing in a modal Dialog (or ModalBottomSheet), replacing the current full-page navigation. The Dialog SHALL be used for all expense operations: new blank, AI-analyzed result, and editing existing records.

#### Scenario: Open new expense dialog
- **WHEN** user taps the "+" add button on the expense list
- **THEN** system shows a modal Dialog with empty fields and title "记一笔"

#### Scenario: Open edit expense dialog
- **WHEN** user taps an expense entry in the list
- **THEN** system shows a modal Dialog pre-filled with the expense data and title "编辑账单"

#### Scenario: Close dialog
- **WHEN** user taps the × close button or taps outside the dialog
- **THEN** the dialog dismisses without saving

### Requirement: Field layout order
The Dialog SHALL display fields in this order from top to bottom:
1. 标题 (title input)
2. 金额 + 币种 (amount input + currency chips on the same row)
3. 日期 / 时间 (date picker + time picker side by side)
4. 明细 (items card, if items exist)
5. 描述 (collapsible description textarea)
6. 票据照片 (photo thumbnails + add button)

#### Scenario: Amount and currency on same line
- **WHEN** the dialog is displayed
- **THEN** the amount input field and currency chip selector (CAD/CNY) are on the same horizontal row

#### Scenario: Date and time side by side
- **WHEN** the dialog is displayed
- **THEN** the date picker and time picker are displayed side by side
- **AND** time is precise to hour:minute (HH:mm)

#### Scenario: Time placeholder for new expense
- **WHEN** creating a new expense with no time set
- **THEN** the time field shows placeholder text "票据时间"

### Requirement: Collapsible description field
The system SHALL provide a "描述" textarea that collapses to 2 lines by default with no scrollbar. When content exceeds 2 lines, an "展开" toggle appears.

#### Scenario: Description within 2 lines
- **WHEN** the description content fits within 2 lines
- **THEN** the textarea displays fully, no toggle shown

#### Scenario: Description exceeds 2 lines
- **WHEN** the description content exceeds 2 lines
- **THEN** the textarea is clipped to 2 lines with overflow hidden
- **AND** an "展开" toggle link appears below

#### Scenario: Expand description
- **WHEN** user taps "展开"
- **THEN** the textarea expands to show full content
- **AND** the toggle changes to "收起"

#### Scenario: Collapse description
- **WHEN** user taps "收起"
- **THEN** the textarea collapses back to 2 lines

### Requirement: AI analysis button always visible
The system SHALL display a "让二狗分析" button in ALL dialog states (new blank, AI-analyzed, edit existing). The button SHALL be on its own row, full-width, positioned above the action buttons (取消/保存/删除).

#### Scenario: AI button in new blank dialog
- **WHEN** the new expense dialog is shown
- **THEN** "让二狗分析" button is visible as a full-width button above "取消" and "保存"

#### Scenario: AI button in edit dialog
- **WHEN** the edit expense dialog is shown
- **THEN** "让二狗分析" button is visible as a full-width button above "删除", "取消", and "保存"

### Requirement: Bilingual item display
The system SHALL display expense items with bilingual text when translations are available. Each item shows the original text with a language tag and the translated text with a translation tag.

#### Scenario: English receipt item
- **WHEN** an item originates from an English receipt
- **THEN** the original English name is shown with tag "原始"
- **AND** the Chinese translation is shown below with tag "中文翻译"

#### Scenario: Chinese receipt item
- **WHEN** an item originates from a Chinese receipt
- **THEN** the original Chinese name is shown with tag "原始"
- **AND** the English translation is shown below with tag "英文翻译"

#### Scenario: Item with no translation
- **WHEN** an item has no translation available
- **THEN** only the original name is shown without any tag

### Requirement: Action buttons layout
The bottom of the dialog SHALL have two rows:
1. "让二狗分析" — full-width button (teal/green color)
2. Action row: "取消" + "保存" for new; "删除" + "取消" + "保存" for edit

#### Scenario: New expense buttons
- **WHEN** creating a new expense
- **THEN** bottom shows: [让二狗分析] full row, then [取消] [保存] row

#### Scenario: Edit expense buttons
- **WHEN** editing an existing expense
- **THEN** bottom shows: [让二狗分析] full row, then [删除] [取消] [保存] row
