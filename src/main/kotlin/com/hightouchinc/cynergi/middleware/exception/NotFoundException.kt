package com.hightouchinc.cynergi.middleware.exception

class NotFoundException(
   val notFound: String
): Exception(notFound) {

   constructor(id: Long):
      this(notFound = id.toString())
}
