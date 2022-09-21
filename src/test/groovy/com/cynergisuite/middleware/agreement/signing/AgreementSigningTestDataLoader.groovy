package com.cynergisuite.middleware.agreement.signing

import com.cynergisuite.middleware.agreement.signing.infrastructure.AgreementSigningRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreEntity
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.stream.IntStream
import java.util.stream.Stream

class AgreementSigningTestDataLoader {

   static Stream<AgreementSigningEntity> stream(int numberIn = 1, CompanyEntity company, StoreEntity store, int primaryCustomerNumber, int secondaryCustomerNumber, int agreementNumber, String agreementType, int statusId, UUID externalSignatureId) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final random = faker.random()

      return IntStream.range(0, number).mapToObj {
         new AgreementSigningEntity(
            null,
            company,
            store,
            primaryCustomerNumber,
            secondaryCustomerNumber,
            agreementNumber,
            agreementType,
            statusId,
            externalSignatureId
         )
      }
   }

}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AgreementSigningTestDataLoaderService {
   private final AgreementSigningRepository agreementSigningRepository

   @Inject
   AgreementSigningTestDataLoaderService(AgreementSigningRepository agreementSigningRepository) {
      this.agreementSigningRepository = agreementSigningRepository
   }

   AgreementSigningEntity single(int numberIn = 1, CompanyEntity company, StoreEntity store, int primaryCustomerNumber, int secondaryCustomerNumber, int agreementNumber, String agreementType, int statusId, UUID externalSignatureId) {
      return AgreementSigningTestDataLoader.stream(numberIn, company, store, primaryCustomerNumber, secondaryCustomerNumber, agreementNumber, agreementType, statusId, externalSignatureId)
         .map { agreementSigningRepository.insert(it) }
         .findFirst().orElseThrow { new Exception("Unable to create AgreementSigningEntity") }
   }
}
