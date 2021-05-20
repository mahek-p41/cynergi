package com.cynergisuite.middleware.vendor.rebate

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

class RebateTypeDataLoader {

   private static final List<RebateType> rebateType = [
      new RebateType(
         1,
         "P",
         "Percent",
         "percent"
      ),
      new RebateType(
         2,
         "U",
         "Unit",
         "unit"
      )
   ]

   static RebateType random() {
      return rebateType.random()
   }

   static List<RebateType> predefined() {
      return rebateType
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class RebateTestTypeDataLoaderService {
   def random() { RebateTypeDataLoader.random() }
   def predefined() { RebateTypeDataLoader.predefined() }
}
