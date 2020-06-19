package com.cynergisuite.middleware.error

class NotFoundException(
   val notFound: String
) : Exception(notFound) {

   constructor(id: Long) :
      this(notFound = id.toString())

   constructor(any: Any) :
      this(notFound = any.toString())
}
