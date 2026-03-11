## ADDED Requirements

### Requirement: Copy AI reply to clipboard
The system SHALL display a copy button below each AI reply bubble. When tapped, the system SHALL copy the full plain-text content of that AI reply to the system clipboard and show a brief confirmation.

#### Scenario: Successful copy
- **WHEN** user taps the copy button below an AI reply
- **THEN** the full text of that AI reply is placed on the system clipboard AND a "已复制" snackbar/toast is shown for ~2 seconds

#### Scenario: Copy during streaming
- **WHEN** AI is currently streaming a response
- **THEN** the copy button SHALL NOT be shown on the in-progress message (only on completed messages)

### Requirement: Regenerate last AI reply
The system SHALL display a regenerate button below the last AI reply in the conversation. When tapped, the system SHALL delete the last AI message and re-send the preceding context to the LLM, streaming a new response.

#### Scenario: Successful regeneration
- **WHEN** user taps the regenerate button on the last AI reply AND no streaming is in progress
- **THEN** the last AI message is deleted from the database AND the system re-sends the conversation context to the LLM AND streams the new response in real-time

#### Scenario: Regenerate not available during streaming
- **WHEN** AI is currently streaming a response (isSending = true)
- **THEN** the regenerate button SHALL be hidden or disabled on all messages

#### Scenario: Regenerate only on last AI message
- **WHEN** the conversation has multiple AI replies
- **THEN** the regenerate button SHALL only appear on the most recent AI message, not on earlier ones

#### Scenario: Regeneration fails due to network error
- **WHEN** user taps regenerate AND the API call fails
- **THEN** the system SHALL show an error message AND the deleted AI message is not restored (user can retry via regenerate again)

### Requirement: Action bar layout
The action bar SHALL appear below each completed AI reply bubble as a horizontal row of icon buttons. The bar SHALL use small icon buttons (contentDescription for accessibility) with subtle styling that does not distract from the conversation flow.

#### Scenario: Action bar visibility
- **WHEN** an AI message is fully rendered (not streaming)
- **THEN** a horizontal action bar with copy (and regenerate if last message) icons appears below the bubble

#### Scenario: User messages have no action bar
- **WHEN** a message is from the user
- **THEN** no action bar is displayed
