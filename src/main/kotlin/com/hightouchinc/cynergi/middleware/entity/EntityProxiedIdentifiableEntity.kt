package com.hightouchinc.cynergi.middleware.entity

class EntityProxiedIdentifiableEntity(
   private val proxy: IdentifiableEntity
) : IdentifiableEntity {
   override fun entityId(): Long? = proxy.entityId()
}
