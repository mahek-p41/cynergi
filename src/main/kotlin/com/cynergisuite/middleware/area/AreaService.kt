package com.cynergisuite.middleware.area

import com.cynergisuite.middleware.area.infrastructure.AreaRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AreaService @Inject constructor(
   private val areaRepository: AreaRepository,
) {

   fun isDarwillEnabledFor(company: CompanyEntity): Boolean =
      areaRepository.existsByCompanyAndAreaType(company, DarwillUpload.toAreaTypeEntity())
}
