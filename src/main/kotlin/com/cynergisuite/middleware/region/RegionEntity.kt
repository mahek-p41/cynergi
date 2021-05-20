package com.cynergisuite.middleware.region

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.division.DivisionEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import org.apache.commons.lang3.builder.CompareToBuilder
import java.util.UUID

data class RegionEntity(
   val id: UUID? = null,
   val number: Long? = null,
   val name: String,
   val description: String?,
   val division: DivisionEntity,
   val regionalManager: EmployeeEntity? = null,
) : Identifiable, Comparable<RegionEntity> {
   constructor(id: UUID? = null, dto: RegionDTO, division: DivisionEntity, regionalManager: EmployeeEntity?) : this(
      id = id,
      number = dto.number,
      name = dto.name!!,
      description = dto.description,
      division = division,
      regionalManager = regionalManager,
   )

   override fun compareTo(other: RegionEntity): Int =
      CompareToBuilder()
         .append(this.id, other.id)
         .append(this.number, other.number)
         .append(this.name, other.name)
         .append(this.description, other.description)
         .append(this.division, other.division)
         .toComparison()

   fun toValueObject(): RegionDTO {
      return RegionDTO(
         id = this.id,
         number = this.number,
         name = this.name,
         description = this.description,
         division = this.division.toValueObject(),
         regionalManager = this.regionalManager?.let { SimpleLegacyIdentifiableDTO(it) },
      )
   }

   override fun myId(): UUID? = id
}
