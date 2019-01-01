package com.hightouchinc.cynergi.middleware.domain

@DataTransferObject
data class BadRequest(
   var fields: Set<BadRequestField>
)

@DataTransferObject
data class BadRequestField(
   var description: String?,
   var field: String,
   var value: Any?
)
