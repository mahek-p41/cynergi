package com.cynergisuite.middleware.region

import com.cynergisuite.domain.Entity
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.division.DivisionEntity
import com.cynergisuite.middleware.division.DivisionValueObject
import java.time.OffsetDateTime
import java.util.*

data class RegionEntity (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val division: DivisionEntity,
   val number: Int,
   val name: String,
   val employeeNumber: Int?,
   val description: String?
) : Entity<RegionEntity> {

   constructor(vo: RegionValueObject, division: DivisionEntity) :
      this(
         id = vo.id,
         division = division,
         number = vo.number!!,
         name = vo.name!!,
         employeeNumber = vo.employeeNumber!!,
         description = vo.description
      )

   override fun myId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): RegionEntity = copy()
}
