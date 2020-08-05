package com.cynergisuite.middleware.vendor.group

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.Company

data class VendorGroupEntity(
   val id: Long? = null,
   val company: Company,
   val value: String,
   val description: String
) : Identifiable {

   constructor(dto: VendorGroupDTO, company: Company) :
      this(
         id = dto.id,
         company = company,
         value = dto.value!!,
         description = dto.description!!
      )

   override fun myId(): Long? = id
}
