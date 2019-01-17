package com.hightouchinc.cynergi.middleware.dto.helper

import com.hightouchinc.cynergi.middleware.dto.IdentifiableDto
import com.hightouchinc.cynergi.middleware.entity.IdentifiableEntity

class EntityProxiedIdentifiableDto(
   private val proxy: IdentifiableEntity
) : IdentifiableDto {
   override fun dtoId(): Long? = proxy.entityId()
}
