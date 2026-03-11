## ADDED Requirements

### Requirement: Full sync on login
The system SHALL perform a full data sync when user logs in on a new device (local Room is empty or stale).

#### Scenario: Login triggers full sync
- **WHEN** user logs in and local Room has no synced data
- **THEN** SyncManager pulls all data from backend in order: soul state → sessions + messages → memories → people → reminders
- **THEN** all data is written to local Room cache with `remoteId` populated

#### Scenario: Login with existing local data
- **WHEN** user logs in and local Room already has data with `remoteId`
- **THEN** SyncManager pulls latest data and merges (backend wins for conflicts)

#### Scenario: Sync progress indication
- **WHEN** full sync is in progress
- **THEN** UI shows a sync progress indicator

### Requirement: Write-through sync pattern
All write operations (create/update/delete) SHALL follow the write-through pattern: write to backend first, then update local cache.

#### Scenario: Successful write-through
- **WHEN** a create/update/delete operation succeeds on backend
- **THEN** local Room cache is updated with backend response (including remoteId)

#### Scenario: Backend write fails
- **WHEN** backend write fails (network error, server error)
- **THEN** system writes to local Room cache with `remoteId = null` and `syncedAt = 0`
- **THEN** system logs the failure via Timber.w
- **THEN** operation does NOT block the user (optimistic local-first fallback)

#### Scenario: Retry unsynced data
- **WHEN** app detects unsynced local records (remoteId = null) and network is available
- **THEN** system attempts to push unsynced records to backend

### Requirement: SyncManager class
The system SHALL have a `SyncManager` class in the repository layer that coordinates sync operations.

#### Scenario: SyncManager initialization
- **WHEN** SyncManager is created (via Koin DI)
- **THEN** it has access to NextApiService and all local DAOs

#### Scenario: Full sync method
- **WHEN** `SyncManager.fullSync()` is called
- **THEN** it sequentially fetches all data types from backend and writes to Room
- **THEN** it returns a Result indicating success or partial failure

#### Scenario: Push unsynced method
- **WHEN** `SyncManager.pushUnsynced()` is called
- **THEN** it finds all local records with `remoteId = null` and pushes them to backend

### Requirement: Offline resilience
The system SHALL remain fully functional when the backend is unavailable, using local Room as the primary data source for reads.

#### Scenario: Read operations always work offline
- **WHEN** backend is unavailable
- **THEN** all read operations (list sessions, recall memories, get soul state) work from local Room cache

#### Scenario: Write operations degrade gracefully
- **WHEN** backend is unavailable and user performs a write
- **THEN** write succeeds locally and is queued for sync

### Requirement: Logout clears local cache
The system SHALL clear all local Room data when user logs out, to prevent data leakage between accounts.

#### Scenario: Logout clears data
- **WHEN** user logs out
- **THEN** system clears all Room tables (sessions, messages, memories, people, reminders, soul state, soul logs)
- **THEN** system clears DataStore session token
