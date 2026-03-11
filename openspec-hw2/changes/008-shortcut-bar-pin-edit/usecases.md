## Use Cases

### Use Case: Customize pinned shortcuts

**Primary Actor:** User
**Scope:** Ergou shortcut bar
**Level:** User goal

**Stakeholders and Interests:**
- User — wants to control which features appear first in the shortcut bar

**Preconditions:**
- User is on the chat screen with shortcut bar visible

**Success Guarantee (Postconditions):**
- Pinned routes list in DataStore reflects user's choices
- Shortcut bar displays pinned items first, unpinned items after

**Trigger:** User long-presses any chip in the shortcut bar

**Main Success Scenario:**
1. User long-presses any shortcut chip.
2. System enters edit mode: chips show PushPin icons (upright for pinned, rotated 45° for unpinned), device vibrates briefly.
3. User taps a pinned chip to unpin it.
4. System moves the chip to the unpinned section (sorted by last usage).
5. User taps an unpinned chip to pin it.
6. System appends the chip to the end of the pinned section.
7. User taps outside the shortcut bar area.
8. System saves the new pinned list to DataStore and exits edit mode.

**Extensions:**
- 2a. User taps outside immediately without making changes: System exits edit mode, no save needed.
- 3a. User unpins all chips: All chips become unpinned and sort by last usage. At least the bar remains functional.
- 7a. User presses back button: Same as tapping outside — save and exit edit mode.

**Open Questions:**
- None
