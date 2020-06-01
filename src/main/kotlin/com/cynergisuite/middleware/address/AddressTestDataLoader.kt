package com.cynergisuite.middleware.address

import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.Locale
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object AddressTestDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1): Stream<AddressEntity> {
      val faker = Faker(Locale.US)
      val number = if (numberIn < 0) 1 else numberIn
      val address = faker.address()
      val phone = faker.phoneNumber()
      val name = faker.name()
      val random = faker.random()
      val lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         val state = address.stateAbbr()
         val postalCode = address.zipCode()

         AddressEntity(
            id = null,
            name = name.username(),
            address1 = address.streetAddress(),
            address2 = if (random.nextBoolean()) address.secondaryAddress() else null,
            city = address.city(),
            state = state,
            postalCode = postalCode,
            latitude = address.latitude().toDouble(),
            longitude = address.longitude().toDouble(),
            country = address.countryCode(),
            county = lorem.characters(2).toUpperCase(),
            phone = phone.cellPhone(),
            fax = phone.cellPhone()
         )
      }
   }

   @JvmStatic
   fun single(): AddressEntity {
      return stream(1).findFirst().orElseThrow { Exception("Unable to create single AddressEntity") }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AddressTestDataLoaderService @Inject constructor(
   private val addressRepository: AddressRepository
) {

   fun single(): AddressEntity {
      return AddressTestDataLoader.single().let { addressRepository.insert(it) }
   }
}
