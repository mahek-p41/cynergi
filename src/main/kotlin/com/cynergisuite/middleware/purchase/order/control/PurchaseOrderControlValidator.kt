package com.cynergisuite.middleware.purchase.order.control

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.payable.DefaultAccountPayableStatusType
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.DefaultAccountPayableStatusTypeRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.Duplicate
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.purchase.order.ApprovalRequiredFlagType
import com.cynergisuite.middleware.purchase.order.DefaultPurchaseOrderType
import com.cynergisuite.middleware.purchase.order.UpdatePurchaseOrderCostType
import com.cynergisuite.middleware.purchase.order.control.infrastructure.PurchaseOrderControlRepository
import com.cynergisuite.middleware.purchase.order.infrastructure.ApprovalRequiredFlagTypeRepository
import com.cynergisuite.middleware.purchase.order.infrastructure.DefaultPurchaseOrderTypeRepository
import com.cynergisuite.middleware.purchase.order.infrastructure.UpdatePurchaseOrderCostTypeRepository
import com.cynergisuite.middleware.vendor.VendorEntity
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

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
   fun validateCreate(@Valid dto: PurchaseOrderControlDTO, company: Company): PurchaseOrderControlEntity {
      logger.debug("Validating Create PurchaseOrderControl {}", dto)
      val defaultAccountPayableStatusType = defaultAccountPayableStatusTypeRepository.findOne(dto.defaultAccountPayableStatusType!!.value)
      val defaultVendor = dto.defaultVendor?.id?.let { vendorRepository.findOne(it, company) }
      val updatePurchaseOrderCost = updatePurchaseOrderCostTypeRepository.findOne(dto.updatePurchaseOrderCost!!.value)
      val defaultPurchaseOrderType = defaultPurchaseOrderTypeRepository.findOne(dto.defaultPurchaseOrderType!!.value)
      val defaultApprover = dto.defaultApprover?.id?.let { employeeRepository.findOne(it, company) }
      val approvalRequiredFlagType = approvalRequiredFlagTypeRepository.findOne(dto.approvalRequiredFlagType!!.value)

      doValidation { errors ->
         if (purchaseOrderControlRepository.exists(company)) {
            errors.add(ValidationError("company", Duplicate("Purchase order control for user's company " + company.myDataset())))
         }

         doSharedValidation(errors, dto, defaultAccountPayableStatusType, defaultVendor, updatePurchaseOrderCost, defaultPurchaseOrderType, defaultApprover, approvalRequiredFlagType)
      }

      return PurchaseOrderControlEntity(
         dto,
         defaultAccountPayableStatusType = defaultAccountPayableStatusType!!,
         defaultVendor = defaultVendor,
         updatePurchaseOrderCost = updatePurchaseOrderCost!!,
         defaultPurchaseOrderType = defaultPurchaseOrderType!!,
         defaultApprover = defaultApprover,
         approvalRequiredFlagType = approvalRequiredFlagType!!
      )
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: Long, dto: PurchaseOrderControlDTO, company: Company): PurchaseOrderControlEntity {
      logger.debug("Validating Update PurchaseOrderControl {}", dto)

      val defaultAccountPayableStatusType = defaultAccountPayableStatusTypeRepository.findOne(dto.defaultAccountPayableStatusType!!.value)
      val defaultVendor = vendorRepository.findOne(dto.defaultVendor!!.id!!, company)
      val updatePurchaseOrderCost = updatePurchaseOrderCostTypeRepository.findOne(dto.updatePurchaseOrderCost!!.value)
      val defaultPurchaseOrderType = defaultPurchaseOrderTypeRepository.findOne(dto.defaultPurchaseOrderType!!.value)
      val defaultApprover = employeeRepository.findOne(dto.defaultApprover!!.id!!, company)
      val approvalRequiredFlagType = approvalRequiredFlagTypeRepository.findOne(dto.approvalRequiredFlagType!!.value)

      doValidation { errors ->
         if (!purchaseOrderControlRepository.exists(id)) {
            errors.add(ValidationError("id", NotFound(id)))
         }

         doSharedValidation(errors, dto, defaultAccountPayableStatusType, defaultVendor, updatePurchaseOrderCost, defaultPurchaseOrderType, defaultApprover, approvalRequiredFlagType)
      }

      return PurchaseOrderControlEntity(
         dto,
         defaultAccountPayableStatusType = defaultAccountPayableStatusType!!,
         defaultVendor = defaultVendor!!,
         updatePurchaseOrderCost = updatePurchaseOrderCost!!,
         defaultPurchaseOrderType = defaultPurchaseOrderType!!,
         defaultApprover = defaultApprover!!,
         approvalRequiredFlagType = approvalRequiredFlagType!!
      )
   }

   private fun doSharedValidation(
      errors: MutableSet<ValidationError>,
      dto: PurchaseOrderControlDTO,
      defaultAccountPayableStatusType: DefaultAccountPayableStatusType?,
      defaultVendor: VendorEntity?,
      updatePurchaseOrderCost: UpdatePurchaseOrderCostType?,
      defaultPurchaseOrderType: DefaultPurchaseOrderType?,
      defaultApprover: EmployeeEntity?,
      approvalRequiredFlagType: ApprovalRequiredFlagType?
   ) {
      defaultAccountPayableStatusType ?: errors.add(ValidationError("defaultAccountPayableStatusType.value", NotFound(dto.defaultAccountPayableStatusType!!.value)))

      if (dto.defaultVendor?.id != null) {
         defaultVendor ?: errors.add(ValidationError("defaultVendor.id", NotFound(dto.defaultVendor!!.id!!)))
      }

      updatePurchaseOrderCost ?: errors.add(ValidationError("updatePurchaseOrderCost.value", NotFound(dto.updatePurchaseOrderCost!!.value)))

      defaultPurchaseOrderType ?: errors.add(ValidationError("defaultPurchaseOrderType.value", NotFound(dto.defaultPurchaseOrderType!!.value)))

      if (dto.defaultApprover?.id != null) {
         defaultApprover ?: errors.add(ValidationError("defaultApprover.id", NotFound(dto.defaultApprover!!.id!!)))
      }

      approvalRequiredFlagType ?: errors.add(ValidationError("approvalRequiredFlagType.value", NotFound(dto.approvalRequiredFlagType!!.value)))
   }
}
