package com.ehDev.imHere.repository

import com.ehDev.imHere.db.dao.InstitutionDao
import com.ehDev.imHere.db.entity.InstitutionEntity

class InstitutionRepository(
        private val institutionDao: InstitutionDao
) {
    suspend fun getCoordinates(prefix: String) : List<InstitutionEntity> = institutionDao.getCoordinates(prefix)
}