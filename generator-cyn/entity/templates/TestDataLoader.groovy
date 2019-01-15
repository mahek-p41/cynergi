package com.hightouchinc.cynergi.test.data.loader

import com.hightouchinc.cynergi.middleware.entity.<%= entity %>
import com.hightouchinc.cynergi.middleware.repository.<%= entity %>Repository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.time.OffsetDateTime
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class <%= entity %>TestDataLoader {
   static Stream<<%= entity %>> stream(int number =1) {
      final int value = number > 0 ? number : 1

      return IntStream.range(0, value).mapToObj {
         new <%= entity %>(
            null,
            UUID.randomUUID(),
            OffsetDateTime.now(),
            OffsetDateTime.now()
         )
      }
   }
}

@Singleton
@CompileStatic
class <%= entity %>DataLoaderService {
   private final <%= entity %>Repository <%= entity %>Repository

   <%= entity %>DataLoaderService(<%= entity %>Repository <%= entity %>Repository) {
      this.<%= entity %>Repository = <%= entity %>Repository
   }

   Stream<<%= entity %>> stream(int number = 1) {
      return <%= entity %>TestDataLoader.stream(number)
         .map {
         <%= entity %>Repository.insert(it)
      }
   }
}
