## ADDED Requirements

### Requirement: Swipe-to-delete with confirmation
ReviewItem SHALL be wrapped in SwipeToDismissBox (endToStart direction) with red background and delete icon. On swipe, system SHALL show AlertDialog for confirmation. On confirm, system SHALL optimistically remove the item and show Snackbar with undo. On undo, item SHALL reappear. On timeout, system SHALL call deleteReview API.

#### Scenario: Swipe and confirm delete
- **WHEN** user swipes left and confirms in dialog
- **THEN** item is removed from list, Snackbar shows "已删除" with "撤销" action

#### Scenario: Swipe and cancel
- **WHEN** user swipes left and cancels in dialog
- **THEN** SwipeToDismissBox resets, item stays

#### Scenario: Undo delete
- **WHEN** user taps "撤销" on Snackbar
- **THEN** item reappears in list, no API call made

#### Scenario: Delete timeout
- **WHEN** Snackbar times out without undo
- **THEN** system calls viewModel.deleteReview(id)

### Requirement: Click-to-expand detail
ReviewItem Card SHALL be clickable to toggle detail expansion. System SHALL use AnimatedVisibility with expandVertically/shrinkVertically animations. Expanded state SHALL be tracked via expandedIds: Set<String>.

#### Scenario: Expand item
- **WHEN** user clicks a collapsed review item
- **THEN** detail section expands showing frequency, last completed, due status, notes, category

#### Scenario: Collapse item
- **WHEN** user clicks an expanded review item
- **THEN** detail section collapses

### Requirement: Detail section content
Expanded detail SHALL display: frequency (translated to Chinese), lastCompleted (or "从未完成"), dueStatus with color-coded label, notes (if non-empty), category (if non-empty). Colors: overdue=red, due_today=orange, due_soon=yellow, upcoming=green, completed/paused=gray.

#### Scenario: Display all fields
- **WHEN** review has all fields populated
- **THEN** all fields display with correct translations and colors

#### Scenario: Missing optional fields
- **WHEN** review has no notes and no category
- **THEN** those fields are hidden

### Requirement: Remove delete IconButton
The existing delete IconButton in ReviewItem SHALL be removed, replaced by swipe-to-delete.

#### Scenario: No delete button visible
- **WHEN** ReviewItem renders
- **THEN** no delete IconButton is shown in the row
