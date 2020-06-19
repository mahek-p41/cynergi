package com.cynergisuite.middleware.audit.infrastructure

import com.cynergisuite.domain.PageRequestBase
import com.cynergisuite.domain.ValidPageSortBy
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.time.OffsetDateTime

@Schema(
   name = "AuditPageRequest",
   title = "Specialized paging for listing audits",
   description = "Defines the parameters available to for a paging request to the audit-fetchAll endpoint. Example ?page=1&size=10&sortBy=id&sortDirection=ASC&storeNumber=1&status=CREATED&status=IN-PROGRESS",
   allOf = [PageRequestBase::class]
)
class AuditPageRequest(
   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @field:Schema(name = "from", description = "Bottom end of the range which will be used to filter audits.  If from is found thru is required.  If both from and thru are empty then the result will include all audits")
   var from: OffsetDateTime? = null,

   @field:Schema(name = "thru", description = "Top end of the range which will be used to filter audits.  If from is found thru is required.  If both from and thru are empty then the result will include all audits")
   var thru: OffsetDateTime? = null,

   @field:Schema(name = "storeNumber", description = "The collection of Store Numbers to filter results with")
   var storeNumber: Set<Int>? = emptySet(),

   @field:Schema(name = "status", description = "Collection of statues that an audit must be in")
   var status: Set<String>? = emptySet()

) : PageRequestBase<AuditPageRequest>(page, size, sortBy, sortDirection) {

   @ValidPageSortBy("id", "storeNumber")
   override fun sortByMe(): String = sortBy()

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

   protected override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): AuditPageRequest =
      AuditPageRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         from = this.from,
         thru = this.thru,
         storeNumber = this.storeNumber,
         status = this.status
      )

   protected override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "from" to from,
         "thru" to thru,
         "storeNumber" to storeNumber,
         "status" to status
      )
}
