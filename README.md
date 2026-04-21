# Peek 🔍

**Peek** is a lightweight in-app network inspector for Android, inspired by Flutter's [Alice](https://pub.dev/packages/alice).

Capture every OkHttp request automatically, browse logs in a floating overlay, inspect full request/response details, and export everything as a shareable JSON file — works in **all build types** (debug, release, staging).

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
| 🌍 All build types | Works in debug, release, staging — everywhere |

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

### Step 2 — Add dependency

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.github.khayrullohLive:peek:v2.0.0")
}
```

---

## Setup

### Step 1 — Initialize in Application

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Peek.init(this)
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
    .addInterceptor(Peek.interceptor())
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

```
┌─────────────┐
│  🔍 12  ⏸  │
└─────────────┘
```

| Button | Action |
|---|---|
| 🔍 + count | Opens the network inspector screen |
| ⏸ | Logging is active — tap to pause |
| ▶ | Logging is paused — tap to resume |

The bubble is **draggable** — drag it anywhere on the screen.

---

## Inspector screen

Tap the 🔍 bubble to open the inspector.

### List view

Each row shows:
- Colored bar (🟢 success / 🟡 pending / 🔴 error)
- HTTP method (GET = blue, POST = green, PUT = orange, DELETE = red)
- Status code and duration
- Request path and host
- Timestamp

**Top-right buttons:**
- 📤 **Share** — exports all logs as a JSON file
- 🗑 **Clear** — deletes all captured logs

### Detail view

Tap any row to see full details:
endpoint, date/time, method, status, duration, request headers, request body, response headers, response body, error.

---

## Manual control

```kotlin
Peek.start()            // start capturing
Peek.pause()            // pause — requests pass through, not logged
Peek.toggle()           // toggle start/pause
suspend Peek.clear()    // delete all logs
```

---

## Export logs

### Via UI

Open the inspector → tap **📤 Share** in the top-right corner.

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
    "requestHeaders": "Authorization: Bearer xxx",
    "requestBody": null,
    "responseHeaders": "Content-Type: application/json",
    "responseBody": "{\"users\": [...]}",
    "error": null
  }
]
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

MIT License — Copyright (c) 2026 khayrullohLive
