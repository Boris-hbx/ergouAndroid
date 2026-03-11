## Requirements

### Requirement: AI-generated description
The system SHALL generate a concise description summary when AI analyzes a receipt. The description SHALL be auto-filled into the 描述 field after analysis completes.

#### Scenario: AI fills description after analysis
- **WHEN** AI completes receipt analysis
- **THEN** the 描述 field is populated with a 1-2 sentence summary of the purchase
- **AND** the summary may include classical Chinese references as a stylistic touch

#### Scenario: User edits AI description
- **WHEN** AI has filled the description
- **THEN** the user can freely edit or replace the AI-generated text

#### Scenario: Re-analysis updates description
- **WHEN** user triggers "让二狗分析" on an already-analyzed expense
- **THEN** the description field is updated with the new AI-generated summary
- **AND** the previous description is replaced

### Requirement: AI extracts receipt time
The system SHALL extract the transaction time (hour:minute) from the receipt when available.

#### Scenario: Receipt has time information
- **WHEN** AI analyzes a receipt that includes a timestamp
- **THEN** the time field is populated with the extracted time (HH:mm format)

#### Scenario: Receipt has no time information
- **WHEN** AI analyzes a receipt without a timestamp
- **THEN** the time field remains at placeholder "票据时间"

### Requirement: AI bilingual translation
The system SHALL provide bilingual translations for receipt items during analysis.

#### Scenario: English receipt items
- **WHEN** AI analyzes an English receipt
- **THEN** each item includes the original English name and a Chinese translation

#### Scenario: Chinese receipt items
- **WHEN** AI analyzes a Chinese receipt
- **THEN** each item includes the original Chinese name and an English translation
