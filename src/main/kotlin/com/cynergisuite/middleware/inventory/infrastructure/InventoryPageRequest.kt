package com.cynergisuite.middleware.inventory.infrastructure

import com.cynergisuite.domain.PageRequest
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.lang.StringBuilder
import javax.validation.constraints.Min
import javax.validation.constraints.Positive

class InventoryPageRequest(pageRequest: PageRequest) : PageRequest(pageRequest) {

   @field:Positive
   @field:Min(1)
   @field:Schema(minimum = "1", description = "The Store Number to filter results with")
   var storeNumber: Int? = null

   @field:Schema(description = "Set of inventory statues to be queried for.  Possible values are N, O, R, D")
   var inventoryStatus: List<String>? = null

   constructor(pageRequest: InventoryPageRequest, storeNumber: Int) : this(pageRequest) {
      this.storeNumber = storeNumber
      this.inventoryStatus = pageRequest.inventoryStatus
   }

   constructor(pageRequest: PageRequest, storeNumber: Int): this(pageRequest) {
      this.storeNumber = storeNumber
   }

   constructor(pageRequest: PageRequest, storeNumber: Int, inventoryStatus: List<String>): this(pageRequest) {
      this.storeNumber = storeNumber
      this.inventoryStatus = inventoryStatus
   }

   override fun equals(other: Any?): Boolean =
      if (other is InventoryPageRequest) {
         EqualsBuilder()
            .appendSuper(super.equals(other))
            .append(this.storeNumber, other.storeNumber)
            .append(this.inventoryStatus, other.inventoryStatus)
            .isEquals
      } else {
         false
      }

   override fun hashCode(): Int =
      HashCodeBuilder()
         .appendSuper(super.hashCode())
         .append(this.storeNumber)
         .append(this.inventoryStatus)
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

      return stringBuilder.toString()
   }
}
