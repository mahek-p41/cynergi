package com.cynergisuite.middleware.address

import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class AddressTestDataLoader {

   static Stream<AddressEntity> stream(int numberIn = 1) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker(Locale.US)
      final address = faker.address()
      final phone = faker.phoneNumber()
      final name = faker.name()
      final random = faker.random()
      final lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         final state = address.stateAbbr()
         final postalCode = address.zipCode()

          new AddressEntity(
              null,
              name.username(),
              address.streetAddress(),
              random.nextBoolean()? address.secondaryAddress() : null,
              address.city(),
              state,
              postalCode,
              address.latitude().toDouble(),
              address.longitude().toDouble(),
              address.countryCode(),
              lorem.characters(2).toUpperCase(),
              phone.cellPhone(),
              phone.cellPhone()
          )
      }
   }

   static AddressEntity single() {
      return stream(1).findFirst().orElseThrow { new Exception("Unable to create single AddressEntity") }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AddressTestDataLoaderService {
   private final AddressRepository addressRepository

   AddressTestDataLoaderService(AddressRepository addressRepository) {
      this.addressRepository = addressRepository
   }

   AddressEntity single() {
      return AddressTestDataLoader.single().with { addressRepository.save(it) }
   }
}
