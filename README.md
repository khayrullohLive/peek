# Peek 🔍

Lightweight in-app network inspector for Android — inspired by Flutter's Alice.

Capture every OkHttp request, browse them in a floating overlay, and export
the full log as a shareable JSON file.

![status](https://img.shields.io/badge/status-alpha-orange)
![license](https://img.shields.io/badge/license-MIT-blue)
![min%20sdk](https://img.shields.io/badge/min%20sdk-24-green)

## Features

- 📥 **Auto-capture** — one line in your OkHttpClient, every request is logged
- 🫧 **Floating bubble** — draggable, sits on top of every screen, opens the inspector
- 📋 **List + detail** — color-coded by status & method, full headers and body
- 📤 **Share as file** — export all logs as JSON via the Android share sheet
- 💾 **Persistent** — stored in Room, survives app restarts
- 🔐 **No-op release variant** — zero overhead in production builds

## Install

### Settings

```kotlin
// settings.gradle.kts
include(":peek")
include(":peek-noop")
```

### App module

```kotlin
// app/build.gradle.kts
dependencies {
    debugImplementation(project(":peek"))
    releaseImplementation(project(":peek-noop"))
}
```

Or once published to JitPack / Maven Central:

```kotlin
dependencies {
    debugImplementation("io.github.peek:peek:1.0.0")
    releaseImplementation("io.github.peek:peek-noop:1.0.0")
}
```

## Use

### 1. Initialize

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Peek.init(this)
    }
}
```

### 2. Add the interceptor

```kotlin
OkHttpClient.Builder()
    .addInterceptor(Peek.interceptor())
    .build()
```

### 3. Show the bubble (optional)

```kotlin
setContent {
    Box(Modifier.fillMaxSize()) {
        YourNavHost()
        PeekBubble()
    }
}
```

### 4. Export logs as file

The inspector screen has a share button in the top-right that exports all
captured logs as a JSON file via the Android share sheet (email, Telegram,
Drive, Slack…).

You can also trigger it programmatically:

```kotlin
lifecycleScope.launch {
    val intent = Peek.buildShareIntent()
    startActivity(Intent.createChooser(intent, "Share logs"))
}
```

## Entity

Only two fields are required — everything else is nullable and populated
best-effort by the interceptor:

```kotlin
data class PeekLog(
    val id: Long = 0,

    // required
    val endpoint: String,
    val date: Long,

    // optional
    val method: String?          = null,
    val host: String?            = null,
    val path: String?            = null,
    val durationMs: Long?        = null,
    val statusCode: Int?         = null,
    val requestHeaders: String?  = null,
    val requestBody: String?     = null,
    val responseHeaders: String? = null,
    val responseBody: String?    = null,
    val error: String?           = null,
)
```

## Project structure

```
peek/
├── peek/           ← main library (debug)
├── peek-noop/      ← no-op stubs (release, 0 KB overhead)
└── sample/         ← demo app showing integration
```

## License

MIT — see [LICENSE](LICENSE).
