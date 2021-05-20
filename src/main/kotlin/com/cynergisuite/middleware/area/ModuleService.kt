package com.cynergisuite.middleware.area

import com.cynergisuite.middleware.area.infrastructure.ModuleRepository
import com.cynergisuite.middleware.company.CompanyEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModuleService @Inject constructor(
   private val repository: ModuleRepository,
   private val validator: ModuleValidator
) {

   fun createLevelConfig(company: CompanyEntity, moduleDTO: ModuleDTO): ModuleType {
      val toInsert = validator.validateCreate(company, moduleDTO)
      return repository.insertConfig(toInsert, company)
   }

   fun updateLevelConfig(company: CompanyEntity, moduleDTO: ModuleDTO): ModuleType {
      val toUpdate = validator.validateUpdate(company, moduleDTO)
      return repository.updateConfig(toUpdate, company)
   }
}
