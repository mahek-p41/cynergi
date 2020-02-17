package com.cynergisuite.middleware.inventory.location

import com.cynergisuite.domain.TypeDomainEntity
import org.apache.commons.lang3.builder.HashCodeBuilder

data class InventoryLocationType(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<InventoryLocationType> {
   private val myHashCode: Int = HashCodeBuilder()
      .append(id)
      .append(value)
      .append(description)
      .append(localizationCode)
      .toHashCode()

   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode

   override fun hashCode(): Int = myHashCode

   override fun equals(other: Any?): Boolean =
      if (other is InventoryLocationType) {
         basicEquality(other)
      } else {
         false
      }
}
