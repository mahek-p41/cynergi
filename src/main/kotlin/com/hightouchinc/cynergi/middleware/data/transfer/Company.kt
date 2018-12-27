package com.hightouchinc.cynergi.middleware.data.transfer

import com.hightouchinc.cynergi.middleware.data.domain.DataTransferObject

@DataTransferObject
data class Company(
   val id: Long? = null,

   val name: String
) {
   constructor(name: String):
      this(id = null, name = name)
}
