package com.ehDev.imHere.repository

import com.ehDev.imHere.db.dao.InterviewDao
import com.ehDev.imHere.db.entity.InterviewEntity

class InterviewRepository(
    private val interviewDao: InterviewDao
) {

    suspend fun insertInterview(interview: InterviewEntity) = interviewDao.insert(interview)

    suspend fun getAllInterviews(): List<InterviewEntity> = interviewDao.getAllInterviews()
}