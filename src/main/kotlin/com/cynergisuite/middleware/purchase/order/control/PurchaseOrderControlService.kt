package com.cynergisuite.middleware.purchase.order.control

import com.cynergisuite.middleware.accounting.account.payable.DefaultAccountPayableStatusTypeDTO
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.employee.EmployeeService
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.purchase.order.control.infrastructure.PurchaseOrderControlRepository
import com.cynergisuite.middleware.purchase.order.type.ApprovalRequiredFlagDTO
import com.cynergisuite.middleware.purchase.order.type.DefaultPurchaseOrderTypeDTO
import com.cynergisuite.middleware.purchase.order.type.UpdatePurchaseOrderCostTypeValueObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseOrderControlService @Inject constructor(
   private val purchaseOrderControlValidator: PurchaseOrderControlValidator,
   private val purchaseOrderControlRepository: PurchaseOrderControlRepository,
   private val employeeService: EmployeeService
) {
   fun fetchOne(company: CompanyEntity): PurchaseOrderControlDTO? {
      return purchaseOrderControlRepository.findOne(company)?.let { transformEntity(it) }
   }

   fun create(dto: PurchaseOrderControlDTO, company: CompanyEntity): PurchaseOrderControlDTO {
      val toCreate = purchaseOrderControlValidator.validateCreate(dto, company)

      return transformEntity(purchaseOrderControlRepository.insert(toCreate, company))
   }

   fun update(id: UUID, dto: PurchaseOrderControlDTO, company: CompanyEntity): PurchaseOrderControlDTO {
      val (existing, toUpdate) = purchaseOrderControlValidator.validateUpdate(id, dto, company)

      return if (existing != toUpdate) {
         transformEntity(purchaseOrderControlRepository.update(toUpdate, company))
      } else {
         transformEntity(existing)
      }
   }

   private fun transformEntity(purchaseOrderControl: PurchaseOrderControlEntity): PurchaseOrderControlDTO {
      return PurchaseOrderControlDTO(
         entity = purchaseOrderControl,
         defaultAccountPayableStatusType = DefaultAccountPayableStatusTypeDTO(purchaseOrderControl.defaultAccountPayableStatusType),
         updatePurchaseOrderCost = UpdatePurchaseOrderCostTypeValueObject(purchaseOrderControl.updatePurchaseOrderCost),
         defaultPurchaseOrderType = DefaultPurchaseOrderTypeDTO(purchaseOrderControl.defaultPurchaseOrderType),
         approvalRequiredFlagType = ApprovalRequiredFlagDTO(purchaseOrderControl.approvalRequiredFlagType)
      )
   }

   fun fetchApprovers(user: User): List<EmployeeValueObject> {
      return employeeService.fetchPurchaseOrderApprovers(user)
   }
}
