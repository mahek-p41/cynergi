package com.cynergisuite.middleware.inventory.infrastructure

import com.cynergisuite.domain.PageRequest
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.lang.StringBuilder
import javax.validation.constraints.Min
import javax.validation.constraints.Positive

@Schema(
   name = "InventoryPageRequest",
   title = "Specialized paging for Inventory listing requests",
   description = "Defines the parameters available to for a paging request to the inventory-fetchAll endpoint. Example ?page=1&size=10&sortBy=id&sortDirection=ASC&storeNumber=1&inventoryStatus=N&inventoryStatus=O&inventoryStatus=R&inventoryStatus=D&locationType=STORE"
)
class InventoryPageRequest(pageRequest: PageRequest) : PageRequest(pageRequest) {

   @field:Positive
   @field:Min(1)
   @field:Schema(name = "storeNumber", minimum = "1", description = "The Store Number to filter results with")
   var storeNumber: Int? = null

   @field:Schema(name = "inventoryStatus", description = "Set of inventory statues to be queried for", allowableValues = ["N", "O", "R", "D"], required = false, nullable = true)
   var inventoryStatus: List<String>? = null

   @field:Schema(name = "locationType", description = "Allows for choosing where the inventory is located to be chosen.  If this property is not filled out then all items are returned", allowableValues = ["STORE", "WAREHOUSE", "PEDNING", "CUSTOM", "LOANER", "SERVICE", "STOLEN", "CHARGEOFF"], required = false, nullable = true)
   var locationType: String? = null

   constructor(pageRequest: InventoryPageRequest, storeNumber: Int) : this(pageRequest) {
      this.storeNumber = storeNumber
      this.inventoryStatus = pageRequest.inventoryStatus
      this.locationType = pageRequest.locationType
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

   override fun toString(): String {
      val stringBuilder = StringBuilder(super.toString())
      val storeNumber = this.storeNumber
      val inventoryStatus = this.inventoryStatus

      if (storeNumber != null) {
         stringBuilder.append("&storeNumber=").append(storeNumber)
      }

      if ( !inventoryStatus.isNullOrEmpty() ) {
         inventoryStatus.joinTo(stringBuilder, "&inventoryStatus=", "&inventoryStatus=")
      }

      if ( !locationType.isNullOrEmpty() ) {
         stringBuilder.append("&locationType=").append(locationType)
      }

      return stringBuilder.toString()
   }
}
