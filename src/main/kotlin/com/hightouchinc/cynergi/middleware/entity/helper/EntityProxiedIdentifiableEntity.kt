package com.hightouchinc.cynergi.middleware.entity.helper

import com.hightouchinc.cynergi.middleware.entity.IdentifiableEntity

class EntityProxiedIdentifiableEntity(
   private val proxy: IdentifiableEntity
) : IdentifiableEntity {
   override fun entityId(): Long? = proxy.entityId()
}
