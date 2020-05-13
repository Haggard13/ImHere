package com.ehDev.imHere.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ehDev.imHere.db.dao.AccountDao

@Database(
    entities = [
        AccountDao::class
    ],
    version = 1,
    exportSchema = false
)
abstract class UrfuRoomDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
}