package com.ehDev.imHere.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ehDev.imHere.db.entity.InterviewEntity

@Dao
interface InterviewDao {

    @Query("SELECT * FROM interview_table")
    suspend fun getAllInterviews(): List<InterviewEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(interview: InterviewEntity)
}