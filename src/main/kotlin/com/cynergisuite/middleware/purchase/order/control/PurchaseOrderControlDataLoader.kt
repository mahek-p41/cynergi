package com.cynergisuite.middleware.purchase.order.control

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.payable.DefaultAccountPayableStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.DefaultAccountPayableStatusTypeDataLoader
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.purchase.order.ApprovalRequiredFlagDTO
import com.cynergisuite.middleware.purchase.order.ApprovalRequiredFlagTypeFactory
import com.cynergisuite.middleware.purchase.order.DefaultPurchaseOrderTypeDTO
import com.cynergisuite.middleware.purchase.order.DefaultPurchaseOrderTypeDataLoader
import com.cynergisuite.middleware.purchase.order.UpdatePurchaseOrderCostTypeFactory
import com.cynergisuite.middleware.purchase.order.UpdatePurchaseOrderCostTypeValueObject
import com.cynergisuite.middleware.purchase.order.control.infrastructure.PurchaseOrderControlRepository
import com.cynergisuite.middleware.vendor.VendorEntity
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

object PurchaseOrderControlDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1, defaultVendor: VendorEntity, defaultApprover: EmployeeEntity): Stream<PurchaseOrderControlEntity> {
      val number = if (numberIn > 0) numberIn else 1

      return IntStream.range(0, number).mapToObj {
         PurchaseOrderControlEntity(
            id = Random.nextLong(),
            dropFiveCharactersOnModelNumber = Random.nextBoolean(),
            updateAccountPayable = Random.nextBoolean(),
            printSecondDescription = Random.nextBoolean(),
            defaultAccountPayableStatusType = DefaultAccountPayableStatusTypeDataLoader.random(),
            printVendorComments = Random.nextBoolean(),
            includeFreightInCost = Random.nextBoolean(),
            updateCostOnModel = Random.nextBoolean(),
            defaultVendor = defaultVendor,
            updatePurchaseOrderCost = UpdatePurchaseOrderCostTypeFactory.random(),
            defaultPurchaseOrderType = DefaultPurchaseOrderTypeDataLoader.random(),
            sortByShipToOnPrint = Random.nextBoolean(),
            invoiceByLocation = Random.nextBoolean(),
            validateInventory = Random.nextBoolean(),
            defaultApprover = defaultApprover,
            approvalRequiredFlagType = ApprovalRequiredFlagTypeFactory.random()
         )
      }
   }

   @JvmStatic
   fun streamDTO(
      numberIn: Int = 1,
      defaultVendor: SimpleIdentifiableDTO?,
      defaultApprover: SimpleIdentifiableDTO?
   ): Stream<PurchaseOrderControlDTO> {
      val number = if (numberIn > 0) numberIn else 1

      return IntStream.range(0, number).mapToObj {
         PurchaseOrderControlDTO(
            dropFiveCharactersOnModelNumber = Random.nextBoolean(),
            updateAccountPayable = Random.nextBoolean(),
            printSecondDescription = Random.nextBoolean(),
            defaultAccountPayableStatusType = DefaultAccountPayableStatusTypeDTO(DefaultAccountPayableStatusTypeDataLoader.random()),
            printVendorComments = Random.nextBoolean(),
            includeFreightInCost = Random.nextBoolean(),
            updateCostOnModel = Random.nextBoolean(),
            defaultVendor = defaultVendor,
            updatePurchaseOrderCost = UpdatePurchaseOrderCostTypeValueObject(UpdatePurchaseOrderCostTypeFactory.random()),
            defaultPurchaseOrderType = DefaultPurchaseOrderTypeDTO(DefaultPurchaseOrderTypeDataLoader.random()),
            sortByShipToOnPrint = Random.nextBoolean(),
            invoiceByLocation = Random.nextBoolean(),
            validateInventory = Random.nextBoolean(),
            defaultApprover = defaultApprover,
            approvalRequiredFlagType = ApprovalRequiredFlagDTO(ApprovalRequiredFlagTypeFactory.random())
         )
      }
   }

   @Singleton
   @Requires(env = ["develop", "test"])
   class PurchaseOrderControlDataLoaderService @Inject constructor(
      private val purchaseOrderControlRepository: PurchaseOrderControlRepository
   ) {
      fun stream(numberIn: Int = 1, company: Company, defaultVendor: VendorEntity, defaultApprover: EmployeeEntity): Stream<PurchaseOrderControlEntity> {
         return PurchaseOrderControlDataLoader.stream(numberIn, defaultVendor, defaultApprover).map {
            purchaseOrderControlRepository.insert(it, company)
         }
      }

      fun single(company: Company, defaultVendor: VendorEntity, defaultApprover: EmployeeEntity): PurchaseOrderControlEntity {
         return stream(1, company, defaultVendor, defaultApprover).findFirst().orElseThrow { Exception("Unable to create PurchaseOrderControl") }
      }

      fun singleDTO(defaultVendor: SimpleIdentifiableDTO?, defaultApprover: SimpleIdentifiableDTO?): PurchaseOrderControlDTO {
         return PurchaseOrderControlDataLoader.streamDTO(1, defaultVendor, defaultApprover).findFirst().orElseThrow { Exception("Unable to create PurchaseOrderControl") }
      }
   }
}
