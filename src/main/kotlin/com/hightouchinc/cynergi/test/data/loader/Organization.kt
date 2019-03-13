package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.Organization
import com.hightouchinc.cynergi.middleware.repository.OrganizationRepository
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object OrganizationTestDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1): Stream<Organization> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         Organization(
            name = lorem.fixedString(6).toUpperCase(),
            billingAccount = lorem.characters(1, 50)
         )
      }
   }

   @JvmStatic
   fun single(): Organization {
      return stream().findFirst().orElseThrow { Exception("Unable to create Organization") }
   }
}

@Singleton
@Requires(env = ["demo", "test"])
class OrganizationDataLoaderService @Inject constructor(
   private val organizationRepository: OrganizationRepository
) {
   fun stream (numberIn: Int = 1): Stream<Organization> {
      return OrganizationTestDataLoader.stream(numberIn = numberIn).map { organizationRepository.insert(it) }
   }

   fun single(): Organization {
      return stream().findFirst().orElseThrow { Exception("Unable to create Organization") }
   }
}
