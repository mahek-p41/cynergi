package com.cynergisuite.middleware.accounting.account.payable.recurring

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableRecurringInvoiceStatusTypeDTO
import com.cynergisuite.middleware.schedule.ScheduleValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "AccountPayableRecurringInvoice", title = "Account Payable Recurring Invoice", description = "Account payable recurring invoice entity")
data class AccountPayableRecurringInvoiceDTO(

   @field:Schema(description = "Account payable recurring invoice id")
   var id: UUID? = null,

   @field:NotNull
   @field:Schema(description = "Account payable recurring invoice vendor")
   var vendor: SimpleIdentifiableDTO? = null,

   @field:NotNull
   @field:Size(max = 20)
   @field:Schema(description = "Account payable recurring invoice", maxLength = 20)
   var invoice: String? = null,

   @field:NotNull
   @field:Schema(description = "Account payable recurring invoice amount")
   var invoiceAmount: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Account payable recurring invoice fixed amount indicator")
   var fixedAmountIndicator: Boolean? = null,

   @field:NotNull
   @field:Schema(description = "Account payable recurring invoice employee number")
   var employeeNumberId: Int? = null,

   @field:Schema(description = "Account payable recurring invoice message", required = false)
   var message: String? = null,

   @field:Size(max = 3)
   @field:Schema(description = "Account payable recurring invoice code indicator", required = false, maxLength = 3)
   var codeIndicator: String? = null,

   @field:NotNull
   @field:Size(max = 1)
   @field:Schema(description = "Account payable recurring invoice type, must be 'E' for now", maxLength = 1)
   var type: String? = null,

   @field:NotNull
   @field:Schema(description = "Account payable recurring invoice pay to")
   var payTo: SimpleIdentifiableDTO? = null,

   @field:Schema(description = "Account payable recurring invoice last transfer to create invoice date")
   var lastTransferToCreateInvoiceDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Account payable recurring invoice status type")
   var status: AccountPayableRecurringInvoiceStatusTypeDTO? = null,

   @field:NotNull
   @field:Schema(description = "Account payable recurring invoice due days")
   var dueDays: Int? = null,

   @field:NotNull
   @field:Schema(description = "Account payable recurring invoice automatic indicator")
   var automatedIndicator: Boolean? = null,

   @field:NotNull
   @field:Schema(description = "Account payable recurring invoice separate check indicator")
   var separateCheckIndicator: Boolean? = null,

   @field:NotNull
   @field:Schema(description = "Account payable recurring invoice expense month creation indicator")
   var expenseMonthCreationIndicator: ExpenseMonthCreationTypeDTO? = null,

   @field:NotNull
   @field:Schema(description = "Account payable recurring invoice invoice day")
   var invoiceDay: Int? = null,

   @field:NotNull
   @field:Schema(description = "Account payable recurring invoice expense day")
   var expenseDay: Int? = null,

   @field:Schema(description = "Account payable recurring invoice schedule", required = false)
   var schedule: ScheduleValueObject? = null,

   @field:Schema(description = "Account payable recurring invoice last created in period", required = false)
   var lastCreatedInPeriod: LocalDate? = null,

   @field:Schema(description = "Account payable recurring invoice next creation date", required = false)
   var nextCreationDate: LocalDate? = null,

   @field:Schema(description = "Account payable recurring invoice next invoice date", required = false)
   var nextInvoiceDate: LocalDate? = null,

   @field:Schema(description = "Account payable recurring invoice next expense date", required = false)
   var nextExpenseDate: LocalDate? = null

) : Identifiable {
   constructor(
      entity: AccountPayableRecurringInvoiceEntity,
      schedule: ScheduleValueObject? = null
   ) :
      this(
         id = entity.id,
         vendor = SimpleIdentifiableDTO(entity.vendor),
         invoice = entity.invoice,
         invoiceAmount = entity.invoiceAmount,
         fixedAmountIndicator = entity.fixedAmountIndicator,
         employeeNumberId = entity.employeeNumberId,
         message = entity.message,
         codeIndicator = entity.codeIndicator,
         type = entity.type,
         payTo = SimpleIdentifiableDTO(entity.payTo),
         lastTransferToCreateInvoiceDate = entity.lastTransferToCreateInvoiceDate,
         status = AccountPayableRecurringInvoiceStatusTypeDTO(entity.status),
         dueDays = entity.dueDays,
         automatedIndicator = entity.automatedIndicator,
         separateCheckIndicator = entity.separateCheckIndicator,
         expenseMonthCreationIndicator = ExpenseMonthCreationTypeDTO(entity.expenseMonthCreationIndicator),
         invoiceDay = entity.invoiceDay,
         expenseDay = entity.expenseDay,
         schedule = schedule,
         lastCreatedInPeriod = entity.lastCreatedInPeriod,
         nextCreationDate = entity.nextCreationDate,
         nextInvoiceDate = entity.nextInvoiceDate,
         nextExpenseDate = entity.nextExpenseDate
      )

   override fun myId(): UUID? = id
}
