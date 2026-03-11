## ADDED Requirements

### Requirement: Notes field defaults to collapsed state
The notes section SHALL render in a collapsed state by default, showing only a clickable header row (e.g., "备注 ▶") instead of the full text input.

#### Scenario: Initial form open — notes collapsed
- **WHEN** the expense edit form opens (create or edit mode)
- **THEN** the notes section displays as a single collapsed row, not the multi-line input

#### Scenario: Collapsed state with existing notes content
- **WHEN** the form opens for an item that already has notes content
- **THEN** the notes section is still collapsed, but the header row shows a brief indicator (e.g., truncated text or icon) that notes exist

### Requirement: Notes field toggles on tap
The notes collapsed header SHALL expand to show the full text input when tapped, and collapse again when tapped a second time.

#### Scenario: Expand notes
- **WHEN** user taps the collapsed notes header
- **THEN** the multi-line notes TextField expands below the header

#### Scenario: Collapse notes
- **WHEN** user taps the notes header while expanded
- **THEN** the TextField collapses, and the entered text is preserved

### Requirement: Notes content preserved across toggle
The notes text content SHALL be preserved when toggling between collapsed and expanded states.

#### Scenario: Type, collapse, re-expand
- **WHEN** user types "报销备注" in notes, collapses, then re-expands
- **THEN** the TextField still contains "报销备注"

### Requirement: Collapsed form fits on one screen
When notes are collapsed, all form fields (type, description, amount/currency, date, photos, notes header, action buttons) SHALL be visible without scrolling on a standard phone screen (≥360dp width, ≥640dp height).

#### Scenario: All fields visible without scroll
- **WHEN** the form is displayed with notes collapsed and ≤3 photo thumbnails
- **THEN** all fields from type selector to save button are visible without scrolling
