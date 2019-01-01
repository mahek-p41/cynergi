package com.hightouchinc.cynergi.middleware.exception

class NotFoundException(
   val notFound: Any
): Exception(notFound.toString()) {

   constructor(id: Long):
      this(notFound = id)
}
