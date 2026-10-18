package com.example.gymtracker

import android.app.Application
import com.example.gymtracker.data.dao.GymDao
import com.example.gymtracker.data.database.GymDatabaseHelper

class GymApplication : Application() {
    val database: GymDatabaseHelper by lazy { GymDatabaseHelper.getInstance(this) }
    val gymDao: GymDao by lazy { database }
}

