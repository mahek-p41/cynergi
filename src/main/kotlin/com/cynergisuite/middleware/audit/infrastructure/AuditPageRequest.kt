package com.cynergisuite.middleware.audit.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.ValidPageSortBy
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import javax.annotation.Nullable
import javax.validation.constraints.Min
import javax.validation.constraints.Positive

@Schema(
   name = "AuditPageRequest",
   title = "Specialized paging for listing audits",
   description = "Defines the parameters available to for a paging request to the audit-fetchAll endpoint. Example ?page=1&size=10&sortBy=id&sortDirection=ASC&storeNumber=1&status=OPENED"
)
class AuditPageRequest(pageRequest: PageRequest) : PageRequest(pageRequest) {

   @field:Nullable
   @field:Positive
   @field:Min(1)
   @field:Schema(minimum = "1", description = "The Store Number to filter results with")
   var storeNumber: Int? = null

   @field:Nullable
   @field:Schema(description = "One of the available statuses for an Audit provided by the system")
   var status: String? = null

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

      if (storeNumber != null) {
         stringBuilder.append("&storeNumber=").append(storeNumber)
      }

      if (status != null) {
         stringBuilder.append("&status=").append(status)
      }

      return stringBuilder.toString()
   }
}
