package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.Checklist
import com.hightouchinc.cynergi.middleware.repository.ChecklistRepository
import groovy.transform.CompileStatic

import javax.inject.Inject
import javax.inject.Singleton
import java.time.OffsetDateTime
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class ChecklistTestDataLoader {
   static Stream<Checklist> stream(int number = 1) {
      final int value = number > 0 ? number : 1
      final def faker = new Faker()
      final def numberFaker = faker.number()
      final def chuckNorris = faker.chuckNorris()

      return IntStream.range(0, value).mapToObj {
         new Checklist(
            null,
            UUID.randomUUID(),
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            numberFaker.digits(6),
            chuckNorris.fact(),
            numberFaker.digits(6),
            OffsetDateTime.now(),
            numberFaker.digits(6),
            ChecklistAutoTestDataLoader.stream(1).findFirst().orElseThrow { new Exception("Unable to create ChecklistAuto") }
         )
      }
   }
}

@Singleton
@CompileStatic
class ChecklistDataLoaderService {
   private final ChecklistRepository checklistRepository

   @Inject
   ChecklistDataLoaderService(ChecklistRepository checklistRepository) {
      this.checklistRepository = checklistRepository
   }

   Stream<Checklist> stream(int number = 1) {
      return ChecklistTestDataLoader.stream(number)
         .map { checklistRepository.insert(it) }
   }
}
