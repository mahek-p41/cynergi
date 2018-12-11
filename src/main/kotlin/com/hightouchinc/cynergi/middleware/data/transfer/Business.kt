package com.hightouchinc.cynergi.middleware.data.transfer

import com.hightouchinc.cynergi.middleware.data.domain.DataTransferObject

@DataTransferObject
data class Business(
   var id: Long,
   var name: String
)
