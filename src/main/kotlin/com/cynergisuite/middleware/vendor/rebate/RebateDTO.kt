package com.cynergisuite.middleware.vendor.rebate

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountStatusTypeValueDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Digits
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "Rebate", title = "An entity containing rebate information", description = "An entity containing rebate information.")
data class RebateDTO(

   @field:Schema(name = "id", minimum = "1", required = false, description = "System generated ID")
   var id: UUID? = null,

   @field:Valid
   @field:Schema(name = "vendors", description = "List of vendors", required = false)
   var vendors: MutableList<SimpleIdentifiableDTO>? = null,

   @field:NotNull
   @field:Valid
   @field:Schema(name = "status", description = "Status type")
   var status: AccountStatusTypeValueDTO? = null,

   @field:NotNull
   @field:Size(min = 3, max = 30)
   @field:Schema(name = "description", description = "Description", minLength = 3, maxLength = 30)
   var description: String? = null,

   @field:NotNull
   @field:Valid
   @field:Schema(name = "rebate", description = "Rebate type")
   var type: RebateTypeDTO? = null,

   @field:DecimalMin(value = "0", inclusive = false)
   @field:DecimalMax("1")
   @field:Digits(integer = 1, fraction = 7)
   @field:Schema(name = "percent", description = "Percent", required = false)
   var percent: BigDecimal? = null,

   @field:DecimalMin(value = "0", inclusive = false)
   @field:Digits(integer = 11, fraction = 2)
   @field:Schema(name = "amountPerUnit", description = "Amount per unit", required = false)
   var amountPerUnit: BigDecimal? = null,

   @field:NotNull
   @field:Schema(name = "accrualIndicator", description = "Accrual indicator", example = "true", defaultValue = "false")
   var accrualIndicator: Boolean? = null,

   @field:Valid
   @field:Schema(name = "generalLedgerDebitAccount", description = "General ledger debit account, must not be null if accrualIndicator is true", required = false)
   var generalLedgerDebitAccount: AccountDTO?,

   @field:NotNull
   @field:Valid
   @field:Schema(name = "generalLedgerCreditAccount", description = "General ledger credit account")
   var generalLedgerCreditAccount: AccountDTO? = null

) : Identifiable {

   constructor(entity: RebateEntity, vendors: List<SimpleIdentifiableDTO>? = null) :
      this(
         id = entity.id,
         vendors = (vendors ?: entity.vendors?.map { SimpleIdentifiableDTO(it) }) as MutableList<SimpleIdentifiableDTO>,
         status = AccountStatusTypeValueDTO(entity.status),
         description = entity.description,
         type = RebateTypeDTO(entity.rebate),
         percent = entity.percent,
         amountPerUnit = entity.amountPerUnit,
         accrualIndicator = entity.accrualIndicator,
         generalLedgerDebitAccount = entity.generalLedgerDebitAccount?.let { AccountDTO(it) },
         generalLedgerCreditAccount = AccountDTO(entity.generalLedgerCreditAccount)
      )

   override fun myId(): UUID? = id
}
