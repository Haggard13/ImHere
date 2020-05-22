package com.ehDev.imHere.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ehDev.imHere.db.entity.InstitutionEntity

@Dao
interface InstitutionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(institution: InstitutionEntity)

    @Query("SELECT * FROM institution_table WHERE institution = :prefix")
    suspend fun getCoordinates(prefix: String) : List<InstitutionEntity>
}