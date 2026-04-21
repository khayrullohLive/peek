package io.github.peek.sample

import android.app.Application
import io.github.peek.Peek

class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Peek.init(this)
    }
}
