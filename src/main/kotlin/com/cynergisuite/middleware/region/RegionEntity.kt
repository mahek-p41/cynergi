package com.cynergisuite.middleware.region

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.division.DivisionEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import org.apache.commons.lang3.builder.CompareToBuilder
import java.time.LocalDate

data class RegionEntity(
   val id: Long? = null,
   val number: Long? = null,
   val name: String,
   val description: String?,
   val division: DivisionEntity,
   val regionalManager: EmployeeEntity? = null,
   val effectiveDate: LocalDate,
   val endingDate: LocalDate?,
) : Identifiable, Comparable<RegionEntity> {
   constructor(dto: RegionDTO, division: DivisionEntity, regionalManager: EmployeeEntity?) : this(
      id = dto.id,
      number = dto.number,
      name = dto.name!!,
      description = dto.description,
      division = division,
      regionalManager = regionalManager,
      effectiveDate = dto.effectiveDate!!,
      endingDate = dto.endingDate,
   )

   override fun compareTo(other: RegionEntity): Int =
      CompareToBuilder()
         .append(this.id, other.id)
         .append(this.number, other.number)
         .append(this.name, other.name)
         .append(this.description, other.description)
         .append(this.division, other.division)
         .append(this.effectiveDate, other.effectiveDate)
         .append(this.endingDate, other.endingDate)
         .toComparison()

   fun toValueObject(): RegionDTO {
      return RegionDTO(
         id = this.id,
         number = this.number,
         name = this.name,
         description = this.description,
         division = this.division.toValueObject(),
         regionalManager = this.regionalManager?.let { SimpleIdentifiableDTO(it) },
         effectiveDate = this.effectiveDate,
         endingDate = this.endingDate,
      )
   }

   override fun myId(): Long? = id
}
