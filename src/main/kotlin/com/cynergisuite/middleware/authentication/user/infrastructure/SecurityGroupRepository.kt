package com.cynergisuite.middleware.authentication.user.infrastructure

import com.cynergisuite.extensions.*
import com.cynergisuite.middleware.authentication.user.SecurityGroup
import com.cynergisuite.middleware.authentication.user.SecurityType
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.employee.EmployeeEntity
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
    private val companyRepository: CompanyRepository,
) {
    private val logger: Logger = LoggerFactory.getLogger(SecurityGroupRepository::class.java)

    fun selectBaseQuery(): String {
        return """
         SELECT
            secgrp.id                                                          AS secgrp_id,
            secgrp.value                                                       AS secgrp_value,
            secgrp.description                                                 AS secgrp_description,
            secgrp.company_id                                                  AS secgrp_company_id,
            empSecGrp.employee_id_sfk										             AS secgrp_emp_id
         FROM security_group secgrp
            JOIN employee_to_security_group empSecGrp ON secgrp.id = empSecGrp.security_group_id AND empSecGrp.deleted = FALSE
            JOIN company comp on secgrp.company_id = comp.id
      """
    }


    @ReadOnly
    fun findOne(id: Long): SecurityGroup? {
        val found =
            jdbc.findFirstOrNull(
                "${selectBaseQuery()} WHERE empSecGrp.employee_id_sfk = :id AND secgrp.deleted = FALSE",
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
   fun findAll(employeeId: Long, companyId: UUID): List<SecurityGroup> =
      jdbc.query("${selectBaseQuery()} WHERE comp.id = :comp_id AND empSecGrp.employee_id_sfk = :id AND secgrp.deleted = FALSE", mapOf("id" to employeeId, "comp_id" to companyId)) { rs, _ -> mapRow(rs, "secgrp_") }


   @ReadOnly
   fun findByCompany(id: UUID): List<SecurityGroup> =
      jdbc.query("${selectBaseQuery()} WHERE secgrp.company_id = :id AND secgrp.deleted = FALSE", mapOf("id" to id)) {rs, _ -> mapRow(rs, "secgrp_") }

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
   fun update(securityGroup: SecurityGroup): SecurityGroup {

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
            "company_id" to securityGroup.company.id
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
            JOIN employee_to_security_group empSecGrp ON secgrp.id = empSecGrp.security_group_id AND empSecGrp.deleted = FALSE
            JOIN security_group_to_security_access_point sgap ON secgrp.id = sgap.security_group_id
            JOIN security_access_point_type_domain sap ON sgap.security_access_point_id = sap.id
            WHERE secgrp.id = :id
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
         """.trimIndent()
      return jdbc.query(
         query
      )
      { rs, _ ->
         mapSecurityTypes(rs, "sap_")
      }
   }

      @Transactional
   fun assignEmployeeToSecurityGroup(employee: EmployeeEntity, securityGroupId: UUID) {
      logger.trace("Assigning Employee {} to Security Group {}", employee, securityGroupId)
      jdbc.update(
         """
            INSERT INTO employee_to_security_group (employee_id_sfk, security_group_id, deleted, emp_number)
            VALUES(:employee_id, :security_group_id, false, :employee_number)
         """.trimIndent(),
         mapOf(
             "employee_id" to employee.id,
             "security_group_id" to securityGroupId,
             "employee_number" to employee.number
         )
      )
   }

   @Transactional
   fun assignAccessPointsToSecurityGroups(securityGroupId: UUID, accessPointIds: List<Int>){
      logger.trace("Assigning Access points to Security Groups {}",  securityGroupId)
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
