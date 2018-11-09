package com.hightouchinc.cynergi.middleware

import io.micronaut.runtime.Micronaut

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
