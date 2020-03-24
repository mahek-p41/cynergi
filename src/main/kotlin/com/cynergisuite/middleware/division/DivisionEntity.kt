package com.cynergisuite.middleware.division

import com.cynergisuite.domain.Entity
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.employee.Employee
import org.apache.commons.lang3.builder.CompareToBuilder
import java.time.OffsetDateTime
import java.util.UUID

data class DivisionEntity (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val number: Int,
   val name: String,
   val description: String,
   val manager: Employee? = null,
   var company: CompanyEntity
) : Entity<DivisionEntity>, Comparable<DivisionEntity> {

   override fun compareTo(other: DivisionEntity): Int =
      CompareToBuilder()
         .append(this.number, other.number)
         .append(this.name, other.name)
         .append(this.manager, other.manager)
         .append(this.description, other.description)
         .append(this.company, other.company)
         .toComparison()

   fun toValueObject(): DivisionValueObject {
      return DivisionValueObject(
         id = this.id,
         number = this.number,
         name = this.name,
         manager = this.manager,
         description = this.description,
         company = this.company.toValueObject()
      )
   }

   override fun myId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): DivisionEntity = copy()
}
