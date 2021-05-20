package com.cynergisuite.middleware.purchase.order.control

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.DefaultAccountPayableStatusTypeRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.ConfigAlreadyExist
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.purchase.order.control.infrastructure.PurchaseOrderControlRepository
import com.cynergisuite.middleware.purchase.order.type.infrastructure.ApprovalRequiredFlagTypeRepository
import com.cynergisuite.middleware.purchase.order.type.infrastructure.DefaultPurchaseOrderTypeRepository
import com.cynergisuite.middleware.purchase.order.type.infrastructure.UpdatePurchaseOrderCostTypeRepository
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseOrderControlValidator @Inject constructor(
   private val defaultAccountPayableStatusTypeRepository: DefaultAccountPayableStatusTypeRepository,
   private val vendorRepository: VendorRepository,
   private val updatePurchaseOrderCostTypeRepository: UpdatePurchaseOrderCostTypeRepository,
   private val defaultPurchaseOrderTypeRepository: DefaultPurchaseOrderTypeRepository,
   private val employeeRepository: EmployeeRepository,
   private val approvalRequiredFlagTypeRepository: ApprovalRequiredFlagTypeRepository,
   private val purchaseOrderControlRepository: PurchaseOrderControlRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(PurchaseOrderControlValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(dto: PurchaseOrderControlDTO, company: Company): PurchaseOrderControlEntity {
      logger.debug("Validating Create PurchaseOrderControl {}", dto)

      return doSharedValidation(dto, company)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: UUID, dto: PurchaseOrderControlDTO, company: Company): Pair<PurchaseOrderControlEntity, PurchaseOrderControlEntity> {
      logger.debug("Validating Update PurchaseOrderControl {}", dto)

      doValidation { errors ->
         if (!purchaseOrderControlRepository.exists(id)) {
            errors.add(ValidationError("id", NotFound(id)))
         }
      }

      val entity = purchaseOrderControlRepository.findOne(company)

      return Pair(entity!!, doSharedValidation(dto, company, entity))
   }

   private fun doSharedValidation(
      dto: PurchaseOrderControlDTO,
      company: Company,
      entity: PurchaseOrderControlEntity? = null
   ): PurchaseOrderControlEntity {
      val defaultAccountPayableStatusType = defaultAccountPayableStatusTypeRepository.findOne(dto.defaultAccountPayableStatusType!!.value)
      val defaultVendor = dto.defaultVendor?.id?.let { vendorRepository.findOne(it, company) }
      val updatePurchaseOrderCost = updatePurchaseOrderCostTypeRepository.findOne(dto.updatePurchaseOrderCost!!.value)
      val defaultPurchaseOrderType = defaultPurchaseOrderTypeRepository.findOne(dto.defaultPurchaseOrderType!!.value)
      val defaultApprover = dto.defaultApprover?.id?.let { employeeRepository.findOne(it, company) }
      val approvalRequiredFlagType = approvalRequiredFlagTypeRepository.findOne(dto.approvalRequiredFlagType!!.value)

      doValidation { errors ->
         if (purchaseOrderControlRepository.exists(company) && entity == null) {
            errors.add(ValidationError("company", ConfigAlreadyExist(company.myDataset())))
         }

         defaultAccountPayableStatusType
            ?: errors.add(ValidationError("defaultAccountPayableStatusType.value", NotFound(dto.defaultAccountPayableStatusType!!.value)))

         // nullable validation
         if (dto.defaultVendor?.id != null) {
            defaultVendor ?: errors.add(ValidationError("defaultVendor.id", NotFound(dto.defaultVendor!!.id!!)))
         }

         updatePurchaseOrderCost
            ?: errors.add(ValidationError("updatePurchaseOrderCost.value", NotFound(dto.updatePurchaseOrderCost!!.value)))

         defaultPurchaseOrderType
            ?: errors.add(ValidationError("defaultPurchaseOrderType.value", NotFound(dto.defaultPurchaseOrderType!!.value)))

         // nullable validation
         if (dto.defaultApprover?.id != null) {
            defaultApprover ?: errors.add(ValidationError("defaultApprover.id", NotFound(dto.defaultApprover!!.id!!)))
         }

         approvalRequiredFlagType
            ?: errors.add(ValidationError("approvalRequiredFlagType.value", NotFound(dto.approvalRequiredFlagType!!.value)))
      }

      return PurchaseOrderControlEntity(
         entity?.id,
         dto,
         defaultAccountPayableStatusType = defaultAccountPayableStatusType!!,
         defaultVendor = defaultVendor,
         updatePurchaseOrderCost = updatePurchaseOrderCost!!,
         defaultPurchaseOrderType = defaultPurchaseOrderType!!,
         defaultApprover = defaultApprover,
         approvalRequiredFlagType = approvalRequiredFlagType!!
      )
   }
}
