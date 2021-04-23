package com.cynergisuite.middleware.purchase.order.detail

import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.purchase.order.infrastructure.PurchaseOrderRepository
import com.cynergisuite.middleware.purchase.order.type.infrastructure.ExceptionIndicatorTypeRepository
import com.cynergisuite.middleware.purchase.order.type.infrastructure.PurchaseOrderRequisitionIndicatorTypeRepository
import com.cynergisuite.middleware.purchase.order.type.infrastructure.PurchaseOrderStatusTypeRepository
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseOrderDetailValidator @Inject constructor(
   private val exceptionIndicatorTypeRepository: ExceptionIndicatorTypeRepository,
   private val purchaseOrderRepository: PurchaseOrderRepository,
   private val purchaseOrderRequisitionIndicatorTypeRepository: PurchaseOrderRequisitionIndicatorTypeRepository,
   private val statusTypeRepository: PurchaseOrderStatusTypeRepository,
   private val storeRepository: StoreRepository,
   private val vendorRepository: VendorRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(PurchaseOrderDetailValidator::class.java)

   fun validateCreate(dto: PurchaseOrderDetailDTO, company: Company): PurchaseOrderDetailEntity {
      logger.trace("Validating Create PurchaseOrderDetail {}", dto)

      return doSharedValidation(dto, company)
   }

   fun validateUpdate(id: Long, dto: PurchaseOrderDetailDTO, company: Company): PurchaseOrderDetailEntity {
      logger.debug("Validating Update PurchaseOrderDetail {}", dto)

      return doSharedValidation(dto, company)
   }

   private fun doSharedValidation(dto: PurchaseOrderDetailDTO, company: Company): PurchaseOrderDetailEntity {
      val purchaseOrder = purchaseOrderRepository.findOne(dto.purchaseOrder!!.id!!, company)
      val shipTo = storeRepository.findOne(dto.shipTo!!.id!!, company)
      val vendor = vendorRepository.findOne(dto.vendor!!.id!!, company)
      val statusType = statusTypeRepository.findOne(dto.statusType!!.value)
      val purchaseOrderRequisitionIndicatorType = purchaseOrderRequisitionIndicatorTypeRepository.findOne(dto.purchaseOrderRequisitionIndicatorType!!.value)
      val exceptionIndicatorType = exceptionIndicatorTypeRepository.findOne(dto.exceptionIndicatorType!!.value)

      doValidation { errors ->
         purchaseOrder
            ?: errors.add(ValidationError("purchaseOrder.id", NotFound(dto.purchaseOrder!!.id!!)))

         shipTo
            ?: errors.add(ValidationError("shipTo.id", NotFound(dto.shipTo!!.id!!)))

         vendor
            ?: errors.add(ValidationError("vendor.id", NotFound(dto.vendor!!.id!!)))

         statusType
            ?: errors.add(ValidationError("statusType.value", NotFound(dto.statusType!!.value)))

         purchaseOrderRequisitionIndicatorType
            ?: errors.add(ValidationError("purchaseOrderRequisitionIndicatorType.value", NotFound(dto.purchaseOrderRequisitionIndicatorType!!.value)))

         exceptionIndicatorType
            ?: errors.add(ValidationError("exceptionIndicatorType.value", NotFound(dto.exceptionIndicatorType!!.value)))
      }

      return PurchaseOrderDetailEntity(
         dto = dto,
         purchaseOrder = purchaseOrder!!,
         shipTo = SimpleIdentifiableEntity(shipTo!!.id),
         vendor = vendor!!,
         statusType = statusType!!,
         purchaseOrderRequisitionIndicatorType = purchaseOrderRequisitionIndicatorType!!,
         exceptionIndicatorType = exceptionIndicatorType!!
      )
   }
}
