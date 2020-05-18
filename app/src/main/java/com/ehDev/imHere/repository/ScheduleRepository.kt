package com.ehDev.imHere.repository

import com.ehDev.imHere.data.VisitState
import com.ehDev.imHere.db.dao.ScheduleDao
import com.ehDev.imHere.db.entity.ScheduleEntity
import java.util.*

class ScheduleRepository(
        private val scheduleDao: ScheduleDao
) {
    suspend fun getSchedule(): List<ScheduleEntity> = scheduleDao.getSchedule()

    suspend fun changeState(date: String) = scheduleDao.changeState(date)
}