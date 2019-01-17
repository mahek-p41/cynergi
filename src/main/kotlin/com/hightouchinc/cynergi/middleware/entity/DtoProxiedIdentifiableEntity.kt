package com.hightouchinc.cynergi.middleware.entity

import com.hightouchinc.cynergi.middleware.dto.IdentifiableDto

class DtoProxiedIdentifiableEntity(
   private val proxy: IdentifiableDto
) : IdentifiableEntity {
   override fun entityId(): Long? = proxy.dtoId()
}
