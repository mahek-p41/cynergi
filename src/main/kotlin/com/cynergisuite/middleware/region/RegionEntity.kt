package com.cynergisuite.middleware.region

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.division.DivisionEntity
import org.apache.commons.lang3.builder.CompareToBuilder

data class RegionEntity (
   val id: Long? = null,
   val number: Int,
   val name: String,
   val description: String?,
   val division: DivisionEntity
) : Identifiable, Comparable<RegionEntity> {

   override fun compareTo(other: RegionEntity): Int =
      CompareToBuilder()
         .append(this.name, other.name)
         .append(this.number, other.number)
         .append(this.description, other.description)
         .append(this.division, other.division)
         .toComparison()

   fun toValueObject(): RegionValueObject {
      return RegionValueObject(
         id = this.id,
         name = this.name,
         description = this.description,
         division = this.division.toValueObject()
      )
   }

   override fun myId(): Long? = id
}



