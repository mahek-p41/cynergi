package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
   name = "AccountFilterRequest",
   title = "Account Filter Request",
   description = "Filter request for Account",
   allOf = [PageRequestBase::class]
)
@Introspected
class AccountFilterRequest(
   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @field:Schema(name = "accountType", description = "Account Type")
   var accountType: String? = null,


) : PageRequestBase<AccountFilterRequest>(page, size, sortBy, sortDirection) {

   override fun sortByMe(): String = sortBy()

   override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): AccountFilterRequest =
      AccountFilterRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         accountType = accountType
      )

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "accountType" to accountType
      )
}
