package com.cynergisuite.middleware.division

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.CompanyEntity
import org.apache.commons.lang3.builder.CompareToBuilder
import java.util.UUID

data class DivisionEntity(
   val id: UUID? = null,
   val company: CompanyEntity,
   val number: Long? = null,
   val name: String,
   val description: String?,
) : Identifiable, Comparable<DivisionEntity> {
   constructor(id: UUID? = null, dto: DivisionDTO, company: CompanyEntity) :
      this(
         id = id,
         number = dto.number,
         name = dto.name!!,
         description = dto.description,
         company = company,
      )

   override fun compareTo(other: DivisionEntity): Int =
      CompareToBuilder()
         .append(this.number, other.number)
         .append(this.name, other.name)
         .append(this.description, other.description)
         .append(this.company, other.company)
         .toComparison()

   fun toValueObject(): DivisionDTO {
      return DivisionDTO(
         id = this.id,
         number = this.number,
         name = this.name,
         description = this.description,
      )
   }

   override fun myId(): UUID? = id
}
