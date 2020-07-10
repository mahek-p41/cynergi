package com.cynergisuite.middleware.employee

import com.cynergisuite.domain.PageRequestBase
import com.cynergisuite.domain.ValidPageSortBy
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
   name = "EmployeePageRequest",
   title = "Specialized paging for listing employees",
   description = "Defines the parameters available to for a paging request to the employee-fetchAll endpoint. Example ?page=1&size=10&sortBy=id&sortDirection=ASC&firstNameMi=Alan&lastName=Smith&search=ASmth",
   allOf = [PageRequestBase::class]
)
class EmployeePageRequest(
   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @field:Schema(name = "firstNameMi", description = "First name & middle name initial")
   var firstNameMi: String? = null,

   @field:Schema(name = "lastName", description = "Last name initial")
   var lastName: String? = null,

   @field:Schema(name = "search", description = "Fuzzy search string")
   var search: String? = null

) : PageRequestBase<EmployeePageRequest>(page, size, sortBy, sortDirection) {

   constructor(pageRequest: EmployeePageRequest) :
      this(
         page = pageRequest.page,
         size = pageRequest.size,
         sortBy = pageRequest.sortBy,
         sortDirection = pageRequest.sortDirection,
         firstNameMi = pageRequest.firstNameMi,
         lastName = pageRequest.lastName,
         search = pageRequest.search
      )

   @ValidPageSortBy("id", "firstNameMi", "lastName")
   override fun sortByMe(): String = sortBy()

   override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): EmployeePageRequest =
      EmployeePageRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         firstNameMi = firstNameMi,
         lastName = lastName,
         search = search
      )

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "firstNameMi" to firstNameMi,
         "lastName" to lastName,
         "search" to search
      )

}
