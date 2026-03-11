## ADDED Requirements

### Requirement: Edit mode entry via long press
The shortcut bar SHALL enter edit mode when the user long-presses any chip. The device SHALL provide haptic feedback (short vibration) upon entering edit mode.

#### Scenario: Long press enters edit mode
- **WHEN** user long-presses any chip in the shortcut bar
- **THEN** the shortcut bar enters edit mode with haptic vibration feedback

#### Scenario: Normal tap in normal mode navigates
- **WHEN** user taps a chip in normal mode
- **THEN** the system navigates to the corresponding feature page (existing behavior unchanged)

### Requirement: Pin state toggle in edit mode
In edit mode, tapping a chip SHALL toggle its pin state. Tapping a pinned chip unpins it. Tapping an unpinned chip pins it (appended to end of pinned list). The maximum number of pinned chips SHALL be 4. Tapping SHALL NOT trigger navigation.

#### Scenario: Unpin a pinned chip
- **WHEN** user taps a pinned chip in edit mode
- **THEN** the chip is removed from the pinned list and moves to the unpinned section

#### Scenario: Pin an unpinned chip
- **WHEN** user taps an unpinned chip in edit mode and pinned count is less than 4
- **THEN** the chip is appended to the end of the pinned list

#### Scenario: Pin rejected when limit reached
- **WHEN** user taps an unpinned chip in edit mode and pinned count is already 4
- **THEN** the system does NOT pin the chip and provides feedback (chip shake animation or Toast)

#### Scenario: Tap does not navigate in edit mode
- **WHEN** user taps any chip in edit mode
- **THEN** the system does NOT navigate to any feature page

### Requirement: PushPin icon indicates pin state
In edit mode, each chip SHALL display a PushPin icon. Pinned chips show the icon upright (0° rotation). Unpinned chips show the icon rotated 45°. In normal mode, no PushPin icon is displayed.

#### Scenario: Pinned chip icon
- **WHEN** the shortcut bar is in edit mode
- **THEN** pinned chips display an upright PushPin icon (0° rotation)

#### Scenario: Unpinned chip icon
- **WHEN** the shortcut bar is in edit mode
- **THEN** unpinned chips display a PushPin icon rotated 45°

#### Scenario: Normal mode has no icons
- **WHEN** the shortcut bar is in normal mode
- **THEN** chips do not display PushPin icons

### Requirement: Exit edit mode on outside tap
Tapping outside the shortcut bar area SHALL exit edit mode and persist the current pinned list to DataStore.

#### Scenario: Tap outside saves and exits
- **WHEN** user taps any area outside the shortcut bar while in edit mode
- **THEN** the system saves the pinned list to DataStore and returns to normal mode

#### Scenario: No changes made
- **WHEN** user enters edit mode and taps outside without changing any pin state
- **THEN** the system exits edit mode without writing to DataStore

### Requirement: Shortcut ordering
Pinned chips SHALL appear before unpinned chips. Pinned chips maintain their list order. Unpinned chips are sorted by most recent usage (descending). This ordering applies in both normal and edit modes.

#### Scenario: Ordering after pin change
- **WHEN** user pins a chip and exits edit mode
- **THEN** the newly pinned chip appears at the end of the pinned section, before all unpinned chips

### Requirement: State survives configuration change
The edit mode state (whether editing or not) SHALL survive Android configuration changes (screen rotation). The pinned list persists in DataStore across app restarts.

#### Scenario: Screen rotation during edit mode
- **WHEN** user rotates the screen while in edit mode
- **THEN** the shortcut bar remains in edit mode with correct pin states displayed
