package com.ehDev.imHere.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ehDev.imHere.db.dao.AccountDao
import com.ehDev.imHere.db.entity.AccountEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [
        AccountEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class UrfuRoomDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao

    companion object {

        @Volatile
        private var INSTANCE: UrfuRoomDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): UrfuRoomDatabase {

            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {

                val instance = buildDBInstance(context = context.applicationContext, scope = scope)

                INSTANCE = instance
                return instance
            }
        }

        private fun buildDBInstance(context: Context, scope: CoroutineScope) = Room.databaseBuilder(
            context,
            UrfuRoomDatabase::class.java,
            "urfu_database"
        ).addCallback(AccountDatabaseCallback(scope = scope))
            .build()
    }

    private class AccountDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {

        private val fakeAccount = AccountEntity("000", "000".hashCode(), "1", "000")

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            INSTANCE?.let { database ->
                scope.launch {
                    database.accountDao().insert(fakeAccount)
                }
            }
        }
    }
}