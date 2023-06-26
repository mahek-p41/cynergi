package com.cynergisuite.middleware.purchase.order.control

import com.cynergisuite.middleware.accounting.account.payable.DefaultAccountPayableStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.DefaultAccountPayableStatusTypeDataLoader
import com.cynergisuite.middleware.authentication.user.SecurityGroup
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.purchase.order.control.infrastructure.PurchaseOrderControlRepository
import com.cynergisuite.middleware.purchase.order.type.ApprovalRequiredFlagDTO
import com.cynergisuite.middleware.purchase.order.type.ApprovalRequiredFlagTypeTestDataLoader
import com.cynergisuite.middleware.purchase.order.type.DefaultPurchaseOrderTypeDTO
import com.cynergisuite.middleware.purchase.order.type.DefaultPurchaseOrderTypeTestDataLoader
import com.cynergisuite.middleware.purchase.order.type.UpdatePurchaseOrderCostTypeTestDataLoader
import com.cynergisuite.middleware.purchase.order.type.UpdatePurchaseOrderCostTypeValueObject
import com.cynergisuite.middleware.vendor.VendorDTO
import com.cynergisuite.middleware.vendor.VendorEntity
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

import java.time.OffsetDateTime
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class PurchaseOrderControlTestDataLoader {

   static Stream<PurchaseOrderControlEntity> stream(int numberIn = 1, VendorEntity defaultVendor, EmployeeEntity defaultApprover) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final random = faker.random()

      return IntStream.range(0, number).mapToObj {
         new PurchaseOrderControlEntity(
            null,
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            random.nextBoolean(),
            random.nextBoolean(),
            random.nextBoolean(),
            DefaultAccountPayableStatusTypeDataLoader.random(),
            random.nextBoolean(),
            random.nextBoolean(),
            random.nextBoolean(),
            defaultVendor,
            UpdatePurchaseOrderCostTypeTestDataLoader.random(),
            DefaultPurchaseOrderTypeTestDataLoader.random(),
            random.nextBoolean(),
            random.nextBoolean(),
            random.nextBoolean(),
            defaultApprover,
            ApprovalRequiredFlagTypeTestDataLoader.random()
         )
      }
   }

   static Stream<PurchaseOrderControlDTO> streamDTO(
      int numberIn = 1,
      VendorDTO defaultVendor,
      EmployeeEntity defaultApprover
   ) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final Random = faker.random()

      return IntStream.range(0, number).mapToObj {
         new PurchaseOrderControlDTO(
            null,
            Random.nextBoolean(),
            Random.nextBoolean(),
            Random.nextBoolean(),
            new DefaultAccountPayableStatusTypeDTO(DefaultAccountPayableStatusTypeDataLoader.random()),
            Random.nextBoolean(),
            Random.nextBoolean(),
            Random.nextBoolean(),
            defaultVendor,
            new UpdatePurchaseOrderCostTypeValueObject(UpdatePurchaseOrderCostTypeTestDataLoader.random()),
            new DefaultPurchaseOrderTypeDTO(DefaultPurchaseOrderTypeTestDataLoader.random()),
            Random.nextBoolean(),
            Random.nextBoolean(),
            Random.nextBoolean(),
            new EmployeeValueObject(defaultApprover.myId(), null, null, null, null, null, null, null, null, null, null),
            new ApprovalRequiredFlagDTO(ApprovalRequiredFlagTypeTestDataLoader.random())
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class PurchaseOrderControlTestDataLoaderService {
   private final PurchaseOrderControlRepository purchaseOrderControlRepository

   PurchaseOrderControlTestDataLoaderService(PurchaseOrderControlRepository purchaseOrderControlRepository) {
      this.purchaseOrderControlRepository = purchaseOrderControlRepository
   }

   Stream<PurchaseOrderControlEntity> stream(int numberIn = 1, CompanyEntity company, VendorEntity defaultVendor, EmployeeEntity defaultApprover) {
      return PurchaseOrderControlTestDataLoader.stream(numberIn, defaultVendor, defaultApprover).map {
         purchaseOrderControlRepository.insert(it, company)
      }
   }

   PurchaseOrderControlEntity single(CompanyEntity company, VendorEntity defaultVendor, EmployeeEntity defaultApprover) {
      return stream(1, company, defaultVendor, defaultApprover).findFirst().orElseThrow { new Exception("Unable to create PurchaseOrderControl") }
   }

   PurchaseOrderControlDTO singleDTO(VendorDTO defaultVendor, EmployeeEntity defaultApprover) {
      return PurchaseOrderControlTestDataLoader.streamDTO(1, defaultVendor, defaultApprover).findFirst().orElseThrow { new Exception("Unable to create PurchaseOrderControl") }
   }
}

