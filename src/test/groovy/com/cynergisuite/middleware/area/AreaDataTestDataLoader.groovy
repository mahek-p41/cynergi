package com.cynergisuite.middleware.area

import com.cynergisuite.middleware.area.infrastructure.AreaRepository
import com.cynergisuite.middleware.company.CompanyEntity
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AreaDataTestDataLoaderService {
   @Inject AreaRepository areaRepository

   AreaEntity enableArea(AreaType areaType, CompanyEntity company) {
      return areaRepository.save(new AreaEntity(null, new AreaTypeEntity(areaType), company))
   }
}
