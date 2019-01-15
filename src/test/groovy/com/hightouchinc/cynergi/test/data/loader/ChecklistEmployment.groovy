package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.ChecklistEmployment
import com.hightouchinc.cynergi.middleware.repository.ChecklistEmploymentRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class ChecklistEmploymentTestDataLoader {
   static Stream<ChecklistEmployment> stream(int number =1) {
      final int value = number > 0 ? number : 1
      final def faker = new Faker()
      final def company = faker.company()
      final def job = faker.job()
      final def date = faker.date()
      final def bool = faker.bool()

      return IntStream.range(0, value).mapToObj {
         new ChecklistEmployment(
            null,
            UUID.randomUUID(),
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            job.field(),
            date.past(3650, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            bool.bool(),
            company.name(),
            bool.bool(),
            job.title()
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
