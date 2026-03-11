## ADDED Requirements

### Requirement: Expense detail page navigation
The system SHALL provide navigation from the expense list to a detail/edit page when the user taps an expense entry. The detail page SHALL be a new Compose route that receives the expense ID as a navigation argument.

#### Scenario: Navigate to detail page
- **WHEN** user taps an expense entry in the list
- **THEN** system navigates to the expense detail page with the entry's ID

#### Scenario: Back navigation from detail page
- **WHEN** user presses the back button on the detail page
- **THEN** system returns to the expense list page and refreshes data

### Requirement: Load expense detail with items
The system SHALL call `GET /api/expenses/:id` to load the full expense detail, including items array and photo count. The detail page SHALL display all fields: amount, date, notes, tags, currency, and AI-parsed item lines.

#### Scenario: Successful detail load
- **WHEN** the detail page opens with a valid expense ID
- **THEN** system calls the API and displays amount, date, notes, tags, currency, and item lines (name, quantity, unit_price, amount, specs)

#### Scenario: Detail load failure
- **WHEN** the API call fails (network error or 404)
- **THEN** system displays an error message with a retry button

#### Scenario: No item lines
- **WHEN** the expense has no items (item_count = 0)
- **THEN** system hides the items section

### Requirement: Edit expense fields
The system SHALL allow editing amount, date, notes, tags, and currency on the detail page. The system SHALL call `PUT /api/expenses/:id` with only the changed fields. Text input fields SHALL use `TextFieldValue` to support Chinese IME.

#### Scenario: Edit amount and notes
- **WHEN** user modifies the amount and notes fields and taps save
- **THEN** system calls `PUT /api/expenses/:id` with updated amount and notes
- **THEN** system displays success feedback and refreshes the detail

#### Scenario: Edit date
- **WHEN** user taps the date field and selects a new date from the date picker
- **THEN** the date field updates to the selected date (YYYY-MM-DD format)

#### Scenario: Edit tags
- **WHEN** user adds or removes tags (via chips or custom input)
- **THEN** the tags array updates accordingly and is sent on save

#### Scenario: Edit currency
- **WHEN** user selects a different currency (CAD/CNY/USD)
- **THEN** the currency field updates and the amount display reformats

#### Scenario: Save with invalid amount
- **WHEN** user clears the amount field or enters non-numeric text
- **THEN** the save button is disabled

#### Scenario: Save API failure
- **WHEN** the PUT request fails
- **THEN** system displays an error message and preserves the user's edits (no data loss)

### Requirement: Delete expense from detail page
The system SHALL allow deleting an expense from the detail page with a confirmation dialog. After successful deletion, system SHALL navigate back to the list.

#### Scenario: Delete with confirmation
- **WHEN** user taps delete on the detail page and confirms in the dialog
- **THEN** system calls `DELETE /api/expenses/:id`, shows success, and navigates back to the list

#### Scenario: Cancel delete
- **WHEN** user taps delete but dismisses the confirmation dialog
- **THEN** no deletion occurs, user stays on the detail page
