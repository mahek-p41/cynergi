package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.Checklist
import com.hightouchinc.cynergi.middleware.entity.ChecklistReference
import com.hightouchinc.cynergi.middleware.repository.ChecklistReferenceRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.time.OffsetDateTime
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class ChecklistReferenceTestDataLoader {
   static Stream<ChecklistReference> stream(Checklist checklist, int number = 1) {
      final int value = number > 0 ? number : 1
      final def faker = new Faker()
      final def bool = faker.bool()
      final def num = faker.number()
      final def lorem = faker.lorem()

      return IntStream.range(0, value).mapToObj {
         new ChecklistReference(
            null,
            UUID.randomUUID(),
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            bool.bool(),
            bool.bool(),
            num.numberBetween(1, 20),
            bool.bool(),
            lorem.fixedString(3),
            bool.bool(),
            bool.bool(),
            num.numberBetween(1, 20),
            bool.bool(),
            checklist.id
         )
      }
   }
}

@Singleton
@CompileStatic
class ChecklistReferenceDataLoaderService {
   private final ChecklistDataLoaderService checklistDataLoaderService
   private final ChecklistReferenceRepository checklistReferenceRepository

   ChecklistReferenceDataLoaderService(
      ChecklistDataLoaderService checklistDataLoaderService,
      ChecklistReferenceRepository checklistReferenceRepository
   ) {
      this.checklistDataLoaderService = checklistDataLoaderService
      this.checklistReferenceRepository = checklistReferenceRepository
   }

   Stream<ChecklistReference> stream(Checklist checklist, int number = 1) {
      checklist = checklist !=  null ? checklist : checklistDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Checklist") }

      return ChecklistReferenceTestDataLoader.stream(checklist, number)
         .map {
            checklistReferenceRepository.insert(it)
         }
   }
}
