package com.cynergisuite.middleware.location.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.location.Location
import com.cynergisuite.middleware.location.LocationEntity
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
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
         rowMapper = RowMapper { rs, _ ->
            LocationEntity(
               id = rs.getLong("id"),
               number = rs.getInt("number"),
               name = rs.getString("name"),
               company = CompanyEntity(
                  id = rs.getLong("comp_id"),
                  uuRowId = rs.getUuid("comp_uu_row_id"),
                  timeCreated = rs.getOffsetDateTime("comp_time_created"),
                  timeUpdated = rs.getOffsetDateTime("comp_time_updated"),
                  name = rs.getString("comp_name"),
                  doingBusinessAs = rs.getString("comp_doing_business_as"),
                  clientCode = rs.getString("comp_client_code"),
                  clientId = rs.getInt("comp_client_id"),
                  datasetCode = rs.getString("comp_dataset_code"),
                  federalIdNumber = rs.getString("comp_federal_id_number")
               )
            )
         }
      )
   }
}
