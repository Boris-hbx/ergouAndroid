## 1. Swipe-to-delete with confirmation

- [x] 1.1 Remove delete IconButton from ReviewItem
- [x] 1.2 Add pendingDeleteIds state and confirmDeleteItem state to ReviewScreen
- [x] 1.3 Wrap each ReviewItem in SwipeToDismissBox (endToStart, red background + delete icon)
- [x] 1.4 Add AlertDialog for delete confirmation (triggered on swipe)
- [x] 1.5 Implement optimistic delete: add to pendingDeleteIds, show Snackbar with undo, call API on timeout
- [x] 1.6 Implement undo: remove from pendingDeleteIds on Snackbar action
- [x] 1.7 Reset SwipeToDismissBox on cancel

## 2. Click-to-expand detail

- [x] 2.1 Add expandedIds: Set<String> state to ReviewScreen
- [x] 2.2 Make ReviewItem Card clickable to toggle expand
- [x] 2.3 Add AnimatedVisibility section with expandVertically/shrinkVertically
- [x] 2.4 Display detail fields: frequency (Chinese), lastCompleted, dueStatus with color, notes, category
- [x] 2.5 Add expand/collapse arrow icon (ExpandMore/ExpandLess)
- [x] 2.6 Color-code dueStatus: overdue=red, due_today=orange, due_soon=yellow, upcoming=green, completed/paused=gray

## 3. Build verification

- [x] 3.1 Build passes with `assembleDebug` (ReviewScreen compiles clean; pre-existing TaskScreen errors unrelated)
