package com.cynergisuite.middleware.accounting.account.payable

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

class DefaultAccountPayableStatusTypeDataLoader {

   private static final List<DefaultAccountPayableStatusType> defaultAccountPayableStatusType = [
      new DefaultAccountPayableStatusType(
         1,
         "H",
         "Hold",
         "hold"
      ),
      new DefaultAccountPayableStatusType(
         2,
         "O",
         "Open",
         "open"
      )
   ]

   static DefaultAccountPayableStatusType random() {
      return defaultAccountPayableStatusType.random()
   }

   static List<DefaultAccountPayableStatusType> predefined() {
      return defaultAccountPayableStatusType
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class DefaultAccountPayableStatusTypeDataLoaderService {
   def random() { DefaultAccountPayableStatusTypeDataLoader.random() }
   def predefined() { DefaultAccountPayableStatusTypeDataLoader.predefined() }
}
