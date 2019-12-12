package com.cynergisuite.middleware.inventory.infrastructure

import com.cynergisuite.domain.PageRequestBase
import com.cynergisuite.domain.ValidPageSortBy
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import javax.validation.constraints.Min
import javax.validation.constraints.Positive

@Schema(
   name = "InventoryPageRequest",
   title = "Specialized paging for Inventory listing requests",
   description = "Defines the parameters available to for a paging request to the inventory-fetchAll endpoint. Example ?page=1&size=10&sortBy=id&sortDirection=ASC&storeNumber=1&inventoryStatus=N&inventoryStatus=O&inventoryStatus=R&inventoryStatus=D&locationType=STORE",
   allOf = [PageRequestBase::class]
)
class InventoryPageRequest(
   page: Int?, size: Int?, sortBy: String?, sortDirection: String?,

   @field:Positive
   @field:Min(1)
   @field:Schema(name = "storeNumber", minimum = "1", description = "The Store Number to filter results with")
   var storeNumber: Int? = null,

   @field:Schema(name = "inventoryStatus", description = "Set of inventory statues to be queried for", allowableValues = ["N", "O", "R", "D"], required = false, nullable = true)
   var inventoryStatus: Set<String>? = setOf("N", "R"),

   @field:Schema(name = "locationType", description = "Allows for choosing where the inventory is located to be chosen.  If this property is not filled out then all items are returned", allowableValues = ["STORE", "WAREHOUSE", "PEDNING", "CUSTOM", "LOANER", "SERVICE", "STOLEN", "CHARGEOFF"], required = false, nullable = true)
   var locationType: String? = null

) : PageRequestBase<InventoryPageRequest>(page, size, sortBy, sortDirection) {

   constructor(pageRequest: InventoryPageRequest, storeNumber: Int):
      this(
         page = pageRequest.page(),
         size = pageRequest.size(),
         sortBy = pageRequest.sortBy(),
         sortDirection = pageRequest.sortDirection(),
         storeNumber = storeNumber,
         inventoryStatus = pageRequest.inventoryStatus,
         locationType = pageRequest.locationType
      )

   override fun myNextPage(page: Int, size: Int, sortBy: String, sortDirection: String): InventoryPageRequest =
      InventoryPageRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         storeNumber = this.storeNumber,
         inventoryStatus = this.inventoryStatus,
         locationType = this.locationType
      )

   @ValidPageSortBy("id")
   override fun sortByMe(): String = sortBy()

   override fun myToString(stringBuilder: StringBuilder, separatorIn: String) {
      val storeNumber = this.storeNumber
      val inventoryStatus = this.inventoryStatus
      var separator = separatorIn

      separator = storeNumber?.apply { stringBuilder.append(separator).append("storeNumber=").append(this) }.let { "&" }

      if ( !inventoryStatus.isNullOrEmpty() ) {
         inventoryStatus.joinTo(stringBuilder, "${separator}inventoryStatus=", "&inventoryStatus=")
      }

      if ( !locationType.isNullOrEmpty() ) {
         stringBuilder.append(separator).append("locationType=").append(locationType)
      }
   }

   override fun equals(other: Any?): Boolean =
      if (other is InventoryPageRequest) {
         EqualsBuilder()
            .appendSuper(super.equals(other))
            .append(this.storeNumber, other.storeNumber)
            .append(this.inventoryStatus, other.inventoryStatus)
            .append(this.locationType, other.locationType)
            .isEquals
      } else {
         false
      }

   override fun hashCode(): Int =
      HashCodeBuilder()
         .appendSuper(super.hashCode())
         .append(this.storeNumber)
         .append(this.inventoryStatus)
         .append(this.locationType)
         .toHashCode()

}
