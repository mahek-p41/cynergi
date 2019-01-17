package com.hightouchinc.cynergi.middleware.dto

import com.hightouchinc.cynergi.middleware.domain.DataTransferObject

@DataTransferObject
data class SimpleIdentifiableDto(
   val id: Long? = null
) : IdentifiableDto {
   override fun dtoId(): Long? = id
}
