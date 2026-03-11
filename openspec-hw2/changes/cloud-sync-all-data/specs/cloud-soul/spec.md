## ADDED Requirements

### Requirement: Soul state cloud persistence
The system SHALL store the singleton soul state on the Next API backend. SoulStateEntity is always `id=1` locally; backend stores one record per user.

#### Scenario: Update soul state
- **WHEN** SoulEvolver adjusts personality parameters
- **THEN** system writes updated soul state to backend via `PUT /api/soul/state` and updates local cache

#### Scenario: Read soul state
- **WHEN** system needs soul state for prompt building
- **THEN** system reads from local Room cache

#### Scenario: Initialize soul state on new device
- **WHEN** user logs in on new device and backend has soul state
- **THEN** system pulls backend soul state and writes to local cache

#### Scenario: First-time user has no soul state
- **WHEN** user logs in and backend has no soul state
- **THEN** system creates default soul state locally and pushes to backend

### Requirement: Soul evolution log cloud persistence
The system SHALL store evolution logs on the Next API backend.

#### Scenario: Append evolution log
- **WHEN** SoulEvolver creates an evolution log entry
- **THEN** system saves to backend via `POST /api/soul/logs` and writes to local cache

#### Scenario: Query recent logs
- **WHEN** soul UI displays evolution history
- **THEN** system reads from local Room cache

### Requirement: Backend API contract for soul
The Next.js backend SHALL expose:

- `GET /api/soul/state` — get current soul state
- `PUT /api/soul/state` — upsert soul state (body: all soul parameters)
- `GET /api/soul/logs?limit=N` — get recent evolution logs
- `POST /api/soul/logs` — create evolution log entry

#### Scenario: Soul state upsert
- **WHEN** `PUT /api/soul/state` is called
- **THEN** backend creates or updates the user's soul state record

### Requirement: Room migration for soul
SoulEvolutionLogEntity SHALL add `remoteId: String?` and `syncedAt: Long` columns. SoulStateEntity adds `syncedAt: Long` only (singleton, no remoteId needed).

#### Scenario: Migration preserves soul data
- **WHEN** database migrates
- **THEN** existing soul state and logs are preserved
