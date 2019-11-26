package com.cynergisuite.middleware.company

import com.cynergisuite.domain.Identifiable
import org.apache.commons.lang3.builder.CompareToBuilder
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.time.OffsetDateTime

data class CompanyEntity(
   val id: Long,
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val number: Int,
   val name: String,
   val dataset: String
) : Identifiable, Comparable<CompanyEntity> {

   constructor(company: CompanyValueObject) :
      this(
         id = company.id,
         number = company.number!!,
         name = company.name!!,
         dataset = company.dataset!!
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
      if (other is CompanyEntity) {
         EqualsBuilder()
            .append(this.id, other.id)
            .append(this.number, other.number)
            .append(this.name, other.name)
            .append(this.dataset, other.dataset)
            .isEquals
      } else {
         false
      }

   override fun compareTo(other: CompanyEntity): Int =
      CompareToBuilder()
         .append(this.id, other.id)
         .append(this.number, other.number)
         .append(this.name, other.name)
         .append(this.dataset, other.dataset)
         .toComparison()
}
