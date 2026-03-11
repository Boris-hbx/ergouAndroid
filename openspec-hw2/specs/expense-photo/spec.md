## ADDED Requirements

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

### Requirement: Upload photo to expense entry
The system SHALL upload photos to the Next backend via `POST /api/expenses/:id/photos` as multipart form data. The upload SHALL enforce a 10MB file size limit on the client side. The system SHALL display upload progress or a loading indicator.

#### Scenario: Successful upload
- **WHEN** user selects a photo and the upload completes
- **THEN** the photo appears in the detail page's photo section
- **THEN** the expense's photo_count increments

#### Scenario: File exceeds 10MB
- **WHEN** the selected file is larger than 10MB
- **THEN** system displays "照片不能超过 10MB" and does not attempt upload

#### Scenario: Upload network failure
- **WHEN** the upload request fails due to network error
- **THEN** system displays an error message with retry option

### Requirement: Display and manage uploaded photos
The system SHALL display uploaded photos as a scrollable grid of thumbnails on the detail page. Photos SHALL be loaded via authenticated URL (`GET /api/uploads/{user_id}/{filename}` with session cookie). The system SHALL support viewing a photo full-screen and deleting a photo.

#### Scenario: Display photo thumbnails
- **WHEN** the detail page loads an expense with photos
- **THEN** system displays photo thumbnails in a horizontal row or grid, loaded via Coil with auth headers

#### Scenario: View photo full-screen
- **WHEN** user taps a photo thumbnail
- **THEN** system displays the photo in a full-screen overlay with dismiss gesture

#### Scenario: Delete a photo
- **WHEN** user triggers delete on a photo and confirms
- **THEN** system calls `DELETE /api/expenses/photos/:photo_id` and removes the thumbnail from the grid

#### Scenario: No photos
- **WHEN** the expense has no uploaded photos
- **THEN** system shows only an "add photo" button, no empty grid

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
