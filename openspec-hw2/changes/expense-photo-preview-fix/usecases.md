## Use Cases

### Use Case: Preview expense photo in edit dialog

**Primary Actor:** User
**Scope:** Ergou expense module (ExpenseEditDialog)
**Level:** User goal

**Stakeholders and Interests:**
- User — wants to view uploaded receipt photos at full size to verify content before saving

**Preconditions:**
- User has opened the expense edit dialog for an existing expense that has uploaded photos
- OR user has added local photos (not yet uploaded) in the new expense dialog

**Success Guarantee (Postconditions):**
- User has viewed the photo at full-screen size and can confirm its content
- The edit dialog remains open and unchanged after dismissing the preview

**Trigger:** User taps a photo thumbnail in the edit dialog

**Main Success Scenario:**
1. User opens the expense edit dialog (new or existing expense).
2. System displays photo thumbnails in the photo section (existing server photos and/or local dialog photos).
3. User taps a photo thumbnail.
4. System displays the photo in a full-screen overlay.
5. User reviews the photo and taps to dismiss.
6. System closes the overlay and returns to the edit dialog in its previous state.

**Extensions:**
- 2a. No photos exist: System shows only the "add photo" button; no thumbnails to tap.
- 4a. Server photo fails to load (network error / auth expired): System shows a placeholder or error state in the overlay.
- 4b. Local photo URI is no longer accessible: System shows an error message and removes the broken thumbnail.

**Open Questions:**
- Should the full-screen preview support pinch-to-zoom? (Current DetailScreen implementation does not; keep parity for now.)
