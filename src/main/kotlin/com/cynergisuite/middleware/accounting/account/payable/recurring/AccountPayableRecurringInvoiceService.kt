package com.cynergisuite.middleware.accounting.account.payable.recurring

import com.cynergisuite.domain.AccountPayableInvoiceListByVendorFilterRequest
import com.cynergisuite.domain.InvoiceReportFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceSelectedTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDistributionEntity
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceService
import com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure.AccountPayableInvoiceDistributionRepository
import com.cynergisuite.middleware.accounting.account.payable.recurring.distribution.infrastructure.AccountPayableRecurringInvoiceDistributionRepository
import com.cynergisuite.middleware.accounting.account.payable.recurring.infrastructure.AccountPayableRecurringInvoiceRepository
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.employee.EmployeeService
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Singleton
class AccountPayableRecurringInvoiceService @Inject constructor(
   private val accountPayableRecurringInvoiceRepository: AccountPayableRecurringInvoiceRepository,
   private val accountPayableRecurringInvoiceValidator: AccountPayableRecurringInvoiceValidator,
   private val employeeService: EmployeeService,
   private val vendorPaymentTermRepository: VendorPaymentTermRepository,
   private val accountPayableInvoiceService: AccountPayableInvoiceService,
   private val accountPayableRecurringInvoiceDistributionRepository: AccountPayableRecurringInvoiceDistributionRepository,
   private val accountPayableInvoiceDistributionRepository: AccountPayableInvoiceDistributionRepository
) {

   fun fetchById(id: UUID, company: CompanyEntity): AccountPayableRecurringInvoiceDTO? =
      accountPayableRecurringInvoiceRepository.findOne(id, company)?.let { AccountPayableRecurringInvoiceDTO(it) }

   fun create(dto: AccountPayableRecurringInvoiceDTO, company: CompanyEntity): AccountPayableRecurringInvoiceDTO {
      val toCreate = accountPayableRecurringInvoiceValidator.validateCreate(dto, company)

      return transformEntity(accountPayableRecurringInvoiceRepository.insert(toCreate, company))
   }

   fun fetchAll(company: CompanyEntity, filterRequest: AccountPayableInvoiceListByVendorFilterRequest): Page<AccountPayableRecurringInvoiceDTO> {
      val found = accountPayableRecurringInvoiceRepository.findAll(company, filterRequest)

      return found.toPage { accountPayableRecurringInvoiceEntity: AccountPayableRecurringInvoiceEntity ->
         AccountPayableRecurringInvoiceDTO(accountPayableRecurringInvoiceEntity)
      }
   }

   fun fetchReport(company: CompanyEntity, filterRequest: InvoiceReportFilterRequest): AccountPayableRecurringInvoiceReportTemplate {
      return accountPayableRecurringInvoiceRepository.fetchReport(company, filterRequest)
   }

   fun update(id: UUID, dto: AccountPayableRecurringInvoiceDTO, company: CompanyEntity): AccountPayableRecurringInvoiceDTO {
      val toUpdate = accountPayableRecurringInvoiceValidator.validateUpdate(id, dto, company)

      return transformEntity(accountPayableRecurringInvoiceRepository.update(toUpdate, company))
   }

   fun transfer(dto: AccountPayableRecurringInvoiceDTO, user: User): AccountPayableInvoiceDTO {
      val employee = employeeService.fetchOne(user.myId(), user.myCompany())
      val vpt = vendorPaymentTermRepository.findOne(dto.vendor!!.paymentTerm!!.id!!, user.myCompany())!!
      val discountDate: LocalDate
      if (vpt.discountMonth == 0) {
         discountDate = dto.nextInvoiceDate!!.plusDays(vpt.discountDays!!.toLong())
      } else {
         discountDate = dto.nextInvoiceDate!!.plusMonths(vpt.discountMonth!!.toLong()).plusDays(vpt.discountDays!!.toLong())
      }

      val invoice = AccountPayableInvoiceDTO(
         id = null,
         vendor = dto.vendor,
         invoice = dto.invoice,
         purchaseOrder = null,
         invoiceDate = dto.nextInvoiceDate,
         invoiceAmount = dto.invoiceAmount,
         discountAmount = null,
         discountPercent = null,
         autoDistributionApplied = false,
         discountTaken = null,
         entryDate = LocalDate.now(),
         expenseDate = dto.nextExpenseDate,
         discountDate = discountDate,
         employee = EmployeeValueObject(employee),
         originalInvoiceAmount = dto.invoiceAmount,
         message = dto.message,
         selected = AccountPayableInvoiceSelectedTypeDTO("N"),
         multiplePaymentIndicator = false,
         paidAmount = BigDecimal.ZERO,
         selectedAmount = dto.invoiceAmount,
         type = AccountPayableInvoiceTypeDTO("E"),
         status = AccountPayableInvoiceStatusTypeDTO("O"),
         dueDate = dto.nextInvoiceDate!!.plusDays(dto.dueDays!!.toLong()),
         payTo = dto.payTo,
         separateCheckIndicator = false,
         useTaxIndicator = false,
         receiveDate = null,
         location = null
      )

      //move recur distributions to inv dists, update next invoice dates
      val recDist = accountPayableRecurringInvoiceDistributionRepository.findByRecurringInvoice(dto.id!!, user.myCompany())
      recDist.forEach {
         val invoiceDist = AccountPayableInvoiceDistributionEntity(
            id = null,
            invoiceId = it.invoiceId,
            accountId = it.accountId,
            profitCenter = it.profitCenter,
            amount = it.amount
         )
         accountPayableInvoiceDistributionRepository.insert(invoiceDist)
      }

      dto.lastCreatedInPeriod = LocalDate.now()
      dto.nextCreationDate = dto.nextCreationDate!!.plusMonths(1)
      dto.nextInvoiceDate = dto.nextInvoiceDate!!.plusMonths(1)
      dto.nextExpenseDate = dto.nextExpenseDate!!.plusMonths(1)
      update(dto.id!!, dto, user.myCompany())
      return accountPayableInvoiceService.create(invoice, user.myCompany())
   }

   private fun transformEntity(accountPayableRecurringInvoiceEntity: AccountPayableRecurringInvoiceEntity): AccountPayableRecurringInvoiceDTO {
      return AccountPayableRecurringInvoiceDTO(accountPayableRecurringInvoiceEntity)
   }
}
