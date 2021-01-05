package com.cynergisuite.middleware.purchase.order

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.purchase.order.infrastructure.PurchaseOrderRepository
import com.cynergisuite.middleware.purchase.order.type.ExceptionIndicatorTypeDTO
import com.cynergisuite.middleware.purchase.order.type.ExceptionIndicatorTypeDataLoader
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderStatusTypeFactory
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderStatusTypeValueObject
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderTypeFactory
import com.cynergisuite.middleware.purchase.order.type.PurchaseOrderTypeValueObject
import com.cynergisuite.middleware.shipping.freight.onboard.FreightOnboardTypeDTO
import com.cynergisuite.middleware.shipping.freight.onboard.FreightOnboardTypeTestDataLoader
import com.cynergisuite.middleware.shipping.freight.term.FreightTermTypeDTO
import com.cynergisuite.middleware.shipping.freight.term.FreightTermTypeDataLoader
import com.cynergisuite.middleware.shipping.location.ShipLocationTypeDTO
import com.cynergisuite.middleware.shipping.location.ShipLocationTypeDataLoader
import com.cynergisuite.middleware.shipping.shipvia.ShipViaEntity
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.vendor.VendorEntity
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermEntity
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.math.RoundingMode
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object PurchaseOrderDataLoader {

   @JvmStatic
   private val poNumber = AtomicLong(1)

   @JvmStatic
   fun stream(
      numberIn: Int = 1,
      vendorIn: VendorEntity,
      approvedByIn: EmployeeEntity,
      purchaseAgentIn: EmployeeEntity,
      shipViaIn: ShipViaEntity,
      shipToIn: Store,
      paymentTermTypeIn: VendorPaymentTermEntity,
      vendorSubmittedEmployeeIn: EmployeeEntity? = null,
      customerAccountIn: AccountEntity? = null
   ): Stream<PurchaseOrderEntity> {
      val number = if (numberIn < 0) 1 else numberIn
      val faker = Faker()
      val lorem = faker.lorem()
      val numbers = faker.number()
      val random = faker.random()

      return IntStream.range(0, number).mapToObj {
         PurchaseOrderEntity(
            id = null,
            number = poNumber.getAndIncrement(),
            vendor = vendorIn,
            statusType = PurchaseOrderStatusTypeFactory.random(),
            orderDate = LocalDate.now(),
            type = PurchaseOrderTypeFactory.random(),
            freightOnboardType = FreightOnboardTypeTestDataLoader.random(),
            freightTermType = FreightTermTypeDataLoader.random(),
            shipLocationType = ShipLocationTypeDataLoader.random(),
            approvedBy = approvedByIn,
            totalAmount = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            receivedAmount = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            paidAmount = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            purchaseAgent = purchaseAgentIn,
            shipVia = shipViaIn,
            requiredDate = LocalDate.now(),
            shipTo = shipToIn,
            paymentTermType = paymentTermTypeIn,
            message = lorem.sentence(),
            totalLandedAmount = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            totalFreightAmount = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            exceptionIndicatorType = ExceptionIndicatorTypeDataLoader.random(),
            vendorSubmittedTime = OffsetDateTime.now(),
            vendorSubmittedEmployee = vendorSubmittedEmployeeIn,
            ecommerceIndicator = random.nextBoolean(),
            customerAccount = customerAccountIn
         )
      }
   }

   @JvmStatic
   fun streamDTO(
      numberIn: Int = 1,
      vendorIn: SimpleIdentifiableDTO,
      approvedByIn: EmployeeEntity,
      purchaseAgentIn: EmployeeEntity,
      shipViaIn: SimpleIdentifiableDTO,
      shipToIn: SimpleIdentifiableDTO,
      paymentTermTypeIn: SimpleIdentifiableDTO,
      vendorSubmittedEmployeeIn: EmployeeEntity? = null,
      customerAccountIn: SimpleIdentifiableDTO? = null
   ): Stream<PurchaseOrderDTO> {
      val number = if (numberIn < 0) 1 else numberIn
      val faker = Faker()
      val lorem = faker.lorem()
      val numbers = faker.number()
      val random = faker.random()

      return IntStream.range(0, number).mapToObj {
         PurchaseOrderDTO(
            id = null,
            number = poNumber.getAndIncrement(),
            vendor = vendorIn,
            statusType = PurchaseOrderStatusTypeValueObject(PurchaseOrderStatusTypeFactory.random()),
            orderDate = LocalDate.now(),
            type = PurchaseOrderTypeValueObject(PurchaseOrderTypeFactory.random()),
            freightOnboardType = FreightOnboardTypeDTO(FreightOnboardTypeTestDataLoader.random()),
            freightTermType = FreightTermTypeDTO(FreightTermTypeDataLoader.random()),
            shipLocationType = ShipLocationTypeDTO(ShipLocationTypeDataLoader.random()),
            approvedBy = EmployeeValueObject(approvedByIn),
            totalAmount = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            receivedAmount = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            paidAmount = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            purchaseAgent = EmployeeValueObject(purchaseAgentIn),
            shipVia = shipViaIn,
            requiredDate = LocalDate.now(),
            shipTo = shipToIn,
            paymentTermType = paymentTermTypeIn,
            message = lorem.sentence(),
            totalLandedAmount = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            totalFreightAmount = numbers.numberBetween(1, 10_000).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            exceptionIndicatorType = ExceptionIndicatorTypeDTO(ExceptionIndicatorTypeDataLoader.random()),
            vendorSubmittedTime = OffsetDateTime.now(),
            vendorSubmittedEmployee = vendorSubmittedEmployeeIn?.let { it -> EmployeeValueObject(it) },
            ecommerceIndicator = random.nextBoolean(),
            customerAccount = customerAccountIn
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class PurchaseOrderDataLoaderService @Inject constructor(
   private val purchaseOrderRepository: PurchaseOrderRepository
) {

   fun stream(
      numberIn: Int = 1,
      companyIn: Company,
      vendorIn: VendorEntity,
      approvedByIn: EmployeeEntity,
      purchaseAgentIn: EmployeeEntity,
      shipViaIn: ShipViaEntity,
      shipToIn: Store,
      paymentTermTypeIn: VendorPaymentTermEntity,
      vendorSubmittedEmployeeIn: EmployeeEntity? = null,
      customerAccountIn: AccountEntity? = null
   ): Stream<PurchaseOrderEntity> {
      return PurchaseOrderDataLoader.stream(numberIn, vendorIn, approvedByIn, purchaseAgentIn, shipViaIn, shipToIn, paymentTermTypeIn, vendorSubmittedEmployeeIn, customerAccountIn)
         .map { purchaseOrderRepository.insert(it, companyIn) }
   }

   fun single(
      companyIn: Company,
      vendorIn: VendorEntity,
      approvedByIn: EmployeeEntity,
      purchaseAgentIn: EmployeeEntity,
      shipViaIn: ShipViaEntity,
      shipToIn: Store,
      paymentTermTypeIn: VendorPaymentTermEntity,
      vendorSubmittedEmployeeIn: EmployeeEntity? = null,
      customerAccountIn: AccountEntity? = null
   ): PurchaseOrderEntity {
      return stream(1, companyIn, vendorIn, approvedByIn, purchaseAgentIn, shipViaIn, shipToIn, paymentTermTypeIn, vendorSubmittedEmployeeIn, customerAccountIn)
         .findFirst().orElseThrow { Exception("Unable to create PurchaseOrderEntity") }
   }

   fun singleDTO(
      vendorIn: SimpleIdentifiableDTO,
      approvedByIn: EmployeeEntity,
      purchaseAgentIn: EmployeeEntity,
      shipViaIn: SimpleIdentifiableDTO,
      shipToIn: SimpleIdentifiableDTO,
      paymentTermTypeIn: SimpleIdentifiableDTO,
      vendorSubmittedEmployeeIn: EmployeeEntity? = null,
      customerAccountIn: SimpleIdentifiableDTO? = null
   ): PurchaseOrderDTO {
      return PurchaseOrderDataLoader.streamDTO(1, vendorIn, approvedByIn, purchaseAgentIn, shipViaIn, shipToIn, paymentTermTypeIn, vendorSubmittedEmployeeIn, customerAccountIn)
         .findFirst().orElseThrow { Exception("Unable to create PurchaseOrder") }
   }
}
