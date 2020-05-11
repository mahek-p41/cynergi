package com.cynergisuite.middleware.audit.action.infrastructure

import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.action.AuditActionEntity
import com.cynergisuite.middleware.audit.status.infrastructure.AuditStatusRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.department.DepartmentEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.store.SimpleStore
import com.cynergisuite.middleware.store.Store
import io.micronaut.spring.tx.annotation.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditActionRepository @Inject constructor(
   private val auditStatusRepository: AuditStatusRepository,
   private val employeeRepository: EmployeeRepository,
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditActionRepository::class.java)

   fun findAll(parent: AuditEntity): List<AuditActionEntity> {
      return jdbc.query("""
         WITH employees AS (
            ${employeeRepository.employeeBaseQuery()}
         )
         SELECT
         auditActions.id                                     AS auditAction_id,
         auditActions.time_updated                           AS auditAction_time_created,
         auditActions.time_updated                           AS auditAction_time_updated,
         auditActionEmployee.emp_id                          AS auditActionEmployee_id,
         auditActionEmployee.emp_number                      AS auditActionEmployee_number,
         auditActionEmployee.emp_last_name                   AS auditActionEmployee_last_name,
         auditActionEmployee.emp_first_name_mi               AS auditActionEmployee_first_name_mi,
         auditActionEmployee.emp_pass_code                   AS auditActionEmployee_pass_code,
         auditActionEmployee.emp_active                      AS auditActionEmployee_active,
         auditActionEmployee.emp_type                        AS auditActionEmployee_type,
         auditActionEmployee.emp_cynergi_system_admin        AS auditActionEmployee_cynergi_system_admin,
         auditActionEmployee.emp_alternative_store_indicator AS auditActionEmployee_alternative_store_indicator,
         auditActionEmployee.emp_alternative_area            AS auditActionEmployee_alternative_area,
         auditActionEmployee.dept_id                         AS auditActionEmployeeDept_id,
         auditActionEmployee.dept_code                       AS auditActionEmployeeDept_code,
         auditActionEmployee.dept_description                AS auditActionEmployeeDept_description,
         auditActionEmployee.dept_security_profile           AS auditActionEmployeeDept_security_profile,
         auditActionEmployee.dept_default_menu               AS auditActionEmployeeDept_default_menu,
         auditActionEmployee.store_id                        AS auditActionEmployee_store_id,
         auditActionEmployee.store_number                    AS auditActionEmployee_store_number,
         auditActionEmployee.store_name                      AS auditActionEmployee_store_name,
         astd.id                                             AS astd_id,
         astd.value                                          AS astd_value,
         astd.description                                    AS astd_description,
         astd.color                                          AS astd_color,
         astd.localization_code                              AS astd_localization_code,
         comp.id                                             AS comp_id,
         comp.uu_row_id                                      AS comp_uu_row_id,
         comp.time_created                                   AS comp_time_created,
         comp.time_updated                                   AS comp_time_updated,
         comp.name                                           AS comp_name,
         comp.doing_business_as                              AS comp_doing_business_as,
         comp.client_code                                    AS comp_client_code,
         comp.client_id                                      AS comp_client_id,
         comp.dataset_code                                   AS comp_dataset_code,
         comp.federal_id_number                              AS comp_federal_id_number
      FROM audit_action auditActions
           JOIN audit audits ON auditActions.audit_id = audits.id
           JOIN company comp ON audits.company_id = comp.id
           JOIN audit_status_type_domain astd ON auditActions.status_id = astd.id
           JOIN employees auditActionEmployee ON comp.dataset_code = auditActionEmployee.comp_dataset_code AND auditActions.changed_by = auditActionEmployee.emp_number
      WHERE auditActions.audit_id = :auditAction_id
      """.trimIndent(),
      mapOf("auditAction_id" to parent.id),
      RowMapper { rs, _ ->
         mapAuditAction(rs)
      })
   }

   @Transactional
   fun insert(parent: AuditEntity, entity: AuditActionEntity): AuditActionEntity {
      logger.debug("Inserting audit_action {}", entity)

      return jdbc.insertReturning("""
         INSERT INTO audit_action(changed_by, status_id, audit_id)
         VALUES (:changed_by, :status_id, :audit_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "changed_by" to entity.changedBy.number,
            "status_id" to entity.status.id,
            "audit_id" to parent.id
         ),
         RowMapper { rs, _ ->
            AuditActionEntity(
               rs.getLong("id"),
               rs.getOffsetDateTime("time_created"),
               rs.getOffsetDateTime("time_updated"),
               entity.status,
               changedBy = entity.changedBy
            )
         }
      )
   }

   fun upsert(parent: AuditEntity, entity: AuditActionEntity): AuditActionEntity {
      logger.debug("Upserting AuditAction {} {}", entity, parent)

      return if (entity.id != null) {
         logger.trace("Not necessary to insert {}", entity)

         entity
      } else {
         logger.trace("Inserting {}", entity)

         insert(parent, entity)
      }
   }

   private fun mapRow(rs: ResultSet, rowPrefix: String = "aa_"): AuditActionEntity =
      AuditActionEntity(
         rs.getLong("${rowPrefix}id"),
         rs.getOffsetDateTime("${rowPrefix}time_created"),
         rs.getOffsetDateTime("${rowPrefix}time_updated"),
         auditStatusRepository.mapRow(rs, "astd_"),
         employeeRepository.mapRow(rs, "auditActionEmployee_")
      )

   private fun mapAuditAction(rs: ResultSet): AuditActionEntity {
      return AuditActionEntity(
         id = rs.getLong("auditAction_id"),
         timeCreated = rs.getOffsetDateTime("auditAction_time_created"),
         timeUpdated = rs.getOffsetDateTime("auditAction_time_updated"),
         status = auditStatusRepository.mapRow(rs, "astd_"),
         changedBy = mapAuditActionEmployee(rs)
      )
   }

   private fun mapAuditActionEmployee(rs: ResultSet): EmployeeEntity {
      return EmployeeEntity(
         id = rs.getLong("auditActionEmployee_id"),
         type = rs.getString("auditActionEmployee_type"),
         number = rs.getInt("auditActionEmployee_number"),
         company = mapCompany(rs),
         lastName = rs.getString("auditActionEmployee_last_name"),
         firstNameMi = rs.getString("auditActionEmployee_first_name_mi"),  // FIXME fix query so that it isn't trimming stuff to null when employee is managed by PostgreSQL
         passCode = rs.getString("auditActionEmployee_pass_code"),
         store = mapStore(rs),
         active = rs.getBoolean("auditActionEmployee_active"),
         department = mapAuditActionEmployeeDepartment(rs),
         cynergiSystemAdmin = rs.getBoolean("auditActionEmployee_cynergi_system_admin"),
         alternativeStoreIndicator = rs.getString("auditActionEmployee_alternative_store_indicator"),
         alternativeArea = rs.getInt("auditActionEmployee_alternative_area")
      )
   }

   private fun mapAuditActionEmployeeDepartment(rs: ResultSet): DepartmentEntity? {
      return if (rs.getString("auditActionEmployeeDept_id") != null) {
         DepartmentEntity(
            id = rs.getLong("auditActionEmployeeDept_id"),
            code = rs.getString("auditActionEmployeeDept_code"),
            description = rs.getString("auditActionEmployeeDept_description"),
            securityProfile = rs.getInt("auditActionEmployeeDept_security_profile"),
            defaultMenu = rs.getString("auditActionEmployeeDept_default_menu"),
            company = mapCompany(rs)
         )
      } else {
         null
      }
   }

   private fun mapStore(rs: ResultSet): Store? {
      return if (rs.getString("auditActionEmployee_store_id") != null) {
         SimpleStore(
            id = rs.getLong("auditActionEmployee_store_id"),
            number = rs.getInt("auditActionEmployee_store_number"),
            name = rs.getString("auditActionEmployee_store_name"),
            company = mapCompany(rs)
         )
      } else {
         null;
      }
   }

   private fun mapCompany(rs: ResultSet): Company {
      return CompanyEntity(
         id = rs.getLong("comp_id"),
         name = rs.getString("comp_name"),
         doingBusinessAs = rs.getString("comp_doing_business_as"),
         clientCode = rs.getString("comp_client_code"),
         clientId = rs.getInt("comp_client_id"),
         federalIdNumber = rs.getString("comp_federal_id_number"),
         datasetCode = rs.getString("comp_dataset_code")
      )
   }
}

