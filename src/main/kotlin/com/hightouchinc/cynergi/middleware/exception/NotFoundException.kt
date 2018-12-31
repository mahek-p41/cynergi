package com.hightouchinc.cynergi.middleware.exception

import com.hightouchinc.cynergi.middleware.domain.DataTransferObject

class NotFoundException(
   val notFound: NotFound
): Exception(notFound.requestedNotFound) {
   constructor(id: Long):
      this(notFound = NotFound(id.toString()))
}

@DataTransferObject
data class NotFound(
   val requestedNotFound: String
)
