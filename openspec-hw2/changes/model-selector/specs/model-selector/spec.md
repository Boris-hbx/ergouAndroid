## ADDED Requirements

### Requirement: Model provider persistence
The system SHALL persist the user's selected model provider (DEEPSEEK or CLAUDE) in DataStore. The selection SHALL survive app restarts and process death. Default value SHALL be DEEPSEEK.

#### Scenario: First launch default
- **WHEN** user opens the app for the first time
- **THEN** the active model provider is DEEPSEEK

#### Scenario: Selection persists across restart
- **WHEN** user selects CLAUDE as the model provider and restarts the app
- **THEN** the active model provider remains CLAUDE

### Requirement: Model selection dialog
The system SHALL display a model selection dialog when the user taps the "模型设置" item in Settings. The dialog SHALL list all available models, each showing: provider name, model ID, and API Key status. The currently active model SHALL be highlighted with a radio button.

#### Scenario: Open model selection dialog
- **WHEN** user taps "模型设置" in Settings
- **THEN** a dialog appears listing DeepSeek (deepseek-chat) and Claude (claude-opus-4-6) with radio buttons

#### Scenario: Current model highlighted
- **WHEN** the dialog opens and CLAUDE is the active provider
- **THEN** the Claude option has its radio button selected

#### Scenario: Select a different model with Key set
- **WHEN** user selects a model whose API Key is already configured
- **THEN** the system persists the selection, closes the dialog, and updates the Settings subtitle

#### Scenario: Select a model with Key missing
- **WHEN** user selects a model whose API Key is not set
- **THEN** the system opens the API Key input dialog for that model before persisting the selection

### Requirement: API Key configuration from model dialog
The system SHALL allow users to set or modify a model's API Key directly from the model selection dialog via a "设置" or "修改" link. Tapping the link SHALL open the API Key input dialog with the provider name as title and model ID as subtitle.

#### Scenario: Set API Key for Claude
- **WHEN** user taps "设置" on the Claude option (Key not set)
- **THEN** a dialog appears with title "Claude API Key", subtitle "claude-opus-4-6", and a text input field

#### Scenario: Modify existing API Key
- **WHEN** user taps "修改" on a model with Key already set
- **THEN** the same API Key dialog appears, allowing the user to overwrite the key

#### Scenario: Save API Key
- **WHEN** user enters a non-empty key and taps "保存"
- **THEN** the key is written to DataStore and the model dialog updates the Key status to "已设置"

#### Scenario: Cancel API Key input
- **WHEN** user taps "取消" in the API Key dialog
- **THEN** no changes are made and the user returns to the model selection dialog

### Requirement: Settings page model display
The "模型设置" item in Settings SHALL display the currently active model as subtitle in the format "{Provider} · {model-id}" (e.g., "Claude · claude-opus-4-6"). The API Key items SHALL be removed from the "账号" section — Key management is now within the model dialog.

#### Scenario: Display current model in settings
- **WHEN** user opens Settings and CLAUDE is active
- **THEN** the "模型设置" subtitle reads "Claude · claude-opus-4-6"

#### Scenario: API Keys not shown in 账号 section
- **WHEN** user opens Settings
- **THEN** the "账号" section only shows "Next 账号", no API Key items

### Requirement: Dynamic LLMService routing
The system SHALL route all chat requests to the LLMService implementation matching the persisted model provider. When the provider is DEEPSEEK, requests go to DeepSeekService. When CLAUDE, requests go to ClaudeService.

#### Scenario: Chat with DeepSeek selected
- **WHEN** user sends a message with DEEPSEEK as active provider
- **THEN** the request is sent to DeepSeek API endpoint with model "deepseek-chat"

#### Scenario: Chat with Claude selected
- **WHEN** user sends a message with CLAUDE as active provider
- **THEN** the request is sent to Claude API endpoint with model "claude-opus-4-6"

#### Scenario: Provider switch takes effect immediately
- **WHEN** user switches from DEEPSEEK to CLAUDE and sends a new message
- **THEN** the new message uses ClaudeService, not DeepSeekService

### Requirement: ClaudeService implementation
The system SHALL provide a ClaudeService implementing LLMService that communicates with the Claude Messages API. It SHALL support non-streaming chat, streaming text, and streaming with tool call chunks. It SHALL use the Claude API Key from DataStore and handle Claude-specific SSE format.

#### Scenario: Non-streaming chat
- **WHEN** ClaudeService.chat() is called with a ChatRequest
- **THEN** it sends a POST to Claude Messages API and returns a ChatResponse

#### Scenario: Streaming text
- **WHEN** ClaudeService.chatStream() is called
- **THEN** it emits text deltas as a Flow<String> parsed from Claude SSE events

#### Scenario: API Key missing at request time
- **WHEN** a chat request is made but the Claude API Key is empty
- **THEN** an ApiException is thrown with a message indicating the Key is not configured

#### Scenario: Network error with retry
- **WHEN** a Claude API request fails due to network error
- **THEN** the system retries with exponential backoff (1s → 2s → 4s), max 3 attempts
