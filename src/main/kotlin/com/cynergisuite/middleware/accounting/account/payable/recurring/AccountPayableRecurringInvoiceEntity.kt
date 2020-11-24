package com.cynergisuite.middleware.accounting.account.payable.recurring

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableRecurringInvoiceStatusType
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.vendor.VendorEntity
import java.math.BigDecimal
import java.time.LocalDate

data class AccountPayableRecurringInvoiceEntity(
   val id: Long? = null,
   val vendor: VendorEntity,
   val invoice: String,
   val invoiceAmount: BigDecimal,
   val fixedAmountIndicator: Boolean,
   val employeeNumberId: Int,
   val message: String? = null,
   val codeIndicator: String? = null,
   val type: String,
   val payTo: VendorEntity,
   val lastTransferToCreateInvoiceDate: LocalDate? = null,
   val status: AccountPayableRecurringInvoiceStatusType,
   val dueDays: Int,
   val automatedIndicator: Boolean,
   val separateCheckIndicator: Boolean,
   val expenseMonthCreationIndicator: ExpenseMonthCreationType,
   val invoiceDay: Int,
   val expenseDay: Int,
   val schedule: ScheduleEntity? = null,
   val lastCreatedInPeriod: LocalDate? = null,
   val nextCreationDate: LocalDate? = null,
   val nextInvoiceDate: LocalDate? = null,
   val nextExpenseDate: LocalDate? = null
) : Identifiable {

   constructor(
      dto: AccountPayableRecurringInvoiceDTO,
      vendor: VendorEntity,
      payTo: VendorEntity,
      status: AccountPayableRecurringInvoiceStatusType,
      expenseMonthCreationIndicator: ExpenseMonthCreationType?,
      schedule: ScheduleEntity?
   ) :
      this(
         id = dto.id,
         vendor = vendor,
         invoice = dto.invoice!!,
         invoiceAmount = dto.invoiceAmount!!,
         fixedAmountIndicator = dto.fixedAmountIndicator!!,
         employeeNumberId = dto.employeeNumberId!!,
         message = dto.message,
         codeIndicator = dto.codeIndicator,
         type = dto.type!!,
         payTo = payTo,
         lastTransferToCreateInvoiceDate = dto.lastTransferToCreateInvoiceDate,
         status = status,
         dueDays = dto.dueDays!!,
         automatedIndicator = dto.automatedIndicator!!,
         separateCheckIndicator = dto.separateCheckIndicator!!,
         expenseMonthCreationIndicator = expenseMonthCreationIndicator!!,
         invoiceDay = dto.invoiceDay!!,
         expenseDay = dto.expenseDay!!,
         schedule = schedule,
         lastCreatedInPeriod = dto.lastCreatedInPeriod,
         nextCreationDate = dto.nextCreationDate,
         nextInvoiceDate = dto.nextInvoiceDate,
         nextExpenseDate = dto.nextExpenseDate
      )

   override fun myId(): Long? = id
}
