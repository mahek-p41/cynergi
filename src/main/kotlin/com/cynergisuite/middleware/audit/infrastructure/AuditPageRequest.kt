package com.cynergisuite.middleware.audit.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.ValidPageSortBy
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.time.OffsetDateTime
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Schema(
   name = "AuditPageRequest",
   title = "Specialized paging for listing audits",
   description = "Defines the parameters available to for a paging request to the audit-fetchAll endpoint. Example ?page=1&size=10&sortBy=id&sortDirection=ASC&storeNumber=1&status=OPENED&status=IN-PROGRESS"
)
class AuditPageRequest(pageRequest: PageRequest) : PageRequest(pageRequest) {

   @field:Schema(name = "from", description = "Bottom end of the range which will be used to filter audits.  If from is found thru is required.  If both from and thru are empty then the result will include all audits")
   var from: OffsetDateTime? = null

   @field:Schema(name = "thru", description = "Top end of the range which will be used to filter audits.  If from is found thru is required.  If both from and thru are empty then the result will include all audits")
   var thru: OffsetDateTime? = null

   @field:Positive
   @field:Min(1)
   @field:Schema(minimum = "1", description = "The Store Number to filter results with")
   var storeNumber: Int? = null

   @field:Schema(name = "status", description = "Collection of statues that an audit must be in")
   var status: Set<String>? = emptySet()

   constructor(pageRequest: AuditPageRequest?, from: OffsetDateTime?, thru: OffsetDateTime?, status: Set<String>) : this(pageRequest ?: PageRequest()) {
      this.from = from
      this.thru = thru
      this.storeNumber = pageRequest?.storeNumber
      this.status = status.toSet()
   }

   @ValidPageSortBy("id", "storeNumber")
   override fun sortByMe(): String = sortBy

   override fun equals(other: Any?): Boolean =
      if (other is AuditPageRequest) {
         EqualsBuilder()
            .appendSuper(super.equals(other))
            .append(this.storeNumber, other.storeNumber)
            .append(this.status, other.status)
            .isEquals
      } else {
         false
      }

   override fun hashCode(): Int =
      HashCodeBuilder()
         .appendSuper(super.hashCode())
         .append(this.storeNumber)
         .append(this.status)
         .toHashCode()

   override fun toString(): String {
      val stringBuilder = StringBuilder(super.toString())
      val storeNumber = this.storeNumber
      val status = this.status

      if (from != null) {
         stringBuilder.append("&from=").append(from)
      }

      if (thru != null) {
         stringBuilder.append("&thru=").append(thru)
      }

      if (storeNumber != null) {
         stringBuilder.append("&storeNumber=").append(storeNumber)
      }

      if ( !status.isNullOrEmpty() ) {
         stringBuilder.append(status.joinToString(separator = "&status=", prefix = "&status="))
      }

      return stringBuilder.toString()
   }
}
