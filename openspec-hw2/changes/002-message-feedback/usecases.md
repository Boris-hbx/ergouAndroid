## Use Cases

### Use Case: Rate AI reply

**Primary Actor:** User
**Scope:** Ergou Chat
**Level:** Subfunction

**Preconditions:**
- AI reply is fully rendered (not streaming)

**Success Guarantee (Postconditions):**
- The message's feedback value is persisted in the database
- The selected button is visually highlighted

**Trigger:** User taps thumbs-up or thumbs-down below an AI reply

**Main Success Scenario:**
1. User views a completed AI reply with action bar
2. User taps thumbs-up (or thumbs-down)
3. System saves feedback=1 (or -1) for this message
4. The tapped button becomes highlighted, the other remains neutral

**Extensions:**
- 2a. User taps the same button again (toggle off): System sets feedback=null, button returns to neutral
- 2b. User taps the opposite button: System updates feedback to the new value, highlights switch
