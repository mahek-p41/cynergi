package com.cynergisuite.middleware.location.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.location.Location
import com.cynergisuite.middleware.location.LocationEntity
import org.apache.commons.lang3.StringUtils
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
   private val companyRepository: CompanyRepository,
   private val jdbc: NamedParameterJdbcTemplate
) {

   fun findOne(locationNumber: Int, company: Company): Location? {
      return jdbc.findFirstOrNull("""
         SELECT
            location.id AS id,
            location.number AS number,
            location.name AS name,
            comp.id AS comp_id,
            comp.uu_row_id AS comp_uu_row_id,
            comp.time_created AS comp_time_created,
            comp.time_updated AS comp_time_updated,
            comp.name AS comp_name,
            comp.doing_business_as AS comp_doing_business_as,
            comp.client_code AS comp_client_code,
            comp.client_id AS comp_client_id,
            comp.dataset_code AS comp_dataset_code,
            comp.federal_id_number AS comp_federal_id_number
         FROM fastinfo_prod_import.store_vw location
              JOIN company comp ON comp.dataset_code = location.dataset
         WHERE number = :location_number
               AND comp.id = :comp_id
         """.trimIndent(),
         params = mapOf(
            "location_number" to locationNumber,
            "comp_id" to company.myId()
         ),
         rowMapper = RowMapper { rs, _ ->  mapRow(rs, companyRepository.mapRow(rs, "comp_")) }
      )
   }

   fun maybeMapRow(rs: ResultSet, company: CompanyEntity, columnPrefix: String = StringUtils.EMPTY) =
      if (rs.getString("${columnPrefix}id") != null) {
         mapRow(rs, company, columnPrefix)
      } else {
         null
      }

   private fun mapRow(rs: ResultSet, company: Company, columnPrefix: String = StringUtils.EMPTY) =
      LocationEntity(
         id = rs.getLong("${columnPrefix}id"),
         number = rs.getInt("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         company = company
      )

}
