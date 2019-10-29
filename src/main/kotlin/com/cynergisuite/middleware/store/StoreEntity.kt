package com.cynergisuite.middleware.store

import com.cynergisuite.domain.Identifiable
import org.apache.commons.lang3.builder.CompareToBuilder
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.time.OffsetDateTime

data class StoreEntity(
   val id: Long,
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val number: Int,
   val name: String,
   val dataset: String
) : Identifiable, Comparable<StoreEntity> {

   constructor(store: StoreValueObject) :
      this(
         id = store.id,
         number = store.number!!,
         name = store.name!!,
         dataset = store.dataset!!
      )

   override fun myId(): Long? = id

   override fun hashCode(): Int =
      HashCodeBuilder()
         .append(this.id)
         .append(this.number)
         .append(this.name)
         .append(this.dataset)
         .toHashCode()

   override fun equals(other: Any?): Boolean =
      if (other is StoreEntity) {
         EqualsBuilder()
            .append(this.id, other.id)
            .append(this.number, other.number)
            .append(this.name, other.name)
            .append(this.dataset, other.dataset)
            .isEquals
      } else {
         false
      }

   override fun compareTo(other: StoreEntity): Int =
      CompareToBuilder()
         .append(this.id, other.id)
         .append(this.number, other.number)
         .append(this.name, other.name)
         .append(this.dataset, other.dataset)
         .toComparison()
}
