package com.cynergisuite.middleware.authentication.user.infrastructure

import com.cynergisuite.extensions.*
import com.cynergisuite.middleware.authentication.user.SecurityEmployeeDTO
import com.cynergisuite.middleware.authentication.user.SecurityGroup
import com.cynergisuite.middleware.authentication.user.SecurityType
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeValueObject
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class SecurityGroupRepository @Inject constructor(
    private val jdbc: Jdbi,
    private val companyRepository: CompanyRepository
) {
    private val logger: Logger = LoggerFactory.getLogger(SecurityGroupRepository::class.java)

    fun selectBaseQuery(): String {
        return """
         SELECT
            secgrp.id                                                          AS secgrp_id,
            secgrp.value                                                       AS secgrp_value,
            secgrp.description                                                 AS secgrp_description,
            secgrp.company_id                                                  AS secgrp_company_id,
            empSecGrp.employee_id_sfk										             AS secgrp_emp_id,
            empSecGrp.emp_number                                               AS secgrp_emp_number
         FROM security_group secgrp
            LEFT JOIN employee_to_security_group empSecGrp ON secgrp.id = empSecGrp.security_group_id AND empSecGrp.deleted = FALSE
            JOIN company comp on secgrp.company_id = comp.id
      """
    }


    @ReadOnly
    fun findOne(id: UUID): SecurityGroup? {
        val found =
            jdbc.findFirstOrNull(
               """
                  SELECT
                     secgrp.id                                                          AS secgrp_id,
                     secgrp.value                                                       AS secgrp_value,
                     secgrp.description                                                 AS secgrp_description,
                     secgrp.company_id                                                  AS secgrp_company_id
                  FROM security_group secgrp
                  JOIN company comp on secgrp.company_id = comp.id
                  WHERE secgrp.id = :id AND secgrp.deleted = FALSE
                  """.trimIndent(),
                mapOf("id" to id)
            ) { rs, _ ->
                mapRow(
                    rs,
                    "secgrp_"
                )
            }

        logger.trace("Searching for Security Group: {} resulted in {}", id, found)

        return found
    }

   @ReadOnly
   fun findByEmployee(employeeId: Long, employeeNumber: Int, companyId: UUID): List<SecurityGroup> =
      jdbc.query("${selectBaseQuery()} WHERE comp.id = :comp_id AND empSecGrp.employee_id_sfk = :id AND secgrp.deleted = FALSE and empSecGrp.emp_number = :emp_number", mapOf("id" to employeeId, "emp_number" to employeeNumber, "comp_id" to companyId)) { rs, _ -> mapRow(rs, "secgrp_") }


   @ReadOnly
   fun findByCompany(id: UUID): List<SecurityGroup> =
      jdbc.query("""
         SELECT
            secgrp.id                                                          AS secgrp_id,
            secgrp.value                                                       AS secgrp_value,
            secgrp.description                                                 AS secgrp_description,
            secgrp.company_id                                                  AS secgrp_company_id
         FROM security_group secgrp
            JOIN company comp on secgrp.company_id = comp.id
         WHERE secgrp.company_id = :id AND secgrp.deleted = FALSE
         ORDER BY secgrp.value""",
         mapOf("id" to id)
      ) {rs, _ -> mapRow(rs, "secgrp_") }

   @Transactional
   fun insert(securityGroup: SecurityGroup): SecurityGroup {
     logger.debug("Inserting securityGroup {}", securityGroup)
     return jdbc.insertReturning(
         """
             INSERT INTO security_group(value, description, company_id)
             VALUES(:value, :description, :company_id)
             RETURNING *
         """.trimIndent(),
         mapOf(
             "value" to securityGroup.value,
             "description" to securityGroup.description,
             "company_id" to securityGroup.company.id
         )
     ) { rs, _ ->
         mapRow(rs)
     }
   }

   @Transactional
   fun createNewGroupAndTypes(securityGroupEmployee: SecurityEmployeeDTO): SecurityGroup {
      val securityGroup = SecurityGroup(securityGroupEmployee.securityGroup)
      val employees = securityGroupEmployee.employees
      try {
         val accessPoints = securityGroup.types.map { it.id }
         val group = insert(securityGroup)
         assignAccessPointsToSecurityGroups(group.id!!, accessPoints)
         assignMultipleEmployeesToSingleSecurityGroup(group.id, employees!!)
         val found = findOne(group.id)
         return found ?: throw IllegalStateException("Failed to find the inserted SecurityGroup.")
      } catch (e: Exception) {
         logger.error("Error creating security group", e)
         throw e
      }
   }

   @Transactional
   fun update(securityGroupEmployee: SecurityEmployeeDTO): SecurityGroup {
      val accessPoints = securityGroupEmployee.securityGroup.types?.map { it.id }
      val securityGroup = securityGroupEmployee.securityGroup
      assignAccessPointsToSecurityGroups(securityGroup.id!!, accessPoints!!)
      assignMultipleEmployeesToSingleSecurityGroup(securityGroup.id!!, securityGroupEmployee.employees!!)
      return jdbc.updateReturning(
         """
            UPDATE security_group
            SET
                value = :value,
                description = :description,
                company_id = :company_id
            WHERE id = :id
            RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to securityGroup.id,
            "value" to securityGroup.value,
            "description" to securityGroup.description,
            "company_id" to securityGroup.company?.id.toUuid()
         )
      ) { rs, _ ->
         mapRow(rs)
      }
   }

   @ReadOnly
   fun findByName(company: CompanyEntity, value: String): SecurityGroup? {
      val params = mutableMapOf<String, Any?>("id" to company.id, "value" to value)
      val query =
         """
            SELECT
               secgrp.id                    AS secgrp_id,
               secgrp.value                 AS secgrp_value,
               secgrp.description           AS secgrp_description,
               secgrp.company_id            AS secgrp_company_id
            FROM security_group secgrp
            WHERE secgrp.company_id = :id AND secgrp.value = :value AND secgrp.deleted = FALSE
         """.trimIndent()
      return jdbc.findFirstOrNull(query, params) { rs, _ -> mapRow(rs, "secgrp_") }
   }

   @ReadOnly
   fun findAllTypesBySecurityGroup(id: UUID): List<SecurityType> {
      val query =
         """
            SELECT
            sap.id                                                             AS secgrp_sac_id,
            sap.value                                                          AS secgrp_sac_value,
            sap.description                                                    AS secgrp_sac_description,
            sap.localization_code                                              AS secgrp_sac_localization_code,
            sap.area_id                                                        AS secgrp_sac_area_id
         FROM security_group secgrp
            JOIN security_group_to_security_access_point sgap ON secgrp.id = sgap.security_group_id
            JOIN security_access_point_type_domain sap ON sgap.security_access_point_id = sap.id
            WHERE secgrp.id = :id
         ORDER BY sap.value
         """.trimIndent()
      return jdbc.query(
         query,
         mapOf("id" to id))
      { rs, _ ->
         mapSecurityTypes(rs, "secgrp_sac_")
      }
   }

   @ReadOnly
   fun findAllSecurityAccessPointTypes(company: CompanyEntity): List<SecurityType> {
      val query =
         """
            SELECT
            sap.id                                                             AS sap_id,
            sap.value                                                          AS sap_value,
            sap.description                                                    AS sap_description,
            sap.localization_code                                              AS sap_localization_code,
            sap.area_id                                                        AS sap_area_id
         FROM security_access_point_type_domain sap
         ORDER BY sap.value
         """.trimIndent()
      return jdbc.query(
         query
      )
      { rs, _ ->
         mapSecurityTypes(rs, "sap_")
      }
   }

   @Transactional
   fun assignSingleEmployeeToMultipleSecurityGroups(employee: EmployeeEntity, securityGroupIds: List<UUID>) {
      logger.trace("Assigning Employee {} to Security Group {}", employee, securityGroupIds)
      if (securityGroupIds.isEmpty()) {
         jdbc.update(
            """
            DELETE FROM employee_to_security_group
            WHERE employee_id_sfk = :employee_id AND emp_number = :emp_number
         """.trimIndent(),
            mapOf(
               "employee_id" to employee.id,
               "emp_number" to employee.number
            )
         )
      } else {
         jdbc.update(
            """
            DELETE FROM employee_to_security_group
            WHERE employee_id_sfk = :employee_id AND emp_number = :employee_number
            AND security_group_id NOT IN (<security_group_ids>)
         """.trimIndent(),
            mapOf(
               "employee_id" to employee.id,
               "employee_number" to employee.number,
               "security_group_ids" to securityGroupIds
            )
         )
         jdbc.update(
            """
            INSERT INTO employee_to_security_group (employee_id_sfk, security_group_id, emp_number)
            SELECT :employee_id, security_group_id, :employee_number
            FROM unnest(:security_group_ids) AS security_group_id
            ON CONFLICT (security_group_id, employee_id_sfk, emp_number) DO NOTHING
            RETURNING
            *
         """.trimIndent(),
            mapOf(
               "employee_id" to employee.id,
               "employee_number" to employee.number,
               "security_group_ids" to securityGroupIds.toTypedArray()
            )
         )
      }
   }

   @Transactional
   fun assignMultipleEmployeesToSingleSecurityGroup(securityGroupId: UUID, employees: List<EmployeeValueObject>?) {
      logger.trace("Assigning Employees {} to Security Group {}", employees, securityGroupId)

      val employeeNumberIdPair: List<Pair<Long, Int>>? = employees?.map { Pair(it.id!!, it.number!!) }
      if (employees.isNullOrEmpty()) {
            jdbc.update(
               """
               DELETE FROM employee_to_security_group
               WHERE security_group_id = :security_group_id

            """.trimIndent(),
               mapOf(
                  "security_group_id" to securityGroupId
               )
            )
         } else {
         if (!employeeNumberIdPair.isNullOrEmpty()) {
            val subQuery = employeeNumberIdPair.joinToString(" UNION ") {
               "SELECT :id${it.first} AS employee_id, :number${it.second} AS emp_number"
            }
            val params = employeeNumberIdPair.flatMap { (id, number) ->
               listOf("id${id}" to id, "number${number}" to number)
            }.toMap()

            jdbc.update(
               """
               WITH pairs(employee_id, emp_number) AS (
                   $subQuery
               )
               DELETE FROM employee_to_security_group etsg
               WHERE etsg.security_group_id = :security_group_id
               AND NOT EXISTS (
                   SELECT 1 FROM pairs p
                   WHERE p.employee_id = etsg.employee_id_sfk
                   AND p.emp_number = etsg.emp_number
               )
               """.trimIndent(),
               params + mapOf("security_group_id" to securityGroupId)
            )

            employees.forEach { employee ->
               val employeeParams = mapOf(
                  "employee_id" to employee.id,
                  "security_group_id" to securityGroupId,
                  "employee_number" to employee.number
               )
               jdbc.update(
                  """
               INSERT INTO employee_to_security_group (employee_id_sfk, security_group_id, emp_number)
               VALUES (:employee_id, :security_group_id, :employee_number)
               ON CONFLICT (security_group_id, employee_id_sfk, emp_number) DO NOTHING
               RETURNING
               *
            """.trimIndent(),
                  employeeParams
               )
            }
         }
      }
   }


   @Transactional
   fun assignAccessPointsToSecurityGroups(securityGroupId: UUID, accessPointIds: List<Int>) {
      logger.trace("Assigning Access points to Security Groups {}",  securityGroupId)
      if (accessPointIds.isEmpty()) {
         jdbc.update(
            """
            DELETE FROM security_group_to_security_access_point
            WHERE security_group_id = :security_id
         """.trimIndent(),
            mapOf(
               "security_id" to securityGroupId,
            )
         )
      } else {
         jdbc.update(
            """
            DELETE FROM security_group_to_security_access_point
            WHERE security_group_id = :security_id
            AND security_access_point_id NOT IN (<access_point_ids>)
         """.trimIndent(),
            mapOf(
               "security_id" to securityGroupId,
               "access_point_ids" to accessPointIds
            )
         )

         jdbc.update(
            """
            INSERT INTO security_group_to_security_access_point (security_group_id, security_access_point_id)
            SELECT :security_id, access_point_id
            FROM unnest(:access_point_ids) AS access_point_id
            ON CONFLICT (security_group_id, security_access_point_id) DO NOTHING
         """.trimIndent(),
            mapOf(
               "security_id" to securityGroupId,
               "access_point_ids" to accessPointIds.toTypedArray()
            )
         )
      }
   }

   fun mapRow(
     rs: ResultSet,
     columnPrefix: String = EMPTY
   ): SecurityGroup {
     val company = companyRepository.findOne(rs.getUuid("${columnPrefix}company_id"))
     return SecurityGroup(
         id = rs.getUuid("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         types = findAllTypesBySecurityGroup(rs.getUuid("${columnPrefix}id")),
         company = company!!
     )
   }

   fun mapSecurityTypes(rs: ResultSet, columnPrefix: String): SecurityType {
        return SecurityType(
           id = rs.getInt("${columnPrefix}id"),
           value = rs.getString("${columnPrefix}value"),
           description = rs.getString("${columnPrefix}description"),
           localizationCode = rs.getString("${columnPrefix}localization_code"),
           areaId = rs.getInt("${columnPrefix}area_id")
        )
   }
}
