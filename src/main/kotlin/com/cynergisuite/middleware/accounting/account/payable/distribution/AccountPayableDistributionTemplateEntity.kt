package com.cynergisuite.middleware.accounting.account.payable.distribution

import com.cynergisuite.domain.Identifiable
import java.util.UUID

data class AccountPayableDistributionTemplateEntity(
   val id: UUID? = null,
   val name: String,
) : Identifiable {

   constructor(
      dto: AccountPayableDistributionTemplateDTO) :
      this(
         id = dto.id,
         name = dto.name!!,
      )

   override fun myId(): UUID? = id
}
