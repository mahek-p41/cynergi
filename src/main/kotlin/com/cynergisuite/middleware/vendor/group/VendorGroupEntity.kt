package com.cynergisuite.middleware.vendor.group

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.CompanyEntity
import java.util.UUID

data class VendorGroupEntity(
   val id: UUID? = null,
   val company: CompanyEntity,
   val value: String,
   val description: String
) : Identifiable {

   constructor(dto: VendorGroupDTO, company: CompanyEntity) :
      this(
         id = dto.id,
         company = company,
         value = dto.value!!,
         description = dto.description!!
      )

   override fun myId(): UUID? = id
}
