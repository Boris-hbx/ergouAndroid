## ADDED Requirements

### Requirement: Photo thumbnail tap opens full-screen preview
When the user taps a receipt photo thumbnail, the system SHALL display a full-screen (or dialog) preview of that photo.

#### Scenario: Tap existing uploaded photo thumbnail
- **WHEN** user taps on an uploaded photo thumbnail in the photo list
- **THEN** a full-screen preview of the photo is displayed with a close/back action

#### Scenario: Tap pending (not yet uploaded) photo thumbnail
- **WHEN** user taps on a pending local photo thumbnail
- **THEN** a full-screen preview of the local photo is displayed with a close/back action

#### Scenario: Close preview returns to edit form
- **WHEN** user dismisses the photo preview (back button or close icon)
- **THEN** the edit form is shown in its previous state with no data loss

### Requirement: Delete button is fully visible and not clipped
The delete button (gray ×) on each photo thumbnail SHALL be fully visible and not obscured by adjacent elements. It SHALL have adequate tap target size (at least 24dp).

#### Scenario: Delete button renders without clipping
- **WHEN** a photo thumbnail with delete button is displayed
- **THEN** the × icon is fully visible with no part hidden or clipped by parent bounds

#### Scenario: Delete button is tappable
- **WHEN** user taps the × delete button on a photo thumbnail
- **THEN** the photo is removed from the list without requiring precise targeting
