## ADDED Requirements

### Requirement: Memory cloud CRUD
The system SHALL store all memories on the Next API backend. MemoryEntity SHALL have `remoteId` for backend linking.

#### Scenario: Save new memory
- **WHEN** MemoryExtractor or user command creates a memory
- **THEN** system saves to backend via `POST /api/memories` and writes to local cache with `remoteId`

#### Scenario: Update memory (repeat count, access)
- **WHEN** memory is accessed or repeat count increments
- **THEN** system updates backend via `PUT /api/memories/{id}` and updates local cache

#### Scenario: Delete memory
- **WHEN** user deletes a memory
- **THEN** system deletes from backend and local cache

#### Scenario: Search memories
- **WHEN** system needs to recall memories (keyword search or top-strength)
- **THEN** system queries local Room cache (populated during sync)

### Requirement: Person cloud CRUD
The system SHALL store all person records on the Next API backend. PersonEntity SHALL have `remoteId` for backend linking.

#### Scenario: Save new person
- **WHEN** user or system saves a person record
- **THEN** system saves to backend via `POST /api/people` and writes to local cache

#### Scenario: Update person
- **WHEN** person details are updated
- **THEN** system updates backend via `PUT /api/people/{id}` and updates local cache

#### Scenario: Delete person
- **WHEN** user deletes a person record
- **THEN** system deletes from backend and local cache

### Requirement: Backend API contract for memories and people
The Next.js backend SHALL expose:

- `GET /api/memories` — list all memories
- `POST /api/memories` — create memory (body: `{ category, content, importance, repeatCount, emotionWeight, sourceSessionId }`)
- `PUT /api/memories/{id}` — update memory
- `DELETE /api/memories/{id}` — delete memory
- `GET /api/memories/search?keyword=xxx` — search by keyword
- `GET /api/people` — list all people
- `POST /api/people` — create person (body: `{ name, relationship, nickname, attitude, notes }`)
- `PUT /api/people/{id}` — update person
- `DELETE /api/people/{id}` — delete person

#### Scenario: Memory fields preserved
- **WHEN** memory is saved to backend
- **THEN** all fields (category, content, importance, repeatCount, emotionWeight, accessCount, lastAccessedAt, sourceSessionId, createdAt) SHALL be preserved

### Requirement: Room migration for memories and people
Room database SHALL add `remoteId: String?` and `syncedAt: Long` columns to MemoryEntity and PersonEntity.

#### Scenario: Migration preserves existing memories
- **WHEN** database migrates
- **THEN** existing memories and people are preserved with `remoteId = null`
