package com.ehDev.imHere.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ehDev.imHere.db.entity.ScheduleEntity

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedule_table WHERE date = :date")
    suspend fun getAccountByDate(date: String): ScheduleEntity

    @Insert
    suspend fun insert(account: ScheduleEntity)
}