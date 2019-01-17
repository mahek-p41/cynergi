package com.hightouchinc.cynergi.middleware.entity

class SimpleIdentifiableEntity(
   private val id: Long?
) : IdentifiableEntity {
   override fun entityId(): Long? = id
}
