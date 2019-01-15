package com.hightouchinc.cynergi.test.data.loader

import com.hightouchinc.cynergi.middleware.entity.ChecklistLandlord
import com.hightouchinc.cynergi.middleware.repository.ChecklistLandlordRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.time.OffsetDateTime
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class ChecklistLandlordTestDataLoader {
   static Stream<ChecklistLandlord> stream(int number =1) {
      final int value = number > 0 ? number : 1

      return IntStream.range(0, value).mapToObj {
         new ChecklistLandlord(
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
class ChecklistLandlordDataLoaderService {
   private final ChecklistLandlordRepository checklistLandlordRepository

   ChecklistLandlordDataLoaderService(ChecklistLandlordRepository checklistLandlordRepository) {
      this.checklistLandlordRepository = checklistLandlordRepository
   }

   Stream<ChecklistLandlord> stream(int number = 1) {
      return ChecklistLandlordTestDataLoader.stream(number)
         .map {
            checklistLandlordRepository.insert(it)
         }
   }
}
