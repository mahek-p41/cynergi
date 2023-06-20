package com.cynergisuite.middleware.authentication.user.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure.AccountPayableInvoiceRepository
import com.cynergisuite.middleware.authentication.user.SecurityGroup
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet

@Singleton
class SecurityGroupRepository @Inject constructor(
    private val jdbc: Jdbi,
    private val companyRepository: CompanyRepository,
) {
    private val logger: Logger = LoggerFactory.getLogger(AccountPayableInvoiceRepository::class.java)

    fun selectBaseQuery(): String {
        return """
         SELECT
            secgrp.id                                                          AS secgrp_id,
            secgrp.value                                                       AS secgrp_value,
            secgrp.description                                                 AS secgrp_description,
            secgrp.company_id                                                  AS secgrp_company_id,
            empSecGrp.employee_id_sfk										   AS secGrp_emp_id
         FROM security_group secgrp
            JOIN employee_to_security_group empSecGrp                   ON secgrp.id = empSecGrp.security_group_id AND empSecGrp.deleted = FALSE
           
      """
    }


    @ReadOnly
    fun findOne(id: Long): SecurityGroup? {
        val found =
            jdbc.findFirstOrNull("${selectBaseQuery()} WHERE empSecGrp.employee_id_sfk = :id AND secGrp.deleted = FALSE", mapOf("id" to id)) { rs, _ -> mapRow(rs) }

        logger.trace("Searching for Company: {} resulted in {}", id, found)

        return found
    }

    fun mapRow(
        rs: ResultSet,
    ): SecurityGroup {
        val company = companyRepository.findOne(rs.getUuid("secgrp_company_id"))
        return SecurityGroup(
            id = rs.getUuid("secgrp_id"),
            value = rs.getString("secgrp_value"),
            description = rs.getString("secgrp_description"),
            company = company!!
        )
    }
}
