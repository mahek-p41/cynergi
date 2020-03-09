package com.cynergisuite.middleware.store

import com.cynergisuite.middleware.region.RegionEntity
import org.apache.commons.lang3.builder.CompareToBuilder
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder

class RegionToStoreEntity(
   val region: RegionEntity,
   val store: StoreEntity
): Comparable<RegionToStoreEntity> {
   override fun compareTo(other: RegionToStoreEntity): Int =
      CompareToBuilder()
         .append(this.region, other.region)
         .append(this.store, other.store)
         .toComparison()

   override fun equals(other: Any?): Boolean =
      when {
         this === other -> {
            true
         }
         javaClass != other?.javaClass -> {
            false
         }
         other is RegionToStoreEntity -> {
            EqualsBuilder()
               .append(this.region, other.region)
               .append(this.store, other.store)
               .isEquals
         }
         else -> {
            false
         }
      }

   override fun hashCode(): Int =
      HashCodeBuilder()
         .append(this.region)
         .append(this.store)
         .toHashCode()

}
