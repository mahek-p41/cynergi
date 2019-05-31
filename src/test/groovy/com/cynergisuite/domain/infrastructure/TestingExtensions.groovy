package com.cynergisuite.domain.infrastructure

import groovy.json.JsonSlurper
import io.micronaut.http.HttpResponse

class HttpResponseExtension {
   static Object bodyAsJson(final HttpResponse self) {
      return new JsonSlurper().parseText(self.body())
   }
}
