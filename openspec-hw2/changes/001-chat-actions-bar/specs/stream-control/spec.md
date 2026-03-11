## ADDED Requirements

### Requirement: Stop button replaces send button during streaming
The system SHALL replace the send button with a stop button when AI is actively streaming a response. The stop button SHALL be visually distinct (e.g., filled square icon) to indicate the stop action.

#### Scenario: Send button transforms to stop button
- **WHEN** the system starts streaming an AI response (isSending transitions to true)
- **THEN** the send button in the input area SHALL be replaced by a stop button icon

#### Scenario: Stop button reverts to send button
- **WHEN** streaming completes (naturally or via cancellation)
- **THEN** the stop button SHALL revert to the normal send button

### Requirement: Cancel streaming on stop
The system SHALL cancel the ongoing LLM API request when the user taps the stop button. The coroutine Job performing the streaming SHALL be cancelled.

#### Scenario: User stops mid-stream with partial content
- **WHEN** user taps the stop button while AI has generated partial content
- **THEN** the API request is cancelled AND the partial content is saved as the AI reply in the database AND the UI returns to idle state

#### Scenario: User stops before any content arrives
- **WHEN** user taps the stop button before any streaming content has been received
- **THEN** the API request is cancelled AND no AI message is saved AND the UI returns to idle state

#### Scenario: Stream finishes before stop is processed
- **WHEN** user taps stop but the stream has already completed
- **THEN** the system SHALL treat it as a normal completion (full response saved)

### Requirement: Input field state during streaming
The input field SHALL remain visible but disabled (not editable) during streaming. This prevents the user from sending another message while one is being generated.

#### Scenario: Input disabled during streaming
- **WHEN** AI is streaming a response
- **THEN** the input text field SHALL be read-only AND the stop button is the only actionable element in the input area

#### Scenario: Input re-enabled after stop
- **WHEN** user stops streaming or streaming completes
- **THEN** the input text field SHALL become editable again AND focus is restored
