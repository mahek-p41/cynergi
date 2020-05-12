package com.cynergisuite.middleware.vendor.group

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.Company

data class VendorGroupEntity(
   val id: Long? = null,
   val company: Company,
   val value: String,
   val description: String
) : Identifiable {

   constructor(id: Long? = null, vo: VendorGroupValueObject, company: Company) :
      this(id = id ?: vo.id,
         company = company,
         value = vo.value!!,
         description = vo.description!!
      )

   constructor(vo: VendorGroupValueObject, company: Company) :
      this(
         id = vo.id,
         company = company,
         value = vo.value!!,
         description = vo.description!!
      )

   constructor(source: VendorGroupEntity, updateWith: VendorGroupValueObject) :
      this(id = source.id!!, vo = updateWith, company = source.company)

   override fun myId(): Long? = id
}
