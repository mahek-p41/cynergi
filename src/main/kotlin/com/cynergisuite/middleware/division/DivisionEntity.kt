package com.cynergisuite.middleware.division

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.CompanyEntity
import org.apache.commons.lang3.builder.CompareToBuilder

data class DivisionEntity (
   val id: Long? = null,
   val number: Int,
   val name: String,
   val description: String?,
   var company: CompanyEntity
) : Identifiable, Comparable<DivisionEntity> {

   override fun compareTo(other: DivisionEntity): Int =
      CompareToBuilder()
         .append(this.number, other.number)
         .append(this.name, other.name)
         .append(this.description, other.description)
         .append(this.company, other.company)
         .toComparison()

   fun toValueObject(): DivisionValueObject {
      return DivisionValueObject(
         id = this.id,
         name = this.name,
         description = this.description,
         company = this.company.toValueObject()
      )
   }

   override fun myId(): Long? = id
}
