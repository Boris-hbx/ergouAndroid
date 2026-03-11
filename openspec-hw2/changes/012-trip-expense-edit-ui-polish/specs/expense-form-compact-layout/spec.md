## ADDED Requirements

### Requirement: Description field uses compact font size
The description OutlinedTextField SHALL use a smaller font size (14sp) for the input text, reduced from the default size.

#### Scenario: Description text renders at compact size
- **WHEN** user types in the description input field
- **THEN** the text renders at 14sp font size

### Requirement: Amount and currency row uses compact dimensions
The amount input field and currency dropdown SHALL use reduced height (48dp) and font size (14sp), matching the date row's visual weight. The row SHALL have reduced vertical padding.

#### Scenario: Amount field renders compactly
- **WHEN** the amount/currency row is displayed
- **THEN** the amount TextField height is 48dp and text size is 14sp

#### Scenario: Currency dropdown matches amount field height
- **WHEN** the currency ExposedDropdownMenu is displayed
- **THEN** its height matches the amount field (48dp) and its text size is 14sp

### Requirement: Date row uses compact height
The date picker Surface SHALL use a reduced height (48dp) matching the amount/currency row.

#### Scenario: Date picker renders at compact height
- **WHEN** the date row is displayed
- **THEN** the clickable date Surface height is 48dp

### Requirement: All compact fields maintain consistent visual rhythm
All input fields (description, amount, currency, date) SHALL use consistent vertical spacing (8dp between fields) and the same compact text size (14sp).

#### Scenario: Vertical spacing is uniform
- **WHEN** the form is displayed with all fields visible
- **THEN** the vertical gap between each field row is 8dp
