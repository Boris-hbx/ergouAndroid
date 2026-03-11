## ADDED Requirements

### Requirement: Persist message feedback
The system SHALL store a feedback value per message: null (no feedback), 1 (liked), -1 (disliked). The value SHALL be persisted in the messages table.

#### Scenario: Save positive feedback
- **WHEN** user taps thumbs-up on an AI reply
- **THEN** message feedback is set to 1 in the database

#### Scenario: Save negative feedback
- **WHEN** user taps thumbs-down on an AI reply
- **THEN** message feedback is set to -1 in the database

#### Scenario: Toggle off feedback
- **WHEN** user taps the already-selected feedback button
- **THEN** message feedback is set to null (cleared)

#### Scenario: Switch feedback
- **WHEN** user taps the opposite feedback button
- **THEN** message feedback is updated to the new value

### Requirement: Display feedback state in action bar
The system SHALL show thumbs-up and thumbs-down icons in the AI message action bar. The selected state SHALL be visually distinct (filled icon or color change).

#### Scenario: No feedback set
- **WHEN** message has no feedback (null)
- **THEN** both icons are shown in neutral/outline style

#### Scenario: Positive feedback set
- **WHEN** message feedback is 1
- **THEN** thumbs-up icon is highlighted, thumbs-down is neutral

#### Scenario: Feedback only on AI messages
- **WHEN** message is from user
- **THEN** no feedback icons are displayed
