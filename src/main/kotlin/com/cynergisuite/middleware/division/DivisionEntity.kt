package com.cynergisuite.middleware.division

import com.cynergisuite.domain.Entity
import com.cynergisuite.middleware.company.Company
import java.time.OffsetDateTime
import java.util.*

data class DivisionEntity (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val company: Company,
   val number: Int,
   val name: String,
   val employeeNumber: Int? = null,
   val description: String?
) : Entity<DivisionEntity> {

   constructor(vo: DivisionValueObject, company: Company) :
      this(
         id = vo.id,
         company = company,
         number = vo.number!!,
         name = vo.name!!,
         employeeNumber = vo.employeeNumber!!,
         description = vo.description
      )

   override fun myId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): DivisionEntity = copy()
}
