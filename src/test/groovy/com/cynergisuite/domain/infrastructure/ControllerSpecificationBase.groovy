package com.cynergisuite.domain.infrastructure

import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client

import javax.inject.Inject

abstract class ControllerSpecificationBase extends ServiceSpecificationBase {
   @Client("/api") @Inject RxHttpClient httpClient

   protected BlockingHttpClient client

   void setup() {
      client = httpClient.toBlocking()
   }
}
