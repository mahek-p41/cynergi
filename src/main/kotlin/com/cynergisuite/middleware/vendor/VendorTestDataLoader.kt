package com.cynergisuite.middleware.vendor

import com.cynergisuite.extensions.truncate
import com.cynergisuite.middleware.address.AddressEntity
import com.cynergisuite.middleware.address.AddressTestDataLoader
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.shipping.freight.calc.method.FreightCalcMethodTypeTestDataLoader
import com.cynergisuite.middleware.shipping.freight.onboard.FreightOnboardTypeTestDataLoader
import com.cynergisuite.middleware.shipping.shipvia.ShipViaEntity
import com.cynergisuite.middleware.vendor.group.VendorGroupEntity
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermEntity
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.math.BigDecimal
import java.math.RoundingMode.HALF_EVEN
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Singleton

object VendorTestDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1, companyIn: Company, addressIn: AddressEntity?, paymentTermIn: VendorPaymentTermEntity, shipViaIn: ShipViaEntity, vendorGroupIn: VendorGroupEntity? = null, nameIn: String? = null): Stream<VendorEntity> {
      val faker = Faker()
      val random = faker.random()
      val number = if (numberIn < 0) 1 else numberIn
      val companyFaker = faker.company()
      val numbers = faker.number()
      val name = faker.name()
      val phone = faker.phoneNumber()
      val email = faker.internet()

      return IntStream.range(0, number).mapToObj {
         VendorEntity(
            id = null,
            company = companyIn,
            name = nameIn ?: companyFaker.name().truncate(30) ?: "Test",
            address = addressIn ?: AddressTestDataLoader.single(),
            accountNumber = numbers.digits(10),
            payTo = null,
            freightOnboardType = FreightOnboardTypeTestDataLoader.random(),
            paymentTerm = paymentTermIn,
            normalDays = if (random.nextBoolean()) numbers.numberBetween(1, 10) else null,
            returnPolicy = random.nextBoolean(),
            shipVia = shipViaIn,
            vendorGroup = vendorGroupIn,
            minimumQuantity = numbers.numberBetween(1, 100),
            minimumAmount = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, HALF_EVEN),
            freeShipQuantity = numbers.numberBetween(1, 100),
            freeShipAmount = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, HALF_EVEN),
            vendor1099 = random.nextBoolean(),
            federalIdNumber = numbers.digits(12),
            salesRepresentativeName = name.firstName(),
            salesRepresentativeFax = phone.cellPhone(),
            separateCheck = random.nextBoolean(),
            bumpPercent = if (random.nextBoolean()) random.nextInt(1, 100).toBigDecimal().divide(BigDecimal(100)).setScale(7, HALF_EVEN) else null,
            freightCalcMethodType = FreightCalcMethodTypeTestDataLoader.random(),
            freightPercent = if (random.nextBoolean()) random.nextInt(1, 100).toBigDecimal().divide(BigDecimal(100)).setScale(7, HALF_EVEN) else null,
            freightAmount = if (random.nextBoolean()) numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, HALF_EVEN) else null,
            chargeInventoryTax1 = random.nextBoolean(),
            chargeInventoryTax2 = random.nextBoolean(),
            chargeInventoryTax3 = random.nextBoolean(),
            chargeInventoryTax4 = random.nextBoolean(),
            federalIdNumberVerification = random.nextBoolean(),
            emailAddress = email.emailAddress(),
            purchaseOrderSubmitEmailAddress = email.safeEmailAddress(),
            allowDropShipToCustomer = random.nextBoolean(),
            autoSubmitPurchaseOrder = random.nextBoolean(),
            note = faker.lorem().sentence(),
            phone = phone.phoneNumber()
         )
      }
   }

   @JvmStatic
   fun single(companyIn: Company, paymentTermIn: VendorPaymentTermEntity, shipViaIn: ShipViaEntity): VendorEntity =
      stream(numberIn = 1, companyIn = companyIn, addressIn = null, paymentTermIn = paymentTermIn, shipViaIn = shipViaIn).findFirst().orElseThrow { Exception("Unable to create VendorEntity") }
}

@Singleton
@Requires(env = ["develop", "test"])
class VendorTestDataLoaderService(
   private val vendorRepository: VendorRepository
) {

   fun stream(numberIn: Int = 1, companyIn: Company, addressIn: AddressEntity? = null, paymentTermIn: VendorPaymentTermEntity, shipViaIn: ShipViaEntity, vendorGroupIn: VendorGroupEntity? = null): Stream<VendorEntity> {
      return VendorTestDataLoader.stream(numberIn, companyIn, addressIn, paymentTermIn, shipViaIn, vendorGroupIn, nameIn = null)
         .map { vendorRepository.insert(it) }
   }

   fun stream(numberIn: Int = 1, companyIn: Company, paymentTermIn: VendorPaymentTermEntity, shipViaIn: ShipViaEntity, nameIn: String? = null): Stream<VendorEntity> {
      return VendorTestDataLoader.stream(numberIn, companyIn, null, paymentTermIn = paymentTermIn, shipViaIn = shipViaIn, nameIn = nameIn)
         .map { vendorRepository.insert(it) }
   }

   fun stream(numberIn: Int = 1, companyIn: Company, paymentTermIn: VendorPaymentTermEntity, shipViaIn: ShipViaEntity): Stream<VendorEntity> {
      return VendorTestDataLoader.stream(numberIn, companyIn, null, paymentTermIn = paymentTermIn, shipViaIn = shipViaIn)
         .map { vendorRepository.insert(it) }
   }

   fun stream(numberIn: Int = 1, companyIn: Company, addressIn: AddressEntity, paymentTermIn: VendorPaymentTermEntity, shipViaIn: ShipViaEntity, nameIn: String? = null): Stream<VendorEntity> {
      return VendorTestDataLoader.stream(numberIn, companyIn, addressIn, paymentTermIn, shipViaIn, nameIn = nameIn)
         .map { vendorRepository.insert(it) }
   }

   fun single(companyIn: Company, paymentTermIn: VendorPaymentTermEntity, shipViaIn: ShipViaEntity): VendorEntity {
      return stream(companyIn = companyIn, addressIn = null, paymentTermIn = paymentTermIn, shipViaIn = shipViaIn).findFirst().orElseThrow { Exception("Unable to create VendorEntity") }
   }

   fun single(companyIn: Company, paymentTermIn: VendorPaymentTermEntity, shipViaIn: ShipViaEntity, vendorGroupIn: VendorGroupEntity): VendorEntity {
      return stream(companyIn = companyIn, addressIn = null, paymentTermIn = paymentTermIn, shipViaIn = shipViaIn, vendorGroupIn = vendorGroupIn).findFirst().orElseThrow { Exception("Unable to create VendorEntity") }
   }

   fun single(companyIn: Company, paymentTermIn: VendorPaymentTermEntity, shipViaIn: ShipViaEntity, nameIn: String): VendorEntity {
      return stream(companyIn = companyIn, paymentTermIn = paymentTermIn, shipViaIn = shipViaIn, nameIn = nameIn).findFirst().orElseThrow { Exception("Unable to create VendorEntity") }
   }

   fun single(companyIn: Company, addressIn: AddressEntity, paymentTermIn: VendorPaymentTermEntity, shipViaIn: ShipViaEntity, nameIn: String): VendorEntity {
      return stream(companyIn = companyIn, addressIn = addressIn, paymentTermIn = paymentTermIn, shipViaIn = shipViaIn, nameIn = nameIn).findFirst().orElseThrow { Exception("Unable to create VendorEntity") }
   }

   fun single(companyIn: Company, addressIn: AddressEntity, paymentTermIn: VendorPaymentTermEntity, shipViaIn: ShipViaEntity): VendorEntity {
      return stream(companyIn = companyIn, addressIn = addressIn, paymentTermIn = paymentTermIn, shipViaIn = shipViaIn).findFirst().orElseThrow { Exception("Unable to create VendorEntity") }
   }
}
