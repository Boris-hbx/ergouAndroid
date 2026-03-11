## ADDED Requirements

### Requirement: Reminder cloud CRUD
The system SHALL store all reminders on the Next API backend. ReminderEntity SHALL have `remoteId` for backend linking.

#### Scenario: Create reminder
- **WHEN** user asks 二狗 to set a reminder
- **THEN** system saves to backend via `POST /api/reminders` and writes to local cache, then registers AlarmManager

#### Scenario: Complete reminder
- **WHEN** reminder is marked completed
- **THEN** system updates backend via `PUT /api/reminders/{id}` and updates local cache

#### Scenario: Delete reminder
- **WHEN** user deletes a reminder
- **THEN** system deletes from backend and local cache, cancels AlarmManager

#### Scenario: Restore reminders on new device
- **WHEN** user logs in on new device
- **THEN** system pulls all pending reminders from backend, writes to local cache, and registers AlarmManager for each

### Requirement: Backend API contract for reminders
The Next.js backend SHALL expose:

- `GET /api/reminders` — list all reminders
- `GET /api/reminders?pending=true` — list pending (not completed) reminders
- `POST /api/reminders` — create reminder (body: `{ content, triggerAt }`)
- `PUT /api/reminders/{id}` — update reminder (body: `{ isCompleted? }`)
- `DELETE /api/reminders/{id}` — delete reminder

#### Scenario: Reminder fields preserved
- **WHEN** reminder is saved to backend
- **THEN** all fields (content, triggerAt, isCompleted, createdAt) SHALL be preserved

### Requirement: Room migration for reminders
ReminderEntity SHALL add `remoteId: String?` and `syncedAt: Long` columns.

#### Scenario: Migration preserves existing reminders
- **WHEN** database migrates
- **THEN** existing reminders are preserved with `remoteId = null`
