package com.hightouchinc.cynergi.middleware.entity.helper

import com.hightouchinc.cynergi.middleware.dto.IdentifiableDto
import com.hightouchinc.cynergi.middleware.entity.IdentifiableEntity

class DtoProxiedIdentifiableEntity(
   private val proxy: IdentifiableDto
) : IdentifiableEntity {
   override fun entityId(): Long? = proxy.dtoId()
}
