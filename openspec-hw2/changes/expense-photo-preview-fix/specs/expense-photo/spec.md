## MODIFIED Requirements

### Requirement: Display and manage uploaded photos
The system SHALL display uploaded photos as a scrollable grid of thumbnails on the detail page AND in the edit dialog. Photos SHALL be loaded via authenticated URL (`GET /api/uploads/{user_id}/{filename}` with session cookie). The system SHALL support viewing a photo full-screen in both the detail page and the edit dialog. The system SHALL support deleting a photo.

#### Scenario: Display photo thumbnails
- **WHEN** the detail page loads an expense with photos
- **THEN** system displays photo thumbnails in a horizontal row or grid, loaded via Coil with auth headers

#### Scenario: Display photo thumbnails in edit dialog
- **WHEN** user opens the edit dialog for an expense with uploaded photos
- **THEN** system displays existing photo thumbnails in a horizontal row, loaded via Coil with auth headers

#### Scenario: View photo full-screen from detail page
- **WHEN** user taps a photo thumbnail on the detail page
- **THEN** system displays the photo in a full-screen overlay with dismiss gesture

#### Scenario: View server photo full-screen from edit dialog
- **WHEN** user taps an existing (server-uploaded) photo thumbnail in the edit dialog
- **THEN** system displays the photo in a full-screen overlay using the authenticated URL
- **THEN** user taps to dismiss and the edit dialog returns to its previous state

#### Scenario: View local photo full-screen from edit dialog
- **WHEN** user taps a not-yet-uploaded (local URI) photo thumbnail in the edit dialog
- **THEN** system displays the photo in a full-screen overlay using the local URI
- **THEN** user taps to dismiss and the edit dialog returns to its previous state

#### Scenario: Delete a photo
- **WHEN** user triggers delete on a photo and confirms
- **THEN** system calls `DELETE /api/expenses/photos/:photo_id` and removes the thumbnail from the grid

#### Scenario: No photos
- **WHEN** the expense has no uploaded photos
- **THEN** system shows only an "add photo" button, no empty grid

#### Scenario: Photo load failure in full-screen overlay
- **WHEN** user taps a thumbnail but the full-size image fails to load (network error or expired auth)
- **THEN** system displays an error placeholder in the overlay
