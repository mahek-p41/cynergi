package com.cynergisuite.middleware.area.infrastructure

import com.cynergisuite.middleware.area.AreaEntity
import com.cynergisuite.middleware.area.AreaTypeEntity
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.data.annotation.Join
import io.micronaut.data.annotation.repeatable.JoinSpecifications
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.repository.CrudRepository
import java.util.UUID

@JdbcRepository
interface AreaRepository : CrudRepository<AreaEntity, UUID> {

   @JoinSpecifications(
      Join("areaType")
   )
   fun existsByCompanyAndAreaType(company: CompanyEntity, areaType: AreaTypeEntity): Boolean

   @JoinSpecifications(
      Join("areaType"),
      Join("company")
   )
   fun findByCompanyAndAreaType(company: CompanyEntity, areaType: AreaTypeEntity): AreaEntity?
}
