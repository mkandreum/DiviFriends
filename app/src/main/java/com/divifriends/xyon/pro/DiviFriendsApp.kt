package com.divifriends.xyon.pro

import android.app.Application
import com.divifriends.xyon.pro.data.AppRepository
import com.divifriends.xyon.pro.data.createAppDataStore

class DiviFriendsApp : Application() {

    val repository: AppRepository by lazy { AppRepository(createAppDataStore()) }

    override fun onCreate() {
        super.onCreate()
        repository.load()
    }
}
