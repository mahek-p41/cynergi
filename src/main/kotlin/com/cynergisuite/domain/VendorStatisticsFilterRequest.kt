package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.constraints.NotNull

@Schema(
   name = "VendorStatisticsFilterRequest",
   title = "Vendor Statistics Filter Request",
   description = "Filter request for Vendor Statistics",
   allOf = [PageRequestBase::class]
)
@Introspected
class VendorStatisticsFilterRequest(
   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @field:NotNull
   @field:Schema(name = "vendorId", description = "Vendor ID")
   var vendorId: UUID? = null

) : PageRequestBase<VendorStatisticsFilterRequest>(page, size, sortBy, sortDirection) {

   override fun sortByMe(): String = sortBy()

   override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): VendorStatisticsFilterRequest =
      VendorStatisticsFilterRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         vendorId = this.vendorId
      )

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "vendorId" to vendorId
      )
}