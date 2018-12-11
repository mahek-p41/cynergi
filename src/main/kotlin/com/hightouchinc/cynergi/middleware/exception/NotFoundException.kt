package com.hightouchinc.cynergi.middleware.exception

class NotFoundException(message: String): Exception(message) {
   constructor(id: Long): this(message = id.toString())
}
