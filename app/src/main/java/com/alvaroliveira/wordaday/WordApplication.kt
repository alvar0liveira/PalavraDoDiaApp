package com.alvaroliveira.wordaday

import android.app.Application
import com.alvaroliveira.wordaday.di.mainModule
import org.koin.core.context.startKoin

class WordApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(mainModule)
        }
    }
}