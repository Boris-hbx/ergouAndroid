## ADDED Requirements

### Requirement: Delete action shows confirmation dialog
When the user taps the delete button on the expense edit form, the system SHALL show a confirmation dialog before performing the deletion.

#### Scenario: Delete button triggers confirmation
- **WHEN** user taps the delete icon button on the edit form
- **THEN** a confirmation dialog appears with "确定删除此费用项？" message and "取消"/"确定" buttons

### Requirement: Cancel delete keeps edit form open
When the user taps "取消" in the delete confirmation dialog, the system SHALL dismiss the dialog and keep the edit form open with all field values unchanged.

#### Scenario: Cancel returns to edit form
- **WHEN** user taps "取消" in the delete confirmation dialog
- **THEN** the dialog closes, the edit form remains open, and all input field values are unchanged

#### Scenario: Form state preserved after cancel
- **WHEN** user has modified fields, taps delete, then taps "取消"
- **THEN** all modified field values (description, amount, date, notes, photos) remain as the user left them

### Requirement: Confirm delete closes form and removes item
When the user taps "确定" in the delete confirmation dialog, the system SHALL delete the expense item and close the edit form.

#### Scenario: Confirm triggers deletion and closes form
- **WHEN** user taps "确定" in the delete confirmation dialog
- **THEN** the expense item is deleted via API, the edit form closes, and the user returns to the trip detail page
