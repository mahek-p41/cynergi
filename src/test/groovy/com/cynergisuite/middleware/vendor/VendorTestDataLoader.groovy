package com.cynergisuite.middleware.vendor


import com.cynergisuite.middleware.address.AddressEntity
import com.cynergisuite.middleware.address.AddressTestDataLoader
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.shipping.freight.calc.method.FreightCalcMethodTypeTestDataLoader
import com.cynergisuite.middleware.shipping.freight.onboard.FreightOnboardTypeTestDataLoader
import com.cynergisuite.middleware.shipping.shipvia.ShipViaEntity
import com.cynergisuite.middleware.vendor.group.VendorGroupEntity
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermEntity
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import javax.inject.Singleton
import java.math.RoundingMode
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class VendorTestDataLoader {

   static Stream<VendorEntity> stream(int numberIn = 1, CompanyEntity companyIn, AddressEntity addressIn, VendorPaymentTermEntity paymentTermIn, ShipViaEntity shipViaIn, VendorGroupEntity vendorGroupIn = null, String nameIn = null) {
      final faker = new Faker()
      final random = faker.random()
      final number = numberIn < 0 ? 1 : numberIn
      final companyFaker = faker.company()
      final numbers = faker.number()
      final name = faker.name()
      final phone = faker.phoneNumber()
      final email = faker.internet()

      return IntStream.range(0, number).mapToObj {
         new VendorEntity(
            null,
            companyIn,
            nameIn ?: companyFaker.name().take(30) ?: "Test",
            addressIn ?: AddressTestDataLoader.single(),
            numbers.digits(10).toString(),
            null,
            FreightOnboardTypeTestDataLoader.random(),
            paymentTermIn,
            random.nextBoolean() ? numbers.numberBetween(1, 10) : null,
            random.nextBoolean(),
            shipViaIn,
            vendorGroupIn,
            numbers.numberBetween(1, 100),
            numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            numbers.numberBetween(1, 100),
            numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextBoolean(),
            numbers.digits(12).toString(),
            name.firstName(),
            phone.cellPhone(),
            random.nextBoolean(),
            random.nextBoolean() ? random.nextInt(1, 100).toBigDecimal().divide(new BigDecimal(100)).setScale(7, RoundingMode.HALF_EVEN) : null,
            FreightCalcMethodTypeTestDataLoader.random(),
            random.nextBoolean() ? random.nextInt(1, 100).toBigDecimal().divide(new BigDecimal(100)).setScale(7, RoundingMode.HALF_EVEN) : null,
            random.nextBoolean() ? numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN) : null,
            random.nextBoolean(),
            random.nextBoolean(),
            random.nextBoolean(),
            random.nextBoolean(),
            random.nextBoolean(),
            email.emailAddress(),
            email.safeEmailAddress(),
            random.nextBoolean(),
            random.nextBoolean(),
            it + 1,
            faker.lorem().sentence(),
            phone.phoneNumber()
         )
      }
   }

   static VendorEntity single(CompanyEntity companyIn, VendorPaymentTermEntity paymentTermIn, ShipViaEntity shipViaIn) {
      stream(1, companyIn, null, paymentTermIn, shipViaIn).findFirst().orElseThrow { new Exception("Unable to create VendorEntity") }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class VendorTestDataLoaderService {
   private final VendorRepository vendorRepository

   VendorTestDataLoaderService(VendorRepository vendorRepository) {
      this.vendorRepository = vendorRepository
   }

   Stream<VendorEntity> stream(int numberIn = 1, CompanyEntity companyIn, AddressEntity addressIn = null, VendorPaymentTermEntity paymentTermIn, ShipViaEntity shipViaIn, VendorGroupEntity vendorGroupIn = null, String nameIn = null) {
      return VendorTestDataLoader.stream(numberIn, companyIn, addressIn, paymentTermIn, shipViaIn, vendorGroupIn, nameIn)
         .map { vendorRepository.insert(it) }
   }

   Stream<VendorEntity> stream(int numberIn = 1, CompanyEntity companyIn, VendorPaymentTermEntity paymentTermIn, ShipViaEntity shipViaIn, VendorGroupEntity vendorGroupIn) {
      return VendorTestDataLoader.stream(numberIn, companyIn, null, paymentTermIn, shipViaIn, vendorGroupIn)
         .map { vendorRepository.insert(it) }
   }

   VendorEntity single(CompanyEntity companyEntity, VendorPaymentTermEntity vendorPaymentTermEntity, ShipViaEntity shipViaEntity) {
      return stream(1, companyEntity, null, vendorPaymentTermEntity, shipViaEntity, null).findFirst().orElseThrow { new Exception("Unable to create VendorEntity") }
   }

   VendorEntity single(CompanyEntity companyEntity, VendorPaymentTermEntity vendorPaymentTermEntity, ShipViaEntity shipViaEntity, String nameIn) {
      return stream(1, companyEntity, null, vendorPaymentTermEntity, shipViaEntity, null, nameIn).findFirst().orElseThrow { new Exception("Unable to create VendorEntity") }
   }

   VendorEntity single(CompanyEntity companyEntity, AddressEntity addressIn, VendorPaymentTermEntity vendorPaymentTermEntity, ShipViaEntity shipViaEntity) {
      return stream(1, companyEntity, addressIn, vendorPaymentTermEntity, shipViaEntity, null).findFirst().orElseThrow { new Exception("Unable to create VendorEntity") }
   }

   VendorEntity single(CompanyEntity companyEntity, VendorPaymentTermEntity vendorPaymentTermEntity, ShipViaEntity shipViaEntity, VendorGroupEntity vendorGroup) {
      return stream(1, companyEntity, null, vendorPaymentTermEntity, shipViaEntity, vendorGroup).findFirst().orElseThrow { new Exception("Unable to create VendorEntity") }
   }
}
