package com.cynergisuite.middleware.division

import com.cynergisuite.domain.Entity
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.employee.Employee
import org.apache.commons.lang3.builder.CompareToBuilder
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.time.OffsetDateTime
import java.util.*

data class DivisionEntity (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val number: Int,
   val name: String,
   val description: String,
   val manager: Employee? = null,
   var company: CompanyEntity? = null
) : Entity<DivisionEntity> {

   fun compareTo(other: DivisionEntity): Int =
      CompareToBuilder()
         .append(this.number, other.number)
         .append(this.name, other.name)
         .append(this.manager, other.manager)
         .append(this.description, other.description)
         .append(this.company, other.company)
         .toComparison()

   override fun equals(other: Any?): Boolean =
      when {
         this === other -> {
            true
         }
         javaClass != other?.javaClass -> {
            false
         }
         other is DivisionEntity -> {
            EqualsBuilder()
               .append(this.id, other.id)
               .append(this.number, other.number)
               .append(this.name, other.name)
               .append(this.manager, other.manager)
               .append(this.description, other.description)
               .append(this.company, other.company)
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
         .append(this.manager)
         .append(this.description)
         .append(this.company)
         .toHashCode()

}
