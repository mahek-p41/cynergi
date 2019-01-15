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
   private final <%= entity %>Repository <%= repository %>Repository

   <%= entity %>DataLoaderService(<%= entity %>Repository <%= repository %>Repository) {
      this.<%= repository %>Repository = <%= repository %>Repository
   }

   Stream<<%= entity %>> stream(int number = 1) {
      return <%= entity %>TestDataLoader.stream(number)
         .map {
            <%= repository %>Repository.insert(it)
         }
   }
}
