package com.hightouchinc.cynergi.middleware.entity

import com.hightouchinc.cynergi.middleware.domain.DataTransferObject

@DataTransferObject
data class Company(
   val id: Long? = null,

   val name: String
) {
   constructor(name: String):
      this(id = null, name = name)
}
