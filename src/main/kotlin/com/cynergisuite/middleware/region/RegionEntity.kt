package com.cynergisuite.middleware.region

import com.cynergisuite.domain.Entity
import com.cynergisuite.middleware.division.DivisionEntity
import com.cynergisuite.middleware.employee.Employee
import org.apache.commons.lang3.builder.CompareToBuilder
import java.time.OffsetDateTime
import java.util.UUID

data class RegionEntity (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val number: Int,
   val name: String,
   val description: String,
   val division: DivisionEntity
) : Entity<RegionEntity>, Comparable<RegionEntity> {

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
         number = this.number,
         name = this.name,
         description = this.description,
         division = this.division.toValueObject()
      )
   }

   override fun myId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): RegionEntity = copy()
}



