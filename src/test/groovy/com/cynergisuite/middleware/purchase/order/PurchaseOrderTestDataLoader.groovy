package com.cynergisuite.middleware.purchase.order

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.purchase.order.infrastructure.PurchaseOrderRepository
import com.cynergisuite.middleware.purchase.order.type.ExceptionIndicatorTypeDTO
import com.cynergisuite.middleware.purchase.order.type.ExceptionIndicatorTypeTestDataLoader
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderStatusTypeTestDataLoader
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderStatusTypeValueObject
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderTypeTestDataLoader
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderTypeValueObject
import com.cynergisuite.middleware.shipping.freight.onboard.FreightOnboardTypeDTO
import com.cynergisuite.middleware.shipping.freight.onboard.FreightOnboardTypeTestDataLoader
import com.cynergisuite.middleware.shipping.freight.term.FreightTermTypeDTO
import com.cynergisuite.middleware.shipping.freight.term.FreightTermTypeTestDataLoader
import com.cynergisuite.middleware.shipping.location.ShipLocationTypeDTO
import com.cynergisuite.middleware.shipping.location.ShipLocationTypeTestDataLoader
import com.cynergisuite.middleware.shipping.shipvia.ShipViaEntity
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.vendor.VendorEntity
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermEntity
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import javax.inject.Singleton
import java.math.RoundingMode
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.IntStream
import java.util.stream.Stream

class PurchaseOrderTestDataLoader {
   private static final AtomicLong poNumber = new AtomicLong(1)

   static Stream<PurchaseOrderEntity> stream(
      int numberIn = 1,
      VendorEntity vendorIn,
      EmployeeEntity approvedByIn,
      EmployeeEntity purchaseAgentIn,
      ShipViaEntity shipViaIn,
      Store shipToIn,
      VendorPaymentTermEntity paymentTermTypeIn,
      EmployeeEntity vendorSubmittedEmployeeIn = null
   ) {
      final number = numberIn < 0 ? 1 : numberIn
      final faker = new Faker()
      final lorem = faker.lorem()
      final numbers = faker.number()
      final random = faker.random()

      return IntStream.range(0, number).mapToObj {
         new PurchaseOrderEntity(
            null,
            poNumber.getAndIncrement(),
            vendorIn,
            PurchaseOrderStatusTypeTestDataLoader.random(),
            LocalDate.now(),
            PurchaseOrderTypeTestDataLoader.random(),
            FreightOnboardTypeTestDataLoader.random(),
            FreightTermTypeTestDataLoader.random(),
            ShipLocationTypeTestDataLoader.random(),
            approvedByIn,
            numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            purchaseAgentIn,
            shipViaIn,
            LocalDate.now(),
            shipToIn,
            paymentTermTypeIn,
            lorem.sentence(),
            numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            ExceptionIndicatorTypeTestDataLoader.random(),
            OffsetDateTime.now(),
            vendorSubmittedEmployeeIn,
            random.nextBoolean(),
         )
      }
   }

   static Stream<PurchaseOrderDTO> streamDTO(
      int numberIn = 1,
      SimpleIdentifiableDTO vendorIn,
      EmployeeEntity approvedByIn,
      EmployeeEntity purchaseAgentIn,
      SimpleIdentifiableDTO shipViaIn,
      SimpleLegacyIdentifiableDTO shipToIn,
      SimpleIdentifiableDTO paymentTermTypeIn,
      EmployeeEntity vendorSubmittedEmployeeIn = null
   ) {
      final number = numberIn < 0 ? 1 : numberIn
      final faker = new Faker()
      final lorem = faker.lorem()
      final numbers = faker.number()
      final random = faker.random()

      return IntStream.range(0, number).mapToObj {
         new PurchaseOrderDTO(
            null,
            poNumber.getAndIncrement(),
            vendorIn,
            new PurchaseOrderStatusTypeValueObject(PurchaseOrderStatusTypeTestDataLoader.random()),
            LocalDate.now(),
            new PurchaseOrderTypeValueObject(PurchaseOrderTypeTestDataLoader.random()),
            new FreightOnboardTypeDTO(FreightOnboardTypeTestDataLoader.random()),
            new FreightTermTypeDTO(FreightTermTypeTestDataLoader.random()),
            new ShipLocationTypeDTO(ShipLocationTypeTestDataLoader.random()),
            new EmployeeValueObject(approvedByIn),
            numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            new EmployeeValueObject(purchaseAgentIn),
            shipViaIn,
            LocalDate.now(),
            shipToIn,
            paymentTermTypeIn,
            lorem.sentence(),
            numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            new ExceptionIndicatorTypeDTO(ExceptionIndicatorTypeTestDataLoader.random()),
            OffsetDateTime.now(),
            vendorSubmittedEmployeeIn?.with { emp -> new EmployeeValueObject(emp) },
            random.nextBoolean(),
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class PurchaseOrderTestDataLoaderService {
   private final PurchaseOrderRepository purchaseOrderRepository

   PurchaseOrderTestDataLoaderService(PurchaseOrderRepository purchaseOrderRepository) {
      this.purchaseOrderRepository = purchaseOrderRepository
   }

   Stream<PurchaseOrderEntity> stream(
      int numberIn = 1,
      CompanyEntity companyIn,
      VendorEntity vendorIn,
      EmployeeEntity approvedByIn,
      EmployeeEntity purchaseAgentIn,
      ShipViaEntity shipViaIn,
      Store shipToIn,
      VendorPaymentTermEntity paymentTermTypeIn,
      EmployeeEntity vendorSubmittedEmployeeIn = null
   ) {
      return PurchaseOrderTestDataLoader.stream(numberIn, vendorIn, approvedByIn, purchaseAgentIn, shipViaIn, shipToIn, paymentTermTypeIn, vendorSubmittedEmployeeIn)
         .map { purchaseOrderRepository.insert(it, companyIn) }
   }

   PurchaseOrderEntity single(
      CompanyEntity companyIn,
      VendorEntity vendorIn,
      EmployeeEntity approvedByIn,
      EmployeeEntity purchaseAgentIn,
      ShipViaEntity shipViaIn,
      Store shipToIn,
      VendorPaymentTermEntity paymentTermTypeIn,
      EmployeeEntity vendorSubmittedEmployeeIn = null
   ) {
      return stream(1, companyIn, vendorIn, approvedByIn, purchaseAgentIn, shipViaIn, shipToIn, paymentTermTypeIn, vendorSubmittedEmployeeIn)
         .findFirst().orElseThrow { new Exception("Unable to create PurchaseOrderEntity") }
   }

   PurchaseOrderDTO singleDTO(
      SimpleIdentifiableDTO vendorIn,
      EmployeeEntity approvedByIn,
      EmployeeEntity purchaseAgentIn,
      SimpleIdentifiableDTO shipViaIn,
      SimpleLegacyIdentifiableDTO shipToIn,
      SimpleIdentifiableDTO paymentTermTypeIn,
      EmployeeEntity vendorSubmittedEmployeeIn = null
   ) {
      return PurchaseOrderTestDataLoader.streamDTO(1, vendorIn, approvedByIn, purchaseAgentIn, shipViaIn, shipToIn, paymentTermTypeIn, vendorSubmittedEmployeeIn)
         .findFirst().orElseThrow { new Exception("Unable to create PurchaseOrder") }
   }
}
