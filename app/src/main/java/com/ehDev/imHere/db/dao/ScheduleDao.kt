package com.ehDev.imHere.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ehDev.imHere.data.VisitState
import com.ehDev.imHere.db.entity.ScheduleEntity
import java.util.*

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedule_table")
    suspend fun getSchedule(): List<ScheduleEntity>

    //изменяет анвизитед на визитед
    @Query("UPDATE schedule_table SET visit = :visited  WHERE date = :date")
    suspend fun changeState(date: String, visited: String = VisitState.VISITED.name)

    @Insert
    suspend fun insert(pair: ScheduleEntity)
}