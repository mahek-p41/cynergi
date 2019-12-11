package com.cynergisuite.middleware.schedule.infrastructure

import com.cynergisuite.domain.DataTransferObject
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.PageRequestBase
import com.cynergisuite.domain.ValidPageSortBy
import io.swagger.v3.oas.annotations.media.Schema
import java.lang.StringBuilder
import javax.validation.constraints.Size

@DataTransferObject
@Schema(
   name = "SchedulePageRequest",
   title = "How to query for a paged set of scheduled items",
   description = "This is the form of the URL parameters that can be used to query for a subset of a larger dataset. Example: ?page=1&size=10&sortBy=id&sortDirection=ASC&command=SOMECOMAND",
   allOf = [PageRequestBase::class]
)
class SchedulePageRequest(
   page: Int, size: Int, sortBy: String, sortDirection: String,

   @field:Size(min = 3, max = 25)
   var command: String? = null

) : PageRequestBase<SchedulePageRequest>(page, size, sortBy, sortDirection) {

   constructor(pageRequest: PageRequest, command: String):
      this(
         page = pageRequest.page(),
         size = pageRequest.size(),
         sortBy = pageRequest.sortBy(),
         sortDirection = pageRequest.sortDirection(),
         command = command
      )

   override fun myNextPage(page: Int, size: Int, sortBy: String, sortDirection: String): SchedulePageRequest =
      SchedulePageRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         command = this.command
      )

   @ValidPageSortBy("id")
   override fun sortByMe(): String = sortBy()
   override fun myToString(stringBuilder: StringBuilder, separatorIn: String) {
      command?.also { stringBuilder.append(separatorIn).append("command=").append(command) }
   }
}
