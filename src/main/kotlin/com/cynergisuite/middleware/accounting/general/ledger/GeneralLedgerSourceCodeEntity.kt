package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.domain.Identifiable

data class GeneralLedgerSourceCodeEntity(
   val id: Long? = null,
   val value: String,
   val description: String
) : Identifiable {

   constructor(dto: GeneralLedgerSourceCodeDTO) :
      this(
         id = dto.id,
         value = dto.value!!,
         description = dto.description!!
      )

   override fun myId(): Long? = id
}
