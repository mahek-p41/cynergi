package com.cynergisuite.middleware.exception

class NotFoundException(
   val notFound: String
): Exception(notFound) {

   constructor(id: Long):
      this(notFound = id.toString())
}
