## MODIFIED Requirements

### Requirement: Photo picker and camera capture
The system SHALL provide two ways to select a photo: pick from gallery and capture with camera. The system SHALL use Android ActivityResult APIs (`PickMultipleVisualMedia` for gallery batch selection, `TakePicture` for camera). Camera capture SHALL request `CAMERA` permission at runtime. The gallery picker SHALL support selecting multiple photos at once.

#### Scenario: Pick photo from gallery
- **WHEN** user selects "from gallery" option
- **THEN** system launches the multi-photo picker and returns the selected image URIs

#### Scenario: Capture photo with camera
- **WHEN** user selects "take photo" and camera permission is granted
- **THEN** system launches the camera app and returns the captured image URI

#### Scenario: Camera permission denied
- **WHEN** user selects "take photo" but denies camera permission
- **THEN** system displays a message explaining camera access is needed, and does not crash

#### Scenario: User cancels selection
- **WHEN** user dismisses the picker or camera without selecting
- **THEN** no action is taken, the UI remains unchanged

### Requirement: AI receipt parsing preview
The system SHALL support sending photos to `POST /api/expenses/parse-preview` for AI analysis. In batch mode, the system SHALL process multiple photos sequentially, sending each as base64. The system SHALL display a preview of all parsed results and allow the user to edit each before saving.

#### Scenario: Successful AI parse (batch)
- **WHEN** user initiates batch scan with multiple photos
- **THEN** system sends each photo to parse-preview API sequentially with progress indicator
- **THEN** system displays all parsed results in a scrollable list

#### Scenario: AI parse timeout
- **WHEN** the parse-preview API does not respond within 120 seconds
- **THEN** system displays a timeout message for that photo and continues with next

#### Scenario: AI parse returns empty or invalid result
- **WHEN** the API returns an empty items list or missing amount
- **THEN** system skips that photo and displays a message that parsing failed

#### Scenario: User edits parsed result before saving
- **WHEN** the preview is displayed
- **THEN** user SHALL be able to edit amount, date, notes, tags, and currency for each result before confirming

### Requirement: Save parsed receipt as new expense
The system SHALL create new expense entries from the confirmed parse previews. In batch mode, the system SHALL save all confirmed results sequentially, calling `POST /api/expenses` for each and uploading the corresponding original photo.

#### Scenario: Save parsed receipts (batch)
- **WHEN** user confirms all parse previews
- **THEN** system creates each expense via API with items array
- **THEN** system uploads the corresponding original photo to each new expense entry
- **THEN** system navigates to the expense list with refreshed data

#### Scenario: Save fails
- **WHEN** a create API call fails
- **THEN** system displays an error for that item and preserves the preview data for retry

#### Scenario: Photo upload fails after successful create
- **WHEN** the expense is created but photo upload fails
- **THEN** system still proceeds (expense saved without photo)
- **THEN** system displays a warning that the photo could not be attached
