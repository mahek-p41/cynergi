package com.hightouchinc.cynergi.middleware.entity.helper

import com.hightouchinc.cynergi.middleware.entity.IdentifiableEntity

class SimpleIdentifiableEntity(
   private val id: Long?
) : IdentifiableEntity {
   override fun entityId(): Long? = id
}
