package com.cynergisuite.middleware.purchase.order.detail

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.purchase.order.PurchaseOrderDTO
import com.cynergisuite.middleware.purchase.order.PurchaseOrderEntity
import com.cynergisuite.middleware.purchase.order.detail.infrastructure.PurchaseOrderDetailRepository
import com.cynergisuite.middleware.purchase.order.type.*
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.vendor.VendorEntity
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object PurchaseOrderDetailDataLoader {

   @JvmStatic
   private val poDetailNumber = AtomicLong(1)

   @JvmStatic
   fun stream(
      numberIn: Int = 1,
      purchaseOrderIn: PurchaseOrderEntity,
      shipToIn: Store,
      vendorIn: VendorEntity
   ): Stream<PurchaseOrderDetailEntity> {
      val number = if (numberIn < 0) 1 else numberIn
      val faker = Faker()
      val lorem = faker.lorem()
      val numbers = faker.number()
      val random = faker.random()

      return IntStream.range(0, number).mapToObj {
         PurchaseOrderDetailEntity(
            id = null,
            number = poDetailNumber.getAndIncrement(),
            purchaseOrder = purchaseOrderIn,
            sequence = null,
            itemfileNumber = lorem.words(1).toString(),
            orderQuantity = numbers.numberBetween(1, 100),
            receivedQuantity = numbers.numberBetween(1, 100),
            cost = numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            message = lorem.sentence(),
            color = numbers.numberBetween(1, 100),
            fabric = numbers.numberBetween(1, 100),
            cancelledQuantity = numbers.numberBetween(1, 100),
            cancelledTempQuantity = numbers.numberBetween(1, 100),
            shipTo = SimpleIdentifiableEntity(shipToIn.myId()),
            requiredDate = LocalDate.now(),
            dateOrdered = LocalDate.now(),
            freightPerItem = numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            tempQuantityToReceive = numbers.numberBetween(1, 100),
            vendor = vendorIn,
            lastReceivedDate = LocalDate.now(),
            landedCost = numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            statusType = PurchaseOrderStatusTypeFactory.random(),
            purchaseOrderRequisitionIndicatorType = PurchaseOrderRequisitionIndicatorTypeDataLoader.random(),
            exceptionIndicatorType = ExceptionIndicatorTypeDataLoader.random(),
            convertedPurchaseOrderNumber = numbers.numberBetween(1, 100),
            approvedIndicator = random.nextBoolean()
         )
      }
   }

   @JvmStatic
   fun streamDTO(
      numberIn: Int = 1,
      purchaseOrderIn: PurchaseOrderDTO,
      shipToIn: SimpleIdentifiableDTO,
      vendorIn: SimpleIdentifiableDTO
   ): Stream<PurchaseOrderDetailDTO> {
      val number = if (numberIn < 0) 1 else numberIn
      val faker = Faker()
      val lorem = faker.lorem()
      val numbers = faker.number()
      val random = faker.random()

      return IntStream.range(0, number).mapToObj {
         PurchaseOrderDetailDTO(
            id = null,
            number = poDetailNumber.getAndIncrement(),
            purchaseOrder = purchaseOrderIn,
            sequence = null,
            itemfileNumber = lorem.words(1).toString(),
            orderQuantity = numbers.numberBetween(1, 100),
            receivedQuantity = numbers.numberBetween(1, 100),
            cost = numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            message = lorem.sentence(),
            color = numbers.numberBetween(1, 100),
            fabric = numbers.numberBetween(1, 100),
            cancelledQuantity = numbers.numberBetween(1, 100),
            cancelledTempQuantity = numbers.numberBetween(1, 100),
            shipTo = SimpleIdentifiableDTO(shipToIn.myId()),
            requiredDate = LocalDate.now(),
            dateOrdered = LocalDate.now(),
            freightPerItem = numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            tempQuantityToReceive = numbers.numberBetween(1, 100),
            vendor = vendorIn,
            lastReceivedDate = LocalDate.now(),
            landedCost = numbers.randomDouble(2, 1, 1000000).toBigDecimal(),
            statusType = PurchaseOrderStatusTypeValueObject(PurchaseOrderStatusTypeFactory.random()),
            purchaseOrderRequisitionIndicatorType = PurchaseOrderRequisitionIndicatorTypeDTO(PurchaseOrderRequisitionIndicatorTypeDataLoader.random()),
            exceptionIndicatorType = ExceptionIndicatorTypeDTO(ExceptionIndicatorTypeDataLoader.random()),
            convertedPurchaseOrderNumber = numbers.numberBetween(1, 100),
            approvedIndicator = random.nextBoolean()
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class PurchaseOrderDetailDataLoaderService @Inject constructor(
   private val purchaseOrderDetailRepository: PurchaseOrderDetailRepository
) {

   fun stream(numberIn: Int = 1, companyIn: Company, purchaseOrderIn: PurchaseOrderEntity, shipToIn: Store, vendorIn: VendorEntity): Stream<PurchaseOrderDetailEntity> {
      return PurchaseOrderDetailDataLoader.stream(numberIn, purchaseOrderIn, shipToIn, vendorIn)
         .map { purchaseOrderDetailRepository.insert(it, companyIn) }
   }

   fun single(companyIn: Company, purchaseOrderIn: PurchaseOrderEntity, shipToIn: Store, vendorIn: VendorEntity): PurchaseOrderDetailEntity {
      return stream(1, companyIn, purchaseOrderIn, shipToIn, vendorIn)
         .findFirst().orElseThrow { Exception("Unable to create PurchaseOrderDetailEntity") }
   }

   fun singleDTO(purchaseOrderIn: PurchaseOrderDTO, shipToIn: SimpleIdentifiableDTO, vendorIn: SimpleIdentifiableDTO): PurchaseOrderDetailDTO {
      return PurchaseOrderDetailDataLoader.streamDTO(1, purchaseOrderIn, shipToIn, vendorIn)
         .findFirst().orElseThrow { Exception("Unable to create PurchaseOrderDetail") }
   }
}
