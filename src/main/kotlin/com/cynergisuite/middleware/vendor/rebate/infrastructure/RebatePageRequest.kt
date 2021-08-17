package com.cynergisuite.middleware.vendor.rebate.infrastructure

import com.cynergisuite.domain.PageRequestBase
import com.cynergisuite.domain.ValidPageSortBy
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.util.*

@Schema(
   name = "RebatePageRequest",
   title = "Specialized paging for listing rebates",
   description = "Defines the parameters available to for a paging request to the rebate-fetchAll endpoint. Example ?page=1&size=10&sortBy=id&sortDirection=ASC&vendorIds=uuid-1&vendorIds=uuid-2",
   allOf = [PageRequestBase::class]
)
class RebatePageRequest(
   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @field:Schema(name = "vendorIds", description = "The collection of vendor IDs to filter results with")
   var vendorIds: Set<UUID>? = emptySet(),

   ) : PageRequestBase<RebatePageRequest>(page, size, sortBy, sortDirection) {

   @ValidPageSortBy("id", "storeNumber")
   override fun sortByMe(): String = sortBy()

   override fun equals(other: Any?): Boolean =
      if (other is RebatePageRequest) {
         EqualsBuilder()
            .appendSuper(super.equals(other))
            .append(this.vendorIds, other.vendorIds)
            .isEquals
      } else {
         false
      }

   override fun hashCode(): Int =
      HashCodeBuilder()
         .appendSuper(super.hashCode())
         .append(this.vendorIds)
         .toHashCode()

   protected override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): RebatePageRequest =
      RebatePageRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         vendorIds = this.vendorIds,
      )

   protected override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "vendorIds" to vendorIds
      )
}
