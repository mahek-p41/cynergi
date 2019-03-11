package com.hightouchinc.cynergi.middleware.exception

class CynergiAccessException(
   val errorMessage: String,
   val user: String
): Exception()
