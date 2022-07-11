package com.cynergisuite.middleware.accounting.account.payable.distribution

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.fasterxml.jackson.annotation.JsonInclude
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.UUID
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Digits
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "AccountPayableDistributionDetail", title = "A data transfer object containing an account payable distribution detail", description = "A data transfer object containing an account payable distribution detail.")
data class AccountPayableDistributionDetailDTO(

   @field:Schema(description = "Account Payable Distribution Detail id")
   var id: UUID? = null,

   @field:NotNull
   @field:Schema(name = "profitCenter", description = "Profit center")
   var profitCenter: SimpleLegacyIdentifiableDTO? = null,

   @field:NotNull
   @field:Schema(name = "account", description = "Account")
   var account: AccountDTO? = null,

   @field:NotNull
   @field:DecimalMin(value = "0", inclusive = false)
   @field:DecimalMax("1")
   @field:Digits(integer = 1, fraction = 7)
   @field:Schema(name = "percent", description = "Percent")
   var percent: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Account payable distribution template")
   var distributionTemplate: AccountPayableDistributionTemplateDTO? = null,

   ) : Identifiable {

   constructor(entity: AccountPayableDistributionDetailEntity) :
      this(
         id = entity.id,
         profitCenter = SimpleLegacyIdentifiableDTO(entity.profitCenter.myId()),
         account = AccountDTO(entity.account),
         percent = entity.percent,
         distributionTemplate = AccountPayableDistributionTemplateDTO(entity.distributionTemplate)
      )

   override fun myId(): UUID? = id
}
