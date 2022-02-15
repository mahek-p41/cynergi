package com.cynergisuite.middleware.accounting.account.payable.distribution

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
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
@Schema(name = "AccountPayableDistribution", title = "A data transfer object containing an account payable distribution", description = "A data transfer object containing an account payable distribution.")
data class AccountPayableDistributionDTO(

   var id: UUID? = null,

   @field:NotNull
   @field:Size(max = 10)
   @field:Schema(name = "name", description = "Description for an account payable distribution.", maxLength = 10)
   var name: String? = null,

   @field:NotNull
   @field:Schema(name = "profitCenter", description = "Profit center")
   var profitCenter: SimpleLegacyIdentifiableDTO? = null,

   @field:NotNull
   @field:Schema(name = "account", description = "Account")
   var account: SimpleIdentifiableDTO? = null,

   @field:NotNull
   @field:DecimalMin(value = "0", inclusive = false)
   @field:DecimalMax("1")
   @field:Digits(integer = 1, fraction = 7)
   @field:Schema(name = "percent", description = "Percent")
   var percent: BigDecimal? = null

) : Identifiable {

   constructor(entity: AccountPayableDistributionEntity) :
      this(
         id = entity.id,
         name = entity.name,
         profitCenter = SimpleLegacyIdentifiableDTO(entity.profitCenter.myId()),
         account = SimpleIdentifiableDTO(entity.account.id),
         percent = entity.percent
      )

   override fun myId(): UUID? = id
}
