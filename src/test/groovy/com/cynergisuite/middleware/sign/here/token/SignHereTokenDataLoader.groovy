package com.cynergisuite.middleware.sign.here.token

import com.cynergisuite.middleware.sign.here.token.infrastructure.SignHereTokenRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.StoreEntity
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.stream.IntStream
import java.util.stream.Stream

class SignHereTokenTestDataLoader {

   static Stream<SignHereTokenEntity> stream(int numberIn = 1, CompanyEntity company, StoreEntity store, String token) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final random = faker.random()

      return IntStream.range(0, number).mapToObj {
         new SignHereTokenEntity(
            null,
            company,
            store,
            token
         )
      }
   }

}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class SignHereTokenTestDataLoaderService {
   private final SignHereTokenRepository signHereTokenRepository

   @Inject
   SignHereTokenTestDataLoaderService(SignHereTokenRepository signHereTokenRepository) {
      this.signHereTokenRepository = signHereTokenRepository
   }

   SignHereTokenEntity single(CompanyEntity company, StoreEntity store, String token) {
      return SignHereTokenTestDataLoader.stream(company, store, token)
         .map { signHereTokenRepository.insert(it) }
         .findFirst().orElseThrow { new Exception("Unable to create AgreementSigningEntity") }
   }
}
