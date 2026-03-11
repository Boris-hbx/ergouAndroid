## ADDED Requirements

### Requirement: Generate followup suggestions after AI reply
The system SHALL generate 2-3 suggested followup questions after each completed AI reply. The generation SHALL happen in a background coroutine and SHALL NOT block the main conversation flow.

#### Scenario: Successful generation
- **WHEN** AI reply streaming completes with non-empty content
- **THEN** the system sends a background LLM request with the last user message and AI reply as context AND generates 2-3 short followup questions

#### Scenario: Generation fails silently
- **WHEN** the background LLM request fails (network error, timeout, invalid response)
- **THEN** no suggestions are shown AND no error is displayed to the user

#### Scenario: New message clears suggestions
- **WHEN** user sends a new message (typed or via suggestion chip)
- **THEN** existing suggestions are cleared immediately

### Requirement: Display suggestion chips
The system SHALL display generated suggestions as horizontally scrollable `SuggestionChip` components below the last AI message in the conversation.

#### Scenario: Chips visible after generation
- **WHEN** suggestions have been generated AND no streaming is in progress
- **THEN** 2-3 chips are shown below the last AI message, horizontally scrollable

#### Scenario: Chips hidden during streaming
- **WHEN** AI is streaming a new response
- **THEN** suggestion chips are hidden

### Requirement: Send suggestion on tap
The system SHALL send the tapped suggestion text as a user message when a chip is tapped. This SHALL behave identically to the user typing and pressing send.

#### Scenario: Tap sends message
- **WHEN** user taps a suggestion chip
- **THEN** the chip text is sent as a new user message AND all suggestions are cleared AND AI begins responding
