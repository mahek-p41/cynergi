package com.hightouchinc.cynergi.middleware

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info

@OpenAPIDefinition(
   info = Info (
      title = "Cynergi Middleware",
      description = "Cynergi Middleware API"
   )
)
object Application {

   @JvmStatic
   fun main(args: Array<String>) {
      Micronaut.build()
         .args(*args)
         .packages("com.hightouchinc.cynergi.middleware")
         .mainClass(Application.javaClass)
         .start()
   }
}
