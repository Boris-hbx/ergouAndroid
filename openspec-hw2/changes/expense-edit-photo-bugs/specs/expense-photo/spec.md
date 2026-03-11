## MODIFIED Requirements

### Requirement: AI receipt parsing preview
The system SHALL support sending photos to AI for analysis. In edit mode with existing server-uploaded photos, the system SHALL download those photos from the server, convert to base64, and include them in the analysis request alongside any local dialog photos. The system SHALL display a preview of parsed results and auto-fill form fields.

#### Scenario: Analyze existing server photos in edit mode
- **WHEN** user taps "让二狗分析" in edit dialog with existing server photos
- **THEN** system downloads each photo from server via authenticated request
- **THEN** system converts downloaded bytes to base64 and sends to ReceiptAnalyzer
- **THEN** system auto-fills amount, title, date, currency, tags from the analysis result

#### Scenario: Mixed local and server photos
- **WHEN** user has both existing server photos and newly added local photos
- **THEN** system combines both sets of base64 images for analysis

#### Scenario: Server photo download failure
- **WHEN** one or more server photos fail to download
- **THEN** system continues analysis with successfully downloaded photos and any local photos
- **THEN** system logs warning for failed downloads

### Requirement: Upload photo to expense entry
The system SHALL upload photos to the Next backend. The system SHALL inform the user when photo uploads fail during expense creation.

#### Scenario: Successful upload
- **WHEN** user selects a photo and the upload completes
- **THEN** the photo appears in the photo section

#### Scenario: Upload failure during new expense creation
- **WHEN** expense is created but one or more photo uploads fail
- **THEN** system SHALL display an error message indicating how many photos failed to upload
