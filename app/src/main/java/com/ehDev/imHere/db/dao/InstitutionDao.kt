package com.ehDev.imHere.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ehDev.imHere.db.entity.InstitutionEntity

@Dao
interface InstitutionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(institution: InstitutionEntity)
}