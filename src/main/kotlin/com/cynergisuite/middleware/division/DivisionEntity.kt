package com.cynergisuite.middleware.division

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.employee.EmployeeEntity
import org.apache.commons.lang3.builder.CompareToBuilder
import java.util.UUID

data class DivisionEntity(
   val id: UUID? = null,
   val company: Company,
   val number: Long? = null,
   val name: String,
   val divisionalManager: EmployeeEntity? = null,
   val description: String?,
) : Identifiable, Comparable<DivisionEntity> {
   constructor(id: UUID? = null, dto: DivisionDTO, company: Company, divisionalManager: EmployeeEntity?) : this(
      id = id,
      number = dto.number,
      name = dto.name!!,
      description = dto.description,
      company = company,
      divisionalManager = divisionalManager,
   )

   override fun compareTo(other: DivisionEntity): Int =
      CompareToBuilder()
         .append(this.number, other.number)
         .append(this.name, other.name)
         .append(this.description, other.description)
         .append(this.company, other.company)
         .append(this.divisionalManager, other.divisionalManager)
         .toComparison()

   fun toValueObject(): DivisionDTO {
      return DivisionDTO(
         id = this.id,
         number = this.number,
         name = this.name,
         description = this.description,
         divisionalManager = this.divisionalManager?.let { SimpleLegacyIdentifiableDTO(it) },
      )
   }

   override fun myId(): UUID? = id
}
