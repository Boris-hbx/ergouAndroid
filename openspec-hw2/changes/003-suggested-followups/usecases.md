## Use Cases

### Use Case: View suggested followup questions

**Primary Actor:** User
**Scope:** Ergou Chat
**Level:** Subfunction

**Preconditions:**
- AI has completed a reply (not streaming)

**Success Guarantee (Postconditions):**
- 2-3 suggested questions are displayed as chips below the last AI reply

**Trigger:** AI reply streaming completes

**Main Success Scenario:**
1. AI finishes streaming a reply
2. System sends a background LLM request to generate 2-3 followup questions based on conversation context
3. System displays the suggestions as clickable chips below the last AI message
4. User sees the suggestions and can choose to tap one or ignore them

**Extensions:**
- 2a. LLM call fails (network error, timeout): System silently ignores, no suggestions shown
- 2b. LLM returns empty/invalid response: No suggestions shown

---

### Use Case: Send a suggested followup question

**Primary Actor:** User
**Scope:** Ergou Chat
**Level:** User goal

**Preconditions:**
- Suggested followup chips are visible

**Success Guarantee (Postconditions):**
- The selected question is sent as a user message
- Suggestions are cleared
- AI streams a new response

**Trigger:** User taps a suggestion chip

**Main Success Scenario:**
1. User taps one of the suggestion chips
2. System clears all suggestion chips
3. System sends the tapped text as a user message (same as typing and pressing send)
4. AI begins streaming a response

**Extensions:**
- 1a. User starts typing instead of tapping: Suggestions remain visible until new AI reply arrives
