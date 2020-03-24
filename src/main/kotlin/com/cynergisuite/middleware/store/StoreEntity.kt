package com.cynergisuite.middleware.store

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.location.Location
import com.cynergisuite.middleware.region.RegionEntity
import org.apache.commons.lang3.builder.CompareToBuilder
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder

data class StoreEntity(
   val id: Long,
   val number: Int,
   val name: String,
   val company: Company,
   val region: RegionEntity? = null

) : Location, Comparable<StoreEntity> {

   constructor(store: StoreValueObject, company: Company) :
      this(
         id = store.id,
         number = store.number!!,
         name = store.name!!,
         company = company
      )

   constructor(store: StoreValueObject, company: Company, region: RegionEntity?) :
      this(
         id = store.id,
         number = store.number!!,
         name = store.name!!,
         company = company,
         region = region
      )

   override fun myId(): Long = id
   override fun myNumber(): Int = number
   override fun myName(): String = name
   override fun myCompany(): Company = company

   override fun hashCode(): Int =
      HashCodeBuilder()
         .append(this.id)
         .append(this.number)
         .append(this.name)
         .append(this.company)
         .toHashCode()

   override fun equals(other: Any?): Boolean =
      if (other is StoreEntity) {
         EqualsBuilder()
            .append(this.id, other.id)
            .append(this.number, other.number)
            .append(this.name, other.name)
            .append(this.company, other.company)
            .isEquals
      } else {
         false
      }

   override fun compareTo(other: StoreEntity): Int =
      CompareToBuilder()
         .append(this.id, other.id)
         .append(this.number, other.number)
         .append(this.name, other.name)
         .append(this.company, other.company)
         .toComparison()
}
