package com.cynergisuite.middleware.area

import com.cynergisuite.middleware.area.infrastructure.AreaRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AreaService @Inject constructor(
   private val areaRepository: AreaRepository,
) {

   fun isEnabledFor(company: CompanyEntity, areaType: AreaType): Boolean =
      areaRepository.existsByCompanyAndAreaType(company, areaType.toAreaTypeEntity())
}
