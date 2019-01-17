package com.hightouchinc.cynergi.middleware.dto

import com.hightouchinc.cynergi.middleware.entity.IdentifiableEntity

class EntityProxiedIdentifiableDto(
   private val proxy: IdentifiableEntity
) : IdentifiableDto {
   override fun dtoId(): Long? = proxy.entityId()
}
