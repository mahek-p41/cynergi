package com.cynergisuite.middleware.region

import com.cynergisuite.domain.Entity
import com.cynergisuite.middleware.division.DivisionEntity
import com.cynergisuite.middleware.employee.Employee
import org.apache.commons.lang3.builder.CompareToBuilder
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.time.OffsetDateTime
import java.util.*

data class RegionEntity (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val number: Int,
   val name: String,
   val employeeNumber: Int,
   val description: String,
   val manager: Employee? = null,
   val division: DivisionEntity? = null
) : Entity<RegionEntity> {

   fun compareTo(other: RegionEntity): Int =
      CompareToBuilder()
         .append(this.name, other.name)
         .append(this.number, other.number)
         .append(this.employeeNumber, other.employeeNumber)
         .append(this.description, other.description)
         .append(this.division, other.division)
         .toComparison()

   override fun equals(other: Any?): Boolean =
      when {
          this === other -> {
             true
          }
          javaClass != other?.javaClass -> {
             false
          }
          other is RegionEntity -> {
             EqualsBuilder()
                .append(this.id, other.id)
                .append(this.number, other.number)
                .append(this.name, other.name)
                .append(this.employeeNumber, other.employeeNumber)
                .append(this.description, other.description)
                .append(this.division, other.division)
                .isEquals
          }
          else -> {
             false
          }
      }

   override fun hashCode(): Int =
      HashCodeBuilder()
         .append(this.id)
         .append(this.number)
         .append(this.name)
         .append(this.employeeNumber)
         .append(this.description)
         .append(this.division)
         .toHashCode()

   override fun myId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): RegionEntity = copy()
}



