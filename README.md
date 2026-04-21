# Peek 🔍

**Peek** is a lightweight in-app network inspector for Android, inspired by Flutter's [Alice](https://pub.dev/packages/alice).

Capture every OkHttp request automatically, browse logs in a floating overlay, inspect full request/response details, and export everything as a shareable JSON file — all without leaving your app.

> **Debug builds only.** Peek is completely removed from release builds via the no-op variant — zero overhead in production.

---

## Features

| Feature | Description |
|---|---|
| 🔌 Auto-capture | One-line OkHttp setup — every request is logged automatically |
| 🫧 Floating bubble | Draggable bubble sits on top of every screen |
| ▶ / ⏸ Start / Pause | Control logging without restarting the app |
| 📋 Request list | Color-coded by HTTP method and status code |
| 🔍 Detail view | Full headers, body, response, duration, error |
| 📤 Share as file | Export all logs as JSON via Android share sheet |
| 💾 Persistent | Stored in Room DB — survives app restarts |
| 🔐 No-op release | 0 KB overhead in production builds |

---

## Installation

### Step 1 — Add JitPack repository

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }  // ← add this
    }
}
```

### Step 2 — Add dependencies

```kotlin
// app/build.gradle.kts
dependencies {
    debugImplementation("com.github.khayrullohLive:peek:v1.1.0")
    releaseImplementation("com.github.khayrullohLive:peek-noop:v1.1.0")
}
```

> `debugImplementation` — real library with UI (debug builds only)
> `releaseImplementation` — empty stubs, no code, no UI (release builds)

---

## Setup

### Step 1 — Initialize in Application

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Peek.init(this)  // ← one line
    }
}
```

Make sure your `Application` class is registered in `AndroidManifest.xml`:

```xml
<application
    android:name=".App"
    ... >
```

---

### Step 2 — Add interceptor to OkHttp

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(Peek.interceptor())  // ← add this
    .build()
```

With Hilt / Retrofit:

```kotlin
@Provides
@Singleton
fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(Peek.interceptor())
        .build()
}
```

---

### Step 3 — Show the floating bubble

Add `PeekBubble()` inside your root `Box` in `MainActivity`:

```kotlin
import io.github.peek.overlay.PeekBubble

setContent {
    AppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            AppNavHost()   // ← your navigation
            PeekBubble()   // ← sits on top of everything
        }
    }
}
```

**That's it.** Every HTTP request is now captured automatically.

---

## Bubble

The floating bubble has two buttons:

```
┌─────────────┐
│  🔍 12  ⏸  │
└─────────────┘
```

| Button | Action |
|---|---|
| 🔍 + count | Opens the network inspector screen |
| ▶ | Logging is paused — tap to resume |
| ⏸ | Logging is active — tap to pause |

The bubble is **draggable** — long-press and drag it anywhere on the screen.

---

## Inspector screen

Tap the 🔍 bubble to open the inspector.

### List view

Each row shows:
- Colored bar (🟢 success / 🟡 pending / 🔴 error)
- HTTP method with color (GET = blue, POST = green, PUT = orange, DELETE = red)
- Status code
- Duration in ms
- Request path
- Host
- Timestamp

**Top-right buttons:**
- 📤 **Share** — exports all logs as a JSON file
- 🗑 **Clear** — deletes all captured logs

### Detail view

Tap any row to see the full details:

- Endpoint (full URL)
- Date & time
- HTTP method
- Status code
- Duration
- Request headers
- Request body
- Response headers
- Response body
- Error message (if failed)

---

## Manual control

You can control logging programmatically:

```kotlin
Peek.start()   // start capturing requests
Peek.pause()   // pause — requests still pass through, not logged
Peek.toggle()  // toggle between start/pause
Peek.clear()   // delete all captured logs (suspend function)
```

---

## Export logs

### Via UI

Open the inspector → tap the **📤 Share** button in the top-right corner.

### Programmatically

```kotlin
lifecycleScope.launch {
    val intent = Peek.buildShareIntent()
    startActivity(Intent.createChooser(intent, "Share Peek logs"))
}
```

### Export format (JSON)

```json
[
  {
    "endpoint": "https://api.example.com/v1/users",
    "date": "2026-04-21T10:30:45.123Z",
    "method": "GET",
    "host": "api.example.com",
    "path": "/v1/users",
    "statusCode": 200,
    "durationMs": 156,
    "requestHeaders": "Authorization: Bearer xxx\nContent-Type: application/json",
    "requestBody": null,
    "responseHeaders": "Content-Type: application/json",
    "responseBody": "{\"users\": [...]}",
    "error": null
  }
]
```

---

## Data model

`PeekLog` entity — only `endpoint` and `date` are required, everything else is nullable:

```kotlin
data class PeekLog(
    val id: Long = 0,

    // Required
    val endpoint: String,   // full URL
    val date: Long,         // epoch millis

    // Nullable — populated best-effort
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

---

## Full integration example

```kotlin
// App.kt
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Peek.init(this)
    }
}

// NetworkModule.kt (Hilt)
@Provides
@Singleton
fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
    .addInterceptor(Peek.interceptor())
    .connectTimeout(30, TimeUnit.SECONDS)
    .build()

// MainActivity.kt
setContent {
    AppTheme {
        Box(Modifier.fillMaxSize()) {
            AppNavHost(navController)
            PeekBubble()
        }
    }
}
```

---

## Requirements

| | Minimum |
|---|---|
| Android | API 24 (Android 7.0) |
| Kotlin | 1.9+ |
| Compose BOM | 2024.01.00+ |
| OkHttp | 4.x |

---

## License

```
MIT License

Copyright (c) 2026 khayrullohLive

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included
in all copies or substantial portions of the Software.
```
