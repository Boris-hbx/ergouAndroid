## ADDED Requirements

### Requirement: Session cloud CRUD
The system SHALL store all chat sessions on the Next API backend. Each session SHALL have a remote ID returned by the backend. Local Room SessionEntity SHALL cache backend data with a `remoteId` field.

#### Scenario: Create new session
- **WHEN** user starts a new conversation
- **THEN** system creates session on backend via `POST /api/sessions` and stores returned ID as `remoteId` in local cache

#### Scenario: Update session title
- **WHEN** session title is updated (auto-generated or manual)
- **THEN** system updates backend via `PUT /api/sessions/{id}` and updates local cache

#### Scenario: Delete session
- **WHEN** user deletes a session
- **THEN** system deletes from backend via `DELETE /api/sessions/{id}` and removes from local cache

#### Scenario: List sessions
- **WHEN** user opens session history
- **THEN** system reads from local Room cache (populated during sync)

### Requirement: Message cloud CRUD
The system SHALL store all chat messages on the Next API backend. MessageEntity SHALL have a `remoteId` field linking to the backend record.

#### Scenario: Save user message
- **WHEN** user sends a message
- **THEN** system saves to backend via `POST /api/sessions/{sessionId}/messages` and writes to local cache with `remoteId`

#### Scenario: Save assistant message
- **WHEN** LLM streaming response completes
- **THEN** system saves final assistant message to backend and local cache

#### Scenario: Update message feedback
- **WHEN** user gives feedback (thumbs up/down) on a message
- **THEN** system updates backend via `PUT /api/messages/{id}` and updates local cache

#### Scenario: Load session messages
- **WHEN** user opens a session
- **THEN** system reads messages from local Room cache

#### Scenario: Delete message
- **WHEN** user deletes a message
- **THEN** system deletes from backend and local cache

### Requirement: Backend API contract for sessions
The Next.js backend SHALL expose the following REST endpoints:

- `GET /api/sessions` — list all sessions (sorted by updatedAt DESC)
- `POST /api/sessions` — create session (body: `{ title }`)
- `GET /api/sessions/{id}` — get session by ID
- `PUT /api/sessions/{id}` — update session (body: `{ title }`)
- `DELETE /api/sessions/{id}` — delete session (cascade deletes messages)
- `GET /api/sessions/{id}/messages` — list messages for session (sorted by createdAt ASC)
- `POST /api/sessions/{id}/messages` — create message (body: `{ role, content, tokenCount? }`)
- `PUT /api/messages/{id}` — update message (body: `{ feedback? }`)
- `DELETE /api/messages/{id}` — delete message

#### Scenario: API returns consistent format
- **WHEN** any session/message endpoint is called
- **THEN** response uses `{ id, ...fields, createdAt, updatedAt }` format consistent with existing Next API patterns

### Requirement: Room migration for sessions
Room database SHALL migrate to add `remoteId: String?` and `syncedAt: Long` columns to SessionEntity and MessageEntity.

#### Scenario: Database upgrade from v8 to v9
- **WHEN** app upgrades and Room version changes
- **THEN** migration adds nullable `remoteId` column and `syncedAt` column with default 0 to both tables
- **THEN** existing local data is preserved with `remoteId = null` (unsynced)
