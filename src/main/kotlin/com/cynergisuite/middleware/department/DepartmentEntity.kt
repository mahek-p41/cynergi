package com.cynergisuite.middleware.department

import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.Relation
import io.micronaut.data.annotation.Relation.Kind.ONE_TO_ONE

@MappedEntity("fastinfo_prod_import.department_vw")
data class DepartmentEntity(

   @field:Id
   @field:GeneratedValue
   val id: Long,
   val code: String,
   val description: String?,

   @field:Relation(ONE_TO_ONE)
   val company: CompanyEntity

) : Department, Comparable<DepartmentEntity> {
   override fun myId(): Long = id
   override fun myCode(): String = code
   override fun myCompany(): CompanyEntity = company

   override fun hashCode(): Int =
      code.hashCode()

   override fun equals(other: Any?): Boolean =
      if (other is Department) {
         code == other.myCode()
      } else {
         false
      }

   override fun toString(): String =
      code

   override fun compareTo(other: DepartmentEntity): Int =
      code.compareTo(other.myCode())
}
