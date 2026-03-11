## ADDED Requirements

### Requirement: Hamburger menu button on ChatScreen
The system SHALL display a hamburger menu icon button (three horizontal lines) in the top-left corner of the ChatScreen top app bar. Tapping it SHALL navigate to the Feature Hub page.

#### Scenario: Button visible
- **WHEN** user is on the ChatScreen
- **THEN** a hamburger menu icon is displayed as the navigationIcon of the TopAppBar

#### Scenario: Button navigates to feature hub
- **WHEN** user taps the hamburger menu icon
- **THEN** the system navigates to the feature-hub route

### Requirement: Feature Hub page layout
The system SHALL display a full-screen page with a top bar ("功能广场" title + back button) and a grid of feature cards. Each card SHALL show an emoji icon, a feature name, and a brief description.

#### Scenario: All features displayed
- **WHEN** user enters the Feature Hub page
- **THEN** a grid displays cards for all 7 features: 任务, 习惯, 回顾, 记账, 差旅, 学习, 养生

#### Scenario: Navigate to feature
- **WHEN** user taps a feature card
- **THEN** the system navigates to the corresponding feature route (task, routine, review, expense, trip, english, health)

#### Scenario: Back navigation
- **WHEN** user taps the back arrow
- **THEN** the system returns to ChatScreen
