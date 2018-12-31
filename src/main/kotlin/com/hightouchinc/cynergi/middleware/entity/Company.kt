package com.hightouchinc.cynergi.middleware.entity

import com.hightouchinc.cynergi.middleware.domain.DataTransferObject
import javax.validation.constraints.NotNull

data class Company(
   val id: Long? = null,

   val name: String
) {
   constructor(name: String):
      this(id = null, name = name)

   constructor(dto: CompanyDto):
      this(id = dto.id, name = dto.name!!)
}

@DataTransferObject
data class CompanyDto(
   var id: Long? = null,

   @NotNull
   var name: String? = null
) {
   constructor(company: Company):
      this(id = company.id, name = company.name)
}
