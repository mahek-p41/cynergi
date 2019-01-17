package com.hightouchinc.cynergi.middleware.dto.helper

import com.hightouchinc.cynergi.middleware.domain.DataTransferObject
import com.hightouchinc.cynergi.middleware.dto.IdentifiableDto

@DataTransferObject
data class SimpleIdentifiableDto(
   val id: Long? = null
) : IdentifiableDto {
   override fun dtoId(): Long? = id
}
