package com.cynergisuite.middleware.purchase.order.detail

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableEntity
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.purchase.order.PurchaseOrderDTO
import com.cynergisuite.middleware.purchase.order.PurchaseOrderEntity
import com.cynergisuite.middleware.purchase.order.detail.infrastructure.PurchaseOrderDetailRepository
import com.cynergisuite.middleware.purchase.order.type.ExceptionIndicatorTypeDTO
import com.cynergisuite.middleware.purchase.order.type.ExceptionIndicatorTypeTestDataLoader
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderRequisitionIndicatorTypeDTO
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderRequisitionIndicatorTypeTestDataLoader
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderStatusTypeTestDataLoader
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderStatusTypeValueObject
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.vendor.VendorEntity
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class PurchaseOrderDetailDataLoader {

   private static final AtomicLong poDetailNumber = new AtomicLong(1)

   static Stream<PurchaseOrderDetailEntity> stream(
      int numberIn = 1,
      PurchaseOrderEntity purchaseOrderIn,
      Store shipToIn,
      VendorEntity vendorIn
   ) {
      final number = numberIn < 0 ? 1 : numberIn
      final faker = new Faker()
      final lorem = faker.lorem()
      final numbers = faker.number()
      final random = faker.random()

      return IntStream.range(0, number).mapToObj {
         new PurchaseOrderDetailEntity(
            null,
            poDetailNumber.getAndIncrement(),
            purchaseOrderIn,
            lorem.words(1).toString(),
            numbers.numberBetween(1, 100),
            numbers.numberBetween(1, 100),
            numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            lorem.sentence(),
            numbers.numberBetween(1, 100),
            numbers.numberBetween(1, 100),
            numbers.numberBetween(1, 100),
            numbers.numberBetween(1, 100),
            new SimpleLegacyIdentifiableEntity(shipToIn.myId()),
            LocalDate.now(),
            LocalDate.now(),
            numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            numbers.numberBetween(1, 100),
            vendorIn,
            LocalDate.now(),
            numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            PurchaseOrderStatusTypeTestDataLoader.random(),
            PurchaseOrderRequisitionIndicatorTypeTestDataLoader.random(),
            ExceptionIndicatorTypeTestDataLoader.random(),
            numbers.numberBetween(1, 100),
            random.nextBoolean()
         )
      }
   }

   static Stream<PurchaseOrderDetailDTO> streamDTO(
      int numberIn = 1,
      PurchaseOrderDTO purchaseOrderIn,
      SimpleLegacyIdentifiableDTO shipToIn,
      SimpleIdentifiableDTO vendorIn
   ) {
      final number = numberIn < 0 ? 1 : numberIn
      final faker = new Faker()
      final lorem = faker.lorem()
      final numbers = faker.number()
      final random = faker.random()

      return IntStream.range(0, number).mapToObj {
         new PurchaseOrderDetailDTO(
            null,
            poDetailNumber.getAndIncrement(),
            purchaseOrderIn,
            lorem.words(1).toString(),
            numbers.numberBetween(1, 100),
            numbers.numberBetween(1, 100),
            numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            lorem.sentence(),
            numbers.numberBetween(1, 100),
            numbers.numberBetween(1, 100),
            numbers.numberBetween(1, 100),
            numbers.numberBetween(1, 100),
            new SimpleLegacyIdentifiableDTO(shipToIn.myId()),
            LocalDate.now(),
            LocalDate.now(),
            numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            numbers.numberBetween(1, 100),
            vendorIn,
            LocalDate.now(),
            numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            new PurchaseOrderStatusTypeValueObject(PurchaseOrderStatusTypeTestDataLoader.random()),
            new PurchaseOrderRequisitionIndicatorTypeDTO(PurchaseOrderRequisitionIndicatorTypeTestDataLoader.random()),
            new ExceptionIndicatorTypeDTO(ExceptionIndicatorTypeTestDataLoader.random()),
            numbers.numberBetween(1, 100),
            random.nextBoolean()
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class PurchaseOrderDetailDataLoaderService {
   private final PurchaseOrderDetailRepository purchaseOrderDetailRepository

   @Inject
   PurchaseOrderDetailDataLoaderService(PurchaseOrderDetailRepository purchaseOrderDetailRepository) {
      this.purchaseOrderDetailRepository = purchaseOrderDetailRepository
   }

   Stream<PurchaseOrderDetailEntity> stream(int numberIn = 1, CompanyEntity companyIn, PurchaseOrderEntity purchaseOrderIn, Store shipToIn, VendorEntity vendorIn) {
      return PurchaseOrderDetailDataLoader.stream(numberIn, purchaseOrderIn, shipToIn, vendorIn)
         .map { purchaseOrderDetailRepository.insert(it, companyIn) }
   }

   PurchaseOrderDetailEntity single(CompanyEntity companyIn, PurchaseOrderEntity purchaseOrderIn, Store shipToIn, VendorEntity vendorIn) {
      return stream(1, companyIn, purchaseOrderIn, shipToIn, vendorIn)
         .findFirst().orElseThrow { new Exception("Unable to create PurchaseOrderDetailEntity") }
   }

   PurchaseOrderDetailDTO singleDTO(PurchaseOrderDTO purchaseOrderIn, SimpleLegacyIdentifiableDTO shipToIn, SimpleIdentifiableDTO vendorIn) {
      return PurchaseOrderDetailDataLoader.streamDTO(1, purchaseOrderIn, shipToIn, vendorIn)
         .findFirst().orElseThrow { new Exception("Unable to create PurchaseOrderDetail") }
   }
}
