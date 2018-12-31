package com.hightouchinc.cynergi.middleware.entity

import com.hightouchinc.cynergi.middleware.domain.DataTransferObject
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes
import javax.validation.constraints.NotNull

data class Company(
   val id: Long? = null,

   val name: String
): IdentifieableEntity {
   constructor(name: String):
      this(id = null, name = name)

   constructor(dto: CompanyDto):
      this(id = dto.id, name = dto.name!!)

   override fun entityId(): Long? = id
}

@DataTransferObject
data class CompanyDto(
   var id: Long? = null,

   @NotNull(message = ErrorCodes.Validation.NOT_NULL)
   var name: String? = null
) {
   constructor(name: String):
      this(id = null, name = name)

   constructor(company: Company):
      this(id = company.id, name = company.name)
}
