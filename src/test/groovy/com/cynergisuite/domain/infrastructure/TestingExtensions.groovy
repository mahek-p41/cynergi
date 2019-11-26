package com.cynergisuite.domain.infrastructure

import groovy.json.JsonSlurper
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException

import static io.micronaut.http.HttpStatus.NO_CONTENT

class HttpResponseExtension {
   static JsonSlurper jsonSlurper = new JsonSlurper()

   static Object bodyAsJson(final HttpResponse self) throws HttpClientException {
      if (self.body() != null) {
         return jsonSlurper.parseText(self.body())
      } else if (self.status == NO_CONTENT) {
         throw new HttpClientResponseException(null, self)
      } else {
         null
      }
   }
}
