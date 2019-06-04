package com.cynergisuite.domain.infrastructure

import groovy.json.JsonSlurper
import io.micronaut.http.HttpResponse

class HttpResponseExtension {
   static JsonSlurper jsonSlurper = new JsonSlurper()

   static Object bodyAsJson(final HttpResponse self) {
      return jsonSlurper.parseText(self.body())
   }
}
