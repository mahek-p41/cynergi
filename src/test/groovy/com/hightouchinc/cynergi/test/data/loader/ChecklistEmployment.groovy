package com.hightouchinc.cynergi.test.data.loader

import com.hightouchinc.cynergi.middleware.entity.ChecklistEmployment
import com.hightouchinc.cynergi.middleware.repository.ChecklistEmploymentRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.time.OffsetDateTime
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class ChecklistEmploymentTestDataLoader {
   static Stream<ChecklistEmployment> stream(int number =1) {
      final int value = number > 0 ? number : 1

      return IntStream.range(0, value).mapToObj {
         new ChecklistEmployment(
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
class ChecklistEmploymentDataLoaderService {
   private final ChecklistEmploymentRepository checklistEmploymentRepository

   ChecklistEmploymentDataLoaderService(ChecklistEmploymentRepository checklistEmploymentRepository) {
      this.checklistEmploymentRepository = checklistEmploymentRepository
   }

   Stream<ChecklistEmployment> stream(int number = 1) {
      return ChecklistEmploymentTestDataLoader.stream(number)
         .map {
            checklistEmploymentRepository.insert(it)
         }
   }
}
