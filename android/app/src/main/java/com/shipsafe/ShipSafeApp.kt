package com.shipsafe

import android.app.Application

class ShipSafeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: ShipSafeApp
            private set
    }
}
