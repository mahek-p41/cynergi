package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.Company

data class GeneralLedgerSourceCodeEntity(
   val id: Long? = null,
   val company: Company,
   val value: String,
   val description: String
) : Identifiable {

   constructor(dto: GeneralLedgerSourceCodeDTO, company: Company) :
      this(
         id = dto.id,
         company = company,
         value = dto.value!!,
         description = dto.description!!
      )

   override fun myId(): Long? = id
}
