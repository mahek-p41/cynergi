package com.cynergisuite.middleware.schedule.infrastructure

import com.cynergisuite.domain.DataTransferObject
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.ValidPageSortBy
import javax.validation.constraints.Size

@DataTransferObject
class SchedulePageRequest(
   page: Int, size: Int, sortBy: String, sortDirection: String,

   @field:Size(min = 3, max = 25)
   var command: String? = null

) : PageRequest(page, size, sortBy, sortDirection) {

   constructor(pageRequest: PageRequest, command: String = "AuditSchedule"):
      this(
         page = pageRequest.page,
         size = pageRequest.size,
         sortBy = pageRequest.sortBy,
         sortDirection = pageRequest.sortDirection,
         command = command
      )

   @ValidPageSortBy("id")
   override fun sortByMe(): String = sortBy

   override fun myNextPage(page: Int, size: Int, sortBy: String, sortDirection: String): SchedulePageRequest =
      SchedulePageRequest(page, size, sortBy, sortDirection, this.command)
}
