package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.<%= entityname %>
import com.hightouchinc.cynergi.middleware.repository.<%= entityname %>Repository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.time.OffsetDateTime
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class <%= entityname %>TestDataLoader {
   static Stream<<%= entityname %>> stream(int number =1) {
      final int value = number > 0 ? number : 1

      return IntStream.range(0, value).mapToObj {
         new <%= entityname %>(
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
class <%= entityname %>DataLoaderService {
   private final <%= entityname %>Repository <%= entityname %>Repository

   <%= entityname %>DataLoaderService(<%= entityname %>Repository <%= entityname %>Repository) {
      this.<%= entityname %>Repository = <%= entityname %>Repository
   }

   Stream<<%= entityname %>> stream(int number = 1) {
      return <%= entityname %>TestDataLoader.stream(number)
         .map {
         <%= entityname %>Repository.insert(it)
      }
   }
}
