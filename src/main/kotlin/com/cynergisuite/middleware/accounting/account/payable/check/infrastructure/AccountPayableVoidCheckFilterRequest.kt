package com.cynergisuite.middleware.accounting.account.payable.check.infrastructure

import com.cynergisuite.domain.SortableRequestBase
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
   name = "AccountPayableVoidCheckFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available for a sortable request.",
   allOf = [SortableRequestBase::class]
)
class AccountPayableVoidCheckFilterRequest(
   sortBy: String? = null,
   sortDirection: String? = null,
   @field:Schema(name = "bank", description = "Bank number")
   var bank: Long,

   @field:Schema(name = "checkNumber", description = "Check Number")
   var checkNumber: String,
) : SortableRequestBase<AccountPayableVoidCheckFilterRequest>(sortBy, sortDirection) {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "bank" to bank,
         "checkNumber" to checkNumber,
      )
}
