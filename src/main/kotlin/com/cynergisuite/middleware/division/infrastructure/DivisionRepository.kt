package com.cynergisuite.middleware.division.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.DatasetRequiringRepository
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.division.DivisionEntity
import com.cynergisuite.middleware.employee.Employee
import com.cynergisuite.middleware.employee.EmployeeEntity
import io.micronaut.spring.tx.annotation.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DivisionRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(DivisionRepository::class.java)

   @Transactional
   fun insert(division: DivisionEntity, company: Company): DivisionEntity {
      logger.debug("Inserting division {}", division)

      return jdbc.insertReturning("""
         INSERT INTO division(company_id, number, name, manager_number, description)
         VALUES (:company_id, :number, :name, :manager_number, :description)
         RETURNING
            *
         """,
         mapOf(
            "company_id" to company.myId(),
            "number" to division.number,
            "name" to division.name,
            "manager_number" to division.manager?.myNumber(),
            "description" to division.description
         ),
         RowMapper { rs, _ ->
            DivisionEntity(
               id = rs.getLong("id"),
               uuRowId = rs.getUuid("uu_row_id"),
               timeCreated = rs.getOffsetDateTime("time_created"),
               timeUpdated = rs.getOffsetDateTime("time_updated"),
               company = company,
               number = rs.getInt("number"),
               name = rs.getString("name"),
               manager = division.manager,
               description = rs.getString("description")
            )
         }
      )
   }
}
