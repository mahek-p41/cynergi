package com.cynergisuite.middleware.division

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import org.apache.commons.lang3.builder.CompareToBuilder

data class DivisionEntity (
   val id: Long? = null,
   val number: Long,
   val name: String,
   val description: String?,
   val company: CompanyEntity,
   val divisionalManager: EmployeeEntity? = null
) : Identifiable, Comparable<DivisionEntity> {
   constructor(dto: DivisionDTO, company: Company, divisionalManager: EmployeeEntity?) : this(
      id = dto.id,
      number = dto.number,
      name = dto.name!!,
      description = dto.description,
      company = CompanyEntity.create(company)!!,
      divisionalManager = divisionalManager
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
         divisionalManager = this.divisionalManager?.let { SimpleIdentifiableDTO(it) }
      )
   }

   override fun myId(): Long? = id
}
