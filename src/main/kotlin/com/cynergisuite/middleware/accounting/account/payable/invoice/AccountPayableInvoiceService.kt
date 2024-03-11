package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.cynergisuite.domain.AccountPayableInvoiceFilterRequest
import com.cynergisuite.domain.AccountPayableInvoiceInquiryFilterRequest
import com.cynergisuite.domain.AccountPayableInvoiceListByVendorFilterRequest
import com.cynergisuite.domain.AccountPayableVendorBalanceReportFilterRequest
import com.cynergisuite.domain.ExpenseReportFilterRequest
import com.cynergisuite.domain.InvoiceReportFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.VendorBalanceDTO
import com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure.AccountPayableDistributionDetailRepository
import com.cynergisuite.middleware.accounting.account.payable.expense.AccountPayableExpenseReportTemplate
import com.cynergisuite.middleware.accounting.account.payable.expense.infrastructure.AccountPayableExpenseReportRepository
import com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure.AccountPayableInvoiceDistributionRepository
import com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure.AccountPayableInvoiceInquiryRepository
import com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure.AccountPayableInvoiceReportRepository
import com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure.AccountPayableInvoiceRepository
import com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure.AccountPayableInvoiceScheduleRepository
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDetailEntity
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDistributionDTO
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDistributionEntity
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentEntity
import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentDetailRepository
import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentRepository
import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentStatusTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentTypeTypeRepository
import com.cynergisuite.middleware.accounting.bank.infrastructure.BankRepository
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationDTO
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationEntity
import com.cynergisuite.middleware.accounting.bank.reconciliation.infrastructure.BankReconciliationRepository
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDTO
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.infrastructure.BankReconciliationTypeRepository
import com.cynergisuite.middleware.accounting.general.ledger.control.infrastructure.GeneralLedgerControlRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import com.cynergisuite.util.GroupingType
import com.opencsv.CSVWriter
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.math.BigDecimal
import java.util.UUID

@Singleton
class AccountPayableInvoiceService @Inject constructor(
   private val accountPayableInvoiceInquiryRepository: AccountPayableInvoiceInquiryRepository,
   private val accountPayableInvoiceRepository: AccountPayableInvoiceRepository,
   private val accountPayableInvoiceReportRepository: AccountPayableInvoiceReportRepository,
   private val accountPayableExpenseReportRepository: AccountPayableExpenseReportRepository,
   private val accountPayableInvoiceValidator: AccountPayableInvoiceValidator,
   private val vendorRepository: VendorRepository,
   private val appttRepository: AccountPayablePaymentTypeTypeRepository,
   private val bankRepository: BankRepository,
   private val glcRepo: GeneralLedgerControlRepository,
   private val bankReconciliationRepository: BankReconciliationRepository,
   private val apInvoiceScheduleRepo: AccountPayableInvoiceScheduleRepository,
   private val accountPayablePaymentRepository: AccountPayablePaymentRepository,
   private val accountPayablePaymentStatusTypeRepository: AccountPayablePaymentStatusTypeRepository,
   private val accountPayablePaymentTypeTypeRepository: AccountPayablePaymentTypeTypeRepository,
   private val accountPayableInvoiceDistributionRepository: AccountPayableInvoiceDistributionRepository,
   private val accountPayableDistributionDetailRepository: AccountPayableDistributionDetailRepository,
   private val vendorPaymentTermRepository: VendorPaymentTermRepository,
   private val accountPayablePaymentDetailRepository: AccountPayablePaymentDetailRepository,
   private val bankReconTypeRepository: BankReconciliationTypeRepository
) {

   fun fetchById(id: UUID, company: CompanyEntity): AccountPayableInvoiceDTO? =
      accountPayableInvoiceRepository.findOne(id, company)?.let { AccountPayableInvoiceDTO(it) }

   fun create(dto: AccountPayableInvoiceDTO, company: CompanyEntity): AccountPayableInvoiceDTO {
      val toCreate = accountPayableInvoiceValidator.validateCreate(dto, company)

      return transformEntity(accountPayableInvoiceRepository.insert(toCreate, company))
   }

   fun fetchAll(company: CompanyEntity, filterRequest: AccountPayableInvoiceFilterRequest): Page<AccountPayableInvoiceDTO> {
      val found = accountPayableInvoiceRepository.findAll(company, filterRequest)

      return found.toPage { accountPayableInvoiceEntity: AccountPayableInvoiceEntity ->
         AccountPayableInvoiceDTO(accountPayableInvoiceEntity)
      }
   }

   fun fetchAllByVendor(company: CompanyEntity, filterRequest: AccountPayableInvoiceListByVendorFilterRequest): Page<AccountPayableInvoiceListByVendorDTO> {
      val found = accountPayableInvoiceRepository.findAllByVendor(company, filterRequest)

      return found.toPage { dto: AccountPayableInvoiceListByVendorDTO -> dto }
   }

   fun fetchOpenByVendor(company: CompanyEntity, filterRequest: AccountPayableInvoiceListByVendorFilterRequest): Page<AccountPayableInvoiceListByVendorDTO> {
      val found = accountPayableInvoiceRepository.findOpenByVendor(company, filterRequest)

      return found.toPage { dto: AccountPayableInvoiceListByVendorDTO -> dto }
   }

   fun fetchReport(company: CompanyEntity, filterRequest: InvoiceReportFilterRequest): AccountPayableInvoiceReportTemplate {
      val found = accountPayableInvoiceReportRepository.fetchReport(company, filterRequest)

      return AccountPayableInvoiceReportTemplate(found)
   }

   fun fetchInvoicePayments(invoiceID: UUID, company: CompanyEntity): List<AccountPayableInvoiceInquiryPaymentDTO>{
      return accountPayableInvoiceInquiryRepository.fetchInquiryPayments(invoiceID, company)
   }

   fun fetchGLDistributions(invoiceID: UUID, company: CompanyEntity): List<AccountPayableDistDetailReportDTO> {
      return accountPayableInvoiceInquiryRepository.fetchInquiryDistributions(invoiceID, company)
   }

   fun fetchExpenseReport(company: CompanyEntity, filterRequest: ExpenseReportFilterRequest): AccountPayableExpenseReportTemplate {
      val allInvoices = accountPayableExpenseReportRepository.fetchReport(company, filterRequest)

      val beginDate = filterRequest.beginDate
      val endDate = filterRequest.endDate

      val beginBalance = allInvoices
         .filter { inv ->
            inv.expenseDate!! < beginDate &&
            (inv.status != "P" ||  inv.pmtDate?.let { it > endDate } ?: true )
         }
         .mapNotNull { it.invoiceAmount }
         .sumOf { it }
      val newInvoicesTotal = allInvoices
         .filter {
            it.expenseDate!! >= beginDate &&
               it.expenseDate!! <= endDate
         }
         .mapNotNull { it.invoiceAmount }
         .sumOf { it }
      val paidInvoicesTotal = allInvoices
         .filter {
            it.pmtDate != null && it.pmtDate!! >= beginDate && it.pmtDate!! <= endDate
         }
         .mapNotNull { it.invoiceAmount }
         .sumOf { it }
      val endBalance = beginBalance + newInvoicesTotal - paidInvoicesTotal
      val chargedAfterEndingDate = allInvoices
         .filter {
            it.pmtDate != null && it.pmtDate!! >= beginDate &&
               it.pmtDate!! <= endDate &&
               it.expenseDate!! > endDate
         }
         .mapNotNull { it.invoiceAmount }
         .sumOf { it }

      return AccountPayableExpenseReportTemplate(allInvoices, beginBalance, newInvoicesTotal, paidInvoicesTotal, endBalance, chargedAfterEndingDate, GroupingType.fromString(filterRequest.sortBy!!))
   }

   fun export(filterRequest: InvoiceReportFilterRequest, company: CompanyEntity): ByteArray {
      val found = accountPayableInvoiceReportRepository.export(company, filterRequest)
      val stream = ByteArrayOutputStream()
      val output = OutputStreamWriter(stream)
      val csvWriter = CSVWriter(output, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER)

      val headers = arrayOf("Vend_Nbr","Vend_Name","Vend_Group","Invoice_Nbr","Invoice_Type","PO-Nbr","Invoice_Date",
            "Entry_Date", "Invoice_Status", "Invoice_Amt", "Discount_Amt", "Due_Date", "Expense_Date", "Amt_Paid",
            "Partial_Pay_Flag", "Bank_Nbr", "Check_Nbr", "GL_Acct_Nbr", "Account_Desc", "PFT_Ctr", "Dist_Amt")
      csvWriter.writeNext(headers)

      for(element in found) {
         val data = arrayOf<String>(
            element.vendorNumber.toString(),
            element.vendorName.toString(),
            element.vendorGroup.toString(),
            element.invoice.toString(),
            element.type.toString(),
            element.status.toString(),
            element.invoiceDate.toString(),
            element.entryDate.toString(),
            element.invoiceAmount.toString(),
            element.discountTaken.toString(),
            element.dueDate.toString(),
            element.expenseDate.toString(),
            element.paidAmount.toString(),
            if (element.invoiceAmount!! > element.paidAmount!!) "Y" else "N",
            element.bankNumber.toString(),
            element.pmtNumber.toString(),
            element.acctNumber.toString(),
            element.acctName.toString(),
            element.distCenter.toString(),
            element.distAmount.toString(),
         )
         csvWriter.writeNext(data)
      }
      csvWriter.close()
      output.close()
      return stream.toByteArray()
   }

   fun inquiry(company: CompanyEntity, filterRequest: AccountPayableInvoiceInquiryFilterRequest): Page<AccountPayableInvoiceInquiryDTO> {
      val found = accountPayableInvoiceInquiryRepository.fetchInquiry(company, filterRequest)

      return found.toPage { dto: AccountPayableInvoiceInquiryDTO -> dto }
   }

   fun update(id: UUID, dto: AccountPayableInvoiceDTO, company: CompanyEntity): AccountPayableInvoiceDTO {
      val toUpdate = accountPayableInvoiceValidator.validateUpdate(id, dto, company)

      return transformEntity(accountPayableInvoiceRepository.update(toUpdate, company))
   }

   fun vendorBalance(company: CompanyEntity, filterRequest: AccountPayableVendorBalanceReportFilterRequest): List<VendorBalanceDTO> {
      return accountPayableInvoiceRepository.vendorBalance(company, filterRequest)
   }

   fun maintenance(dto: AccountPayableInvoiceMaintenanceDTO, company: CompanyEntity): AccountPayableInvoiceMaintenanceDTO {
      val vendor = vendorRepository.findOne(dto.apInvoice?.vendor!!.id!!, company)
      val payTo = if (dto.apInvoice?.payTo != null) vendorRepository.findOne(dto.apInvoice?.payTo!!.id!!, company) else vendor
      val invoice = dto.apInvoice!!
      val terms = vendorPaymentTermRepository.findOne(vendor!!.paymentTerm.id!!, company)
      val scheduleList: MutableList<AccountPayableInvoiceScheduleEntity> = mutableListOf()
      val bank = bankRepository.findOne(dto.apPayment?.bank!!.id!!, company)
      val apPmtStatus = accountPayablePaymentStatusTypeRepository.findOne("P")
      val paymentType = accountPayablePaymentTypeTypeRepository.findOne(dto.apPayment!!.type!!.value)


      //create ap invoice schedule
      terms!!.numberOfPayments.let {
         for (i in 1..it) {
            val apptt = dto.apInvoiceSchedule?.get(0)?.externalPaymentTypeId?.value?.let { it1 -> appttRepository.findOne(it1) }
            val schedule = AccountPayableInvoiceScheduleEntity(
               null,
               invoice.id!!,
               company.id!!,
               invoice.invoiceDate!!.plusMonths(i.toLong()),
               i,
               terms.scheduleRecords[i-1].duePercent.times(invoice.invoiceAmount!!),
               bank!!.id!!,
               apptt,
               dto.apInvoiceSchedule?.get(0)?.externalPaymentNumber,
               dto.apInvoiceSchedule?.get(0)?.externalPaymentDate,
               false,
               false
            )

            scheduleList.add(apInvoiceScheduleRepo.insert(schedule))
         }
      }
      //update ap Payment and check if discounts apply
      if (invoice.status?.value == "P" || scheduleList.any { it.externalPaymentNumber != null}) {
         scheduleList.forEach {
            if (it.externalPaymentDate != null && invoice.discountDate != null && it.externalPaymentDate <= invoice.discountDate) {
               invoice.paidAmount = it.amountToPay
               invoice.discountTaken = invoice.invoiceAmount?.minus(it.amountToPay)
            } else {
               invoice.paidAmount = it.amountToPay
            }
         }

         for (schedule in scheduleList) {
            //check if payment already exists for this bank or payment number
            if (schedule.bank == dto.apPayment?.bank?.id || schedule.externalPaymentNumber == dto.apPayment?.paymentNumber) {
               dto.apPayment?.amount = dto.apPayment?.amount?.plus(schedule.amountToPay)
               val apPayment = AccountPayablePaymentEntity(
                  dto.apPayment!!,
                  bank!!,
                  vendor,
                  apPmtStatus!!,
                  paymentType!!,
                  null
               )
               accountPayablePaymentRepository.update(apPayment, company)

               val distributionEntity = accountPayablePaymentRepository.findDistribution(bank.generalLedgerAccount.id!!, company)
               if (distributionEntity != null ) {
                  val distribution = AccountPayablePaymentDistributionDTO(distributionEntity)
                  distribution.distributionAmount = distribution.distributionAmount.plus(schedule.amountToPay)
                  val distEnt = AccountPayablePaymentDistributionEntity(distribution)
                  accountPayablePaymentRepository.updateDistribution(distEnt, company)
               } else {
                  val glc = glcRepo.findOne(company)
                  //create 2 ap Payment distribution records per payment
                  val bankDist = AccountPayablePaymentDistributionEntity(
                     null,
                     dto.apPayment!!.id!!,
                     bank.generalLedgerAccount.id,
                     bank.generalLedgerProfitCenter.myId(),
                     dto.apPayment!!.amount!!,
                  )
                  val apAcct = AccountPayablePaymentDistributionEntity(
                     null,
                     dto.apPayment!!.id!!,
                     glc!!.defaultAccountPayableAccount!!.id!!,
                     glc.defaultProfitCenter.myId(),
                     dto.apPayment!!.amount!!.times(BigDecimal(-1)),
                  )
                  accountPayablePaymentRepository.insertDistributions(apAcct, company)
                  accountPayablePaymentRepository.insertDistributions(bankDist, company)
               }

               val bankRecon = bankReconciliationRepository.findOne(bank.id!!, apPayment.type.value, apPayment.paymentNumber, apPayment.paymentDate, company)
               if (bankRecon != null) {
                  bankRecon.amount.plus(schedule.amountToPay)
                  bankReconciliationRepository.update(bankRecon, company)
               } else {
                  val bankReconType = bankReconTypeRepository.findOne(apPayment.type.value)
                  val newBankRecon = BankReconciliationDTO(
                     null,
                     SimpleIdentifiableDTO(bank),
                     BankReconciliationTypeDTO(apPayment.type.value, apPayment.type.description),
                     apPayment.paymentDate,
                     null,
                     apPayment.amount,
                     "A/P VND#" + vendor.number,
                     apPayment.paymentNumber
                  )
                  val bankReconEntity = BankReconciliationEntity(
                     newBankRecon,
                     bank,
                     bankReconType!!
                  )
                  bankReconciliationRepository.insert(bankReconEntity, company)
               }
            } else {
               val newApPayment = AccountPayablePaymentEntity(
                  null,
                  bank!!,
                  payTo!!,
                  apPmtStatus!!,
                  schedule.externalPaymentTypeId!!,
                  schedule.externalPaymentDate!!,
                  null,
                  null,
                  schedule.externalPaymentNumber!!,
                  schedule.amountToPay,
               )
               val newApPaymentEnt = accountPayablePaymentRepository.insert(newApPayment, company)
               val glc = glcRepo.findOne(company)
               //create 2 ap Payment distribution records per payment
               val bankDist = AccountPayablePaymentDistributionEntity(
                  null,
                  newApPaymentEnt.id!!,
                  bank.generalLedgerAccount.id!!,
                  bank.generalLedgerProfitCenter.myId(),
                  newApPaymentEnt.amount,
               )
               val apAcct = AccountPayablePaymentDistributionEntity(
                  null,
                  newApPaymentEnt.id,
                  glc!!.defaultAccountPayableAccount!!.id!!,
                  glc.defaultProfitCenter.myId(),
                  newApPaymentEnt.amount.times(BigDecimal(-1)),
               )

               val toCreate = accountPayableInvoiceValidator.validateCreate(invoice, company)


               val apPaymentDetail = AccountPayablePaymentDetailEntity(
                  null,
                  vendor,
                  toCreate,
                  newApPaymentEnt,
                  newApPaymentEnt.amount,
                  invoice.discountTaken
               )
               accountPayablePaymentRepository.insertDistributions(apAcct, company)
               accountPayablePaymentRepository.insertDistributions(bankDist, company)
               accountPayablePaymentDetailRepository.insert(apPaymentDetail, company)

               val bankRecon = bankReconciliationRepository.findOne(bank.id!!, newApPaymentEnt.type.value, newApPaymentEnt.paymentNumber, newApPaymentEnt.paymentDate, company)
               if (bankRecon != null) {
                  bankRecon.amount.plus(apPaymentDetail.amount)
                  bankReconciliationRepository.update(bankRecon, company)
               } else {
                  val bankReconType = bankReconTypeRepository.findOne(newApPaymentEnt.type.value)
                  val newBankRecon = BankReconciliationDTO(
                     null,
                     SimpleIdentifiableDTO(bank),
                     BankReconciliationTypeDTO(newApPaymentEnt.type.value, newApPaymentEnt.type.description),
                     newApPaymentEnt.paymentDate,
                     null,
                     newApPaymentEnt.amount,
                     "A/P VND#" + vendor.number,
                     newApPaymentEnt.paymentNumber
                  )
                  val bankReconEntity = BankReconciliationEntity(
                     newBankRecon,
                     bank,
                     bankReconType!!
                  )
                  bankReconciliationRepository.insert(bankReconEntity, company)
               }
            }
         }
      }


      //for each invoice dist with template chosen, create ap invoice dist
      val distributionDetails = accountPayableDistributionDetailRepository.findAllRecordsByGroup(company, dto.glDistribution?.id!!)
      for (dist in distributionDetails) {
         val invDist = AccountPayableInvoiceDistributionEntity(
            null,
            invoice.id!!,
            dist.account.id!!,
            dist.profitCenter.myId(),
            dto.apInvoice?.invoiceAmount!!.times(dist.percent)
         )
         accountPayableInvoiceDistributionRepository.insert(invDist)
      }

      val toCreate = accountPayableInvoiceValidator.validateCreate(invoice, company)
      val updatedInvoice = accountPayableInvoiceRepository.insert(toCreate, company)
      val updatedInvoiceDTO = transformEntity(updatedInvoice)
      val scheduleListDTO = scheduleList.map { AccountPayableInvoiceScheduleDTO(it) }.toMutableList()
      val updatedInvoiceMaintenanceDTO = AccountPayableInvoiceMaintenanceDTO(updatedInvoiceDTO, null, null, scheduleListDTO)
      return updatedInvoiceMaintenanceDTO
   }

   private fun transformEntity(accountPayableInvoiceEntity: AccountPayableInvoiceEntity): AccountPayableInvoiceDTO {
      return AccountPayableInvoiceDTO(accountPayableInvoiceEntity)
   }

   fun exportExpenseInvoices(filterRequest: ExpenseReportFilterRequest, company: CompanyEntity): ByteArray {
      val found = accountPayableExpenseReportRepository.fetchReport(company, filterRequest)
      val stream = ByteArrayOutputStream()
      val output = OutputStreamWriter(stream)
      val csvWriter = CSVWriter(output, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER)

      val headers = arrayOf("GL_Acct_Nbr", "Account_Desc","Invoice_Date","Invoice_Nbr", "Vend_Nbr","Vend_Name",
         "PFT_Ctr","Invoice_Total", "GL_Amt",  "Payment_Nbr", "Date_Paid","PO-Nbr", "Expense_Date")
      csvWriter.writeNext(headers)

      for(element in found) {
         val data = arrayOf(
            element.acctNumber.toString(),
            element.acctName.toString(),
            element.invoiceDate.toString(),
            element.invoice.toString(),
            element.vendorNumber.toString(),
            element.vendorName.toString(),
            element.distCenter.toString(),
            element.invoiceAmount.toString(),
            element.pmtNumber.toString(),
            element.pmtDate.toString(),
            element.poHeaderNumber.toString(),
            element.expenseDate.toString(),
         )
         csvWriter.writeNext(data)
      }
      csvWriter.close()
      output.close()
      return stream.toByteArray()
   }

   fun delete (id: UUID, company: CompanyEntity) {
      accountPayableInvoiceRepository.delete(id, company)
   }
}
