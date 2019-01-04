package com.hightouchinc.cynergi.middleware.entity

data class Checklist(
   var id: Long
): IdentifiableEntity {

   override fun entityId(): Long? = id
}
