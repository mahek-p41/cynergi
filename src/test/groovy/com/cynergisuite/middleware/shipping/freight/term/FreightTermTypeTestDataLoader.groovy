package com.cynergisuite.middleware.shipping.freight.term

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

class FreightTermTypeTestDataLoader {

   private static final List<FreightTermType> freightTermType = [
      new FreightTermType(
         1,
         "C",
         "Collect",
         "collect"
      ),
      new FreightTermType(
         2,
         "P",
         "Prepaid",
         "prepaid"
      )
   ]

   static FreightTermType random() { freightTermType.random() }

   static List<FreightTermType> predefined() { freightTermType }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class FreightTermTypeTestDataLoaderService {
   def random() { FreightTermTypeTestDataLoader.random() }
   def predefined() { FreightTermTypeTestDataLoader.predefined() }
}
