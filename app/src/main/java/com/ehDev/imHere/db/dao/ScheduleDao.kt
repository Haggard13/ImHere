package com.ehDev.imHere.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ehDev.imHere.db.entity.ScheduleEntity
import java.util.*

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedule_table")
    suspend fun getSchedule(): List<ScheduleEntity>

    @Insert
    suspend fun insert(pair: ScheduleEntity)
}