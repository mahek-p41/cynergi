package com.cynergisuite.middleware.schedule.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.ValidPageSortBy
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import javax.annotation.Nullable

@Schema(
   name = "SchedulePageRequest",
   title = "Specialized paging for listing schedule",
   description = "Defines the parameters available to for a paging request to the schedule-fetchAll endpoint. Example ?page=1&size=10&sortBy=id&sortDirection=ASC"
)
class SchedulePageRequest(pageRequest: PageRequest) : PageRequest(pageRequest) {

   @field:Schema(description = "The Title of Schedule")
   var title: String = "X"

   @field:Nullable
   @field:Schema(description = "The Description of Schedule")
   var description: String? = null

   @field:Schema(description = "The Schedule Name")
   var schedule: String = "x"

   @field:Schema(description = "The Command to run")
   var command: String = "x"

   @field:Schema(description = "The Type of Schedule")
   var type: String = "x"

   @ValidPageSortBy("id")
   override fun sortByMe(): String = sortBy

   override fun equals(other: Any?): Boolean =
      if (other is SchedulePageRequest) {
         EqualsBuilder()
            .appendSuper(super.equals(other))
            .append(this.title, other.title)
            .append(this.description, other.description)
            .append(this.schedule, other.schedule)
            .append(this.command, other.command)
            .append(this.type, other.type)
            .isEquals
      } else {
         false
      }

   override fun hashCode(): Int =
      HashCodeBuilder()
         .appendSuper(super.hashCode())
         .append(this.title)
         .append(this.description)
         .append(this.schedule)
         .append(this.command)
         .append(this.type)
         .toHashCode()

   override fun toString(): String {
      val stringBuilder = StringBuilder(super.toString())
      val title = this.title
      val description = this.description
      val schedule = this.schedule
      val command = this.command
      //val type = this.type

      if (title != null) {
         stringBuilder.append("&title=").append(title)
      }

      if (description != null) {
         stringBuilder.append("&description=").append(description)
      }

      if (schedule != null) {
         stringBuilder.append("&schedule=").append(schedule)
      }

      if (command != null) {
         stringBuilder.append("&command=").append(command)
      }

      return stringBuilder.toString()
   }
}
