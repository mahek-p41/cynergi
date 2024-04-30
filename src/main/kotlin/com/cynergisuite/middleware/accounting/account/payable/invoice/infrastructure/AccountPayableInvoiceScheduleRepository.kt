package com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure

import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.getUuidOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.update
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDistributionDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceScheduleEntity
import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentTypeTypeRepository
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.sql.ResultSet
import java.util.UUID
import org.apache.commons.lang3.StringUtils
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.transaction.Transactional

@Singleton
class AccountPayableInvoiceScheduleRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val accountPayablePaymentTypeTypeRepository: AccountPayablePaymentTypeTypeRepository
   ){
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableInvoiceScheduleRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         SELECT
            apInvSch.id                                                             AS apInvSch_id,
            apInvSch.invoice_id                                                     AS apInvSch_invoice_id,
            apInvSch.company_id                                                     AS apInvSch_company_id,
            apInvSch.schedule_date                                                  AS apInvSch_schedule_date,
            apInvSch.payment_sequence_number                                        AS apInvSch_payment_sequence_number,
            apInvSch.amount_to_pay                                                  AS apInvSch_amount_to_pay,
            apInvSch.bank_id                                                        AS apInvSch_bank_id,
            apInvSch.external_payment_type_id                                       AS apInvSch_external_payment_type_id,
            apInvSch.external_payment_number                                        AS apInvSch_external_payment_number,
            apInvSch.external_payment_date                                          AS apInvSch_external_payment_date,
            apInvSch.selected_for_processing                                        AS apInvSch_selected_for_processing,
            apInvSch.payment_processed                                              AS apInvSch_payment_processed
         FROM account_payable_invoice_schedule apInvSch
            JOIN company comp ON apInvSch.company_id = comp.id AND comp.deleted = FALSE
      """
   }

   @ReadOnly
   fun fetchByInvoiceId(id: UUID, company: CompanyEntity): List<AccountPayableInvoiceScheduleEntity> {
      val schedules = mutableListOf<AccountPayableInvoiceScheduleEntity>()

      jdbc.query(
         """
            ${selectBaseQuery()}
            WHERE apInvSch.invoice_id = :apInvoiceId and apInvSch.company_id = :companyId
         """.trimIndent(),
         mapOf("apInvoiceId" to id, "companyId" to company.id)
      ) { rs, _ ->
         do {
            schedules.add(mapRow(rs,"apInvSch_"))
         } while (rs.next())
      }

      return schedules
   }

   @Transactional
   fun insert(entity: AccountPayableInvoiceScheduleEntity): AccountPayableInvoiceScheduleEntity {
      logger.debug("Inserting account_payable_invoice_schedule {}", entity)
      return jdbc.insertReturning(
         """
         INSERT INTO account_payable_invoice_schedule (
            invoice_id,
            company_id,
            schedule_date,
            payment_sequence_number,
            amount_to_pay,
            bank_id,
            external_payment_type_id,
            external_payment_number,
            external_payment_date,
            selected_for_processing,
            payment_processed
         ) VALUES (
            :invoiceId,
            :companyId,
            :scheduleDate,
            :paymentSequenceNumber,
            :amountToPay,
            :bank,
            :externalPaymentTypeId,
            :externalPaymentNumber,
            :externalPaymentDate,
            :selectedForProcessing,
            :paymentProcessed
         )
         RETURNING *
      """.trimIndent(),
         mapOf(
            "invoiceId" to entity.invoiceId,
            "companyId" to entity.companyId,
            "scheduleDate" to entity.scheduleDate,
            "paymentSequenceNumber" to entity.paymentSequenceNumber,
            "amountToPay" to entity.amountToPay,
            "bank" to entity.bank,
            "externalPaymentTypeId" to entity.externalPaymentTypeId?.id,
            "externalPaymentNumber" to entity.externalPaymentNumber,
            "externalPaymentDate" to entity.externalPaymentDate,
            "selectedForProcessing" to entity.selectedForProcessing,
            "paymentProcessed" to entity.paymentProcessed
         )
      ) { rs, _ ->
         mapRow(rs)
      }
   }

   @Transactional
   fun replace(invoiceId: UUID, entityList: List<AccountPayableInvoiceScheduleEntity>, company: CompanyEntity): List<AccountPayableInvoiceScheduleEntity> {
      val updatedList: MutableList<AccountPayableInvoiceScheduleEntity> = mutableListOf()
      jdbc.update(
         """
         DELETE FROM account_payable_invoice_schedule
         WHERE invoice_id = :invoice_id
      """.trimIndent(),
         mapOf(
            "invoice_id" to invoiceId,
         )
      )
      entityList.forEach {
         updatedList.add(insert(it))
      }
      return updatedList
   }

   private fun mapRow(
      rs: ResultSet,
      columnPrefix: String = StringUtils.EMPTY
   ): AccountPayableInvoiceScheduleEntity {
      val externalPaymentType = rs.getIntOrNull("external_payment_type_id") ?.let {
         accountPayablePaymentTypeTypeRepository.findOne(it)
      }
      return AccountPayableInvoiceScheduleEntity(
         id = rs.getUuid("${columnPrefix}id"),
         invoiceId = rs.getUuid("${columnPrefix}invoice_id"),
         companyId = rs.getUuid("${columnPrefix}company_id"),
         scheduleDate = rs.getDate("${columnPrefix}schedule_date").toLocalDate(),
         paymentSequenceNumber = rs.getInt("${columnPrefix}payment_sequence_number"),
         amountToPay = rs.getBigDecimal("${columnPrefix}amount_to_pay"),
         bank = rs.getUuidOrNull("${columnPrefix}bank_id"),
         externalPaymentTypeId = externalPaymentType,
         externalPaymentNumber = rs.getString("${columnPrefix}external_payment_number"),
         externalPaymentDate = rs.getDate("${columnPrefix}external_payment_date")?.toLocalDate(),
         selectedForProcessing = rs.getBoolean("${columnPrefix}selected_for_processing"),
         paymentProcessed = rs.getBoolean("${columnPrefix}payment_processed")

      )
   }
}
