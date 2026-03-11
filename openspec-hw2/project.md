# Ergou (二狗) Project Overview

Android personal AI assistant with a classical scholar + loyal dog personality. Solo developer project focused on privacy-first local-first design.

## Technology Stack
- Language: Kotlin 2.0.21
- Platform: Android (minSdk 26, targetSdk 35)
- UI: Jetpack Compose + Material 3
- Database: Room 2.7.0 (with KSP)
- Network: Ktor 3.0.3
- DI: Koin 4.0.2
- LLM: DeepSeek API (streaming SSE)
- Build: AGP 9.0.1, Gradle 9.2.1

## Project Structure
```
com.ergou.app/
├── data/
│   ├── local/      # Room (dao/, entity/, database/)
│   ├── remote/     # API (dto/, service/)
│   ├── repository/ # Business logic
│   └── tool/       # Tool Use (tools/)
├── ui/             # Feature screens (chat/, settings/, soul/, etc.)
├── di/             # Koin modules
└── util/           # Utilities
```

## Conventions
- MVVM architecture strictly enforced
- One public class per file, file name = class name
- Entity suffix for Room entities, Dao for DAOs, Tool for tools
- Timber for all logging: `[Module] action key=value`
- StateFlow for ViewModel state, Channel for one-shot events

## Key Systems
- **Tool Calling**: 17 tools via ToolExecutor (max 5 rounds, non-streaming for tools)
- **Memory**: Auto-extraction via MemoryExtractor, strength decay (7-day half-life)
- **Soul Evolution**: SoulEvolver analyzes conversations, evolves personality parameters
- **Next API**: Task/Routine/Expense/English/Trip data synced with Next backend

## Error Handling
- Custom exceptions: ErgouException → ApiException, ToolException, StorageException
- Repository catches and converts, ViewModel updates UI state
- Network retry: exponential backoff (1s → 2s → 4s), max 3 attempts
- Non-idempotent operations (send message) don't retry

## Security
- API Key in encrypted DataStore
- PromptSanitizer.sanitize() for all user input in prompts
- No sensitive data in logs
- All data local, no telemetry
