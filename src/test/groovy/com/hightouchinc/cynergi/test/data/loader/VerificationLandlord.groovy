package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.VerificationLandlord
import com.hightouchinc.cynergi.middleware.repository.VerificationLandlordRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.time.OffsetDateTime
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class VerificationLandlordTestDataLoader {
   static Stream<VerificationLandlord> stream(int number =1) {
      final int value = number > 0 ? number : 1
      final def faker = new Faker()
      final def bool = faker.bool()
      final def phone = faker.phoneNumber()
      final def lorem = faker.lorem()
      final def num = faker.number()
      final def name = faker.name()

      return IntStream.range(0, value).mapToObj {
         new VerificationLandlord(
            null,
            UUID.randomUUID(),
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            bool.bool(),
            phone.cellPhone(),
            lorem.characters(10),
            bool.bool(),
            num.randomNumber(3, true).intValue(),
            name.name(),
            lorem.characters(10),
            bool.bool(),
            bool.bool(),
            num.randomDouble(2, 100, 10000).toBigDecimal()
         )
      }
   }
}

@Singleton
@CompileStatic
class VerificationLandlordDataLoaderService {
   private final VerificationLandlordRepository verificationLandlordRepository

   VerificationLandlordDataLoaderService(VerificationLandlordRepository verificationLandlordRepository) {
      this.verificationLandlordRepository = verificationLandlordRepository
   }

   Stream<VerificationLandlord> stream(int number = 1) {
      return VerificationLandlordTestDataLoader.stream(number)
         .map {
            verificationLandlordRepository.insert(it)
         }
   }
}
