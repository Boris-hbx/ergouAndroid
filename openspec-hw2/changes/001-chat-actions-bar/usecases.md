## Use Cases

### Use Case: Copy AI reply content

**Primary Actor:** User
**Scope:** Ergou Chat
**Level:** Subfunction

**Stakeholders and Interests:**
- User — wants to quickly extract AI reply text for use elsewhere

**Preconditions:**
- At least one AI reply exists in the current conversation

**Success Guarantee (Postconditions):**
- The full text of the AI reply is on the system clipboard
- A brief toast/feedback confirms the copy succeeded

**Trigger:** User taps the copy button below an AI reply bubble

**Main Success Scenario:**
1. User views an AI reply in the conversation
2. User taps the copy icon below the reply
3. System copies the reply's full text to the clipboard
4. System shows a brief "已复制" feedback

**Extensions:**
- 3a. Clipboard write fails: System shows error toast, no crash

---

### Use Case: Regenerate AI reply

**Primary Actor:** User
**Scope:** Ergou Chat
**Level:** User goal

**Stakeholders and Interests:**
- User — unsatisfied with the current AI reply, wants a different response

**Preconditions:**
- The conversation has at least one AI reply
- No streaming is currently in progress

**Success Guarantee (Postconditions):**
- The last AI reply is replaced with a new response
- The new response is persisted in the database

**Trigger:** User taps the regenerate button below the last AI reply

**Main Success Scenario:**
1. User taps the regenerate icon below the last AI reply
2. System deletes the last AI message from the conversation
3. System re-sends the same user message context to the LLM
4. System streams the new AI response, displaying it in real-time
5. System persists the new response once generation completes

**Extensions:**
- 2a. The reply is not the most recent AI message: Regenerate button is only shown on the last AI message, so this cannot happen
- 4a. API call fails (network error, rate limit): System shows error message, the deleted AI message is not restored — user can retry
- 4b. User stops the regeneration mid-stream: See "Stop streaming generation" use case

---

### Use Case: Stop streaming generation

**Primary Actor:** User
**Scope:** Ergou Chat
**Level:** User goal

**Stakeholders and Interests:**
- User — wants to halt a long or unwanted AI response mid-generation

**Preconditions:**
- AI is currently streaming a response (isSending = true, streamingContent is non-empty)

**Success Guarantee (Postconditions):**
- Streaming stops immediately
- The partial response generated so far is persisted as the AI reply
- The UI returns to idle state (input enabled, send button restored)

**Trigger:** User taps the stop button that replaces the send button during streaming

**Main Success Scenario:**
1. User sees the AI generating a response (streaming indicator active)
2. User taps the stop button in the input area
3. System cancels the ongoing API request
4. System saves the partial response as the AI message
5. System restores the input area to idle state (send button, input enabled)

**Extensions:**
- 3a. Stream has already finished by the time cancel is processed: System ignores the cancel, normal completion occurs
- 4a. Partial response is empty (cancelled immediately): System discards the empty message, no AI reply is saved
