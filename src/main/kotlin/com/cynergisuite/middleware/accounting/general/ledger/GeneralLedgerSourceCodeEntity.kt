package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.domain.Identifiable
import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class GeneralLedgerSourceCodeEntity(
   val id: UUID? = null,
   val value: String,
   val description: String
) : Identifiable {

   constructor(dto: GeneralLedgerSourceCodeDTO) :
      this(
         id = dto.id,
         value = dto.value!!,
         description = dto.description!!
      )

   override fun myId(): UUID? = id
}
