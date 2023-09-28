package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.cynergisuite.domain.*
import com.cynergisuite.middleware.accounting.account.VendorBalanceDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure.AccountPayableInvoiceInquiryRepository
import com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure.AccountPayableInvoiceReportRepository
import com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure.AccountPayableInvoiceRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.opencsv.CSVWriter
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.util.UUID

@Singleton
class AccountPayableInvoiceService @Inject constructor(
   private val accountPayableInvoiceInquiryRepository: AccountPayableInvoiceInquiryRepository,
   private val accountPayableInvoiceRepository: AccountPayableInvoiceRepository,
   private val accountPayableInvoiceReportRepository: AccountPayableInvoiceReportRepository,
   private val accountPayableInvoiceValidator: AccountPayableInvoiceValidator
) {

   fun fetchById(id: UUID, company: CompanyEntity): AccountPayableInvoiceDTO? =
      accountPayableInvoiceRepository.findOne(id, company)?.let { AccountPayableInvoiceDTO(it) }

   fun create(dto: AccountPayableInvoiceDTO, company: CompanyEntity): AccountPayableInvoiceDTO {
      val toCreate = accountPayableInvoiceValidator.validateCreate(dto, company)

      return transformEntity(accountPayableInvoiceRepository.insert(toCreate, company))
   }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<AccountPayableInvoiceDTO> {
      val found = accountPayableInvoiceRepository.findAll(company, pageRequest)

      return found.toPage { accountPayableInvoiceEntity: AccountPayableInvoiceEntity ->
         AccountPayableInvoiceDTO(accountPayableInvoiceEntity)
      }
   }

   fun fetchAllByVendor(company: CompanyEntity, filterRequest: AccountPayableInvoiceListByVendorFilterRequest): Page<AccountPayableInvoiceListByVendorDTO> {
      val found = accountPayableInvoiceRepository.findAllByVendor(company, filterRequest)

      return found.toPage { dto: AccountPayableInvoiceListByVendorDTO -> dto }
   }

   fun fetchReport(company: CompanyEntity, filterRequest: InvoiceReportFilterRequest): AccountPayableInvoiceReportTemplate {
      val found = accountPayableInvoiceReportRepository.fetchReport(company, filterRequest)

      return AccountPayableInvoiceReportTemplate(found)
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

   fun vendorBalance(company: CompanyEntity, filterRequest: AccountPayableVendorBalanceReportFilterRequest): VendorBalanceDTO {
      return accountPayableInvoiceRepository.vendorBalance(company, filterRequest)

   }

   private fun transformEntity(accountPayableInvoiceEntity: AccountPayableInvoiceEntity): AccountPayableInvoiceDTO {
      return AccountPayableInvoiceDTO(accountPayableInvoiceEntity)
   }
}
