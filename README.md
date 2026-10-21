# SiftAI Android starter

A starter Android app for a personal AI assistant, with a dark SiftAI interface, chat, image attachment support for compatible vision models, basic text-file reading, and an optional DuckDuckGo Instant Answer search context toggle.

## Build an APK on GitHub (no Android Studio required on your phone)

1. Download and unzip this project.
2. Create a new repository on GitHub.
3. Upload the **contents** of this ZIP to the repository root (so `app/`, `build.gradle.kts`, and `.github/` are at the top level).
4. Open **Actions** → **Build SiftAI APK** → **Run workflow**. If Actions are disabled, enable them in the repository settings.
5. When it finishes, open the workflow run, scroll to **Artifacts**, and download `SiftAI-debug-APK`.
6. Extract the artifact ZIP; install `app-debug.apk` on your Android phone. Android may ask you to allow installs from that source.

A push to `main` also triggers a build.

## Activate AI chat

The app is a client and does not include an AI model. On first launch, tap ⚙ Settings and enter:
- API endpoint, e.g. an OpenAI-compatible `/v1/chat/completions` endpoint
- Model ID supported by that provider
- API key from that provider

The default values are examples. You need a provider that supports the chosen model and API format. Provider usage limits and charges still apply. There is no genuinely unlimited hosted AI service; app project count is limited by your device/storage/provider policy.

## Current starter limitations

- Image understanding works only when the selected model/provider accepts OpenAI-style image URL content.
- Text/Markdown/JSON/CSV attachments are included as text (up to 20,000 characters).
- PDF, DOCX, spreadsheets, audio, and video extraction are not implemented in this starter.
- Search uses DuckDuckGo Instant Answer snippets, not a full search-engine results API; results can be empty or incomplete.
- Conversations are kept in memory during the current app session only.
- The API key is stored in app preferences on this device. This is acceptable for a personal prototype, but **not secure enough for a public app**. For a public release, put provider credentials behind your own backend and add authentication, rate limits, and privacy controls.
- This project is a starter, not a complete replacement for ChatGPT. Features such as image generation, voice, account sync, advanced web browsing, and project-file workspaces need additional integrations.

## App identity

- App name: SiftAI
- Package ID: `com.siftai.app`
- Version: `1.0`
