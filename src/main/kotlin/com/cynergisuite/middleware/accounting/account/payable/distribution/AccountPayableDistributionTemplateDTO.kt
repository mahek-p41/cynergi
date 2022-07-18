package com.cynergisuite.middleware.accounting.account.payable.distribution

import com.cynergisuite.domain.Identifiable
import com.fasterxml.jackson.annotation.JsonInclude
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "AccountPayableDistributionTemplate", title = "A data transfer object containing an account payable distribution template", description = "A data transfer object containing an account payable distribution template.")
data class AccountPayableDistributionTemplateDTO(

   @field:Schema(description = "Account Payable Distribution Template id")
   var id: UUID? = null,

   @field:NotNull
   @field:Size(max = 10)
   @field:Schema(name = "name", description = "Description for an account payable distribution template.", maxLength = 10)
   var name: String? = null,

) : Identifiable {

   constructor(entity: AccountPayableDistributionTemplateEntity) :
      this(
         id = entity.id,
         name = entity.name
      )

   override fun myId(): UUID? = id
}
