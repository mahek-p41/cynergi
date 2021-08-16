package com.cynergisuite.middleware.purchase.order

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.purchase.order.infrastructure.PurchaseOrderRepository
import com.cynergisuite.middleware.purchase.order.type.infrastructure.ExceptionIndicatorTypeRepository
import com.cynergisuite.middleware.purchase.order.type.infrastructure.PurchaseOrderStatusTypeRepository
import com.cynergisuite.middleware.purchase.order.type.infrastructure.PurchaseOrderTypeRepository
import com.cynergisuite.middleware.shipping.freight.onboard.infrastructure.FreightOnboardTypeRepository
import com.cynergisuite.middleware.shipping.freight.term.infrastructure.FreightTermTypeRepository
import com.cynergisuite.middleware.shipping.location.infrastructure.ShipLocationTypeRepository
import com.cynergisuite.middleware.shipping.shipvia.infrastructure.ShipViaRepository
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseOrderValidator @Inject constructor(
   private val accountRepository: AccountRepository,
   private val employeeRepository: EmployeeRepository,
   private val exceptionIndicatorTypeRepository: ExceptionIndicatorTypeRepository,
   private val freightOnboardTypeRepository: FreightOnboardTypeRepository,
   private val freightTermTypeRepository: FreightTermTypeRepository,
   private val purchaseOrderRepository: PurchaseOrderRepository,
   private val statusTypeRepository: PurchaseOrderStatusTypeRepository,
   private val typeRepository: PurchaseOrderTypeRepository,
   private val storeRepository: StoreRepository,
   private val shipLocationTypeRepository: ShipLocationTypeRepository,
   private val shipViaRepository: ShipViaRepository,
   private val paymentTermRepository: VendorPaymentTermRepository,
   private val vendorRepository: VendorRepository,
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(PurchaseOrderValidator::class.java)

   fun validateCreate(dto: PurchaseOrderDTO, company: CompanyEntity): PurchaseOrderEntity {
      logger.trace("Validating Create PurchaseOrder {}", dto)

      return doSharedValidation(dto, company)
   }

   fun validateUpdate(id: UUID, dto: PurchaseOrderDTO, company: CompanyEntity): PurchaseOrderEntity {
      logger.debug("Validating Update PurchaseOrder {}", dto)

      val existingEntity = purchaseOrderRepository.findOne(id, company) ?: throw NotFoundException(id)

      return doSharedValidation(dto, company, existingEntity)
   }

   private fun doSharedValidation(dto: PurchaseOrderDTO, company: CompanyEntity, existingEntity: PurchaseOrderEntity? = null): PurchaseOrderEntity {
      val vendor = vendorRepository.findOne(dto.vendor!!.id!!, company)
      val statusType = statusTypeRepository.findOne(dto.statusType!!.value)
      val type = typeRepository.findOne(dto.type!!.value)
      val freightOnboardType = freightOnboardTypeRepository.findOne(dto.freightOnboardType!!.value!!)
      val freightTermType = freightTermTypeRepository.findOne(dto.freightTermType!!.value!!)
      val shipLocationType = shipLocationTypeRepository.findOne(dto.shipLocationType!!.value!!)
      val approvedBy = employeeRepository.findOne(dto.approvedBy!!.number!!, dto.approvedBy!!.type!!, company)
      val purchaseAgent = employeeRepository.findOne(dto.purchaseAgent!!.number!!, dto.purchaseAgent!!.type!!, company)
      val shipVia = shipViaRepository.findOne(dto.shipVia!!.id!!, company)
      val shipTo = storeRepository.findOne(dto.shipTo!!.id!!, company)
      val paymentTermType = paymentTermRepository.findOne(dto.paymentTermType!!.id!!, company)
      val exceptionIndicatorType = exceptionIndicatorTypeRepository.findOne(dto.exceptionIndicatorType!!.value)
      val vendorSubmittedEmployee = dto.vendorSubmittedEmployee?.id?.let { employeeRepository.findOne(dto.vendorSubmittedEmployee!!.number!!, dto.vendorSubmittedEmployee!!.type!!, company) }

      doValidation { errors ->
         vendor
            ?: errors.add(ValidationError("vendor.id", NotFound(dto.vendor!!.id!!)))

         statusType
            ?: errors.add(ValidationError("statusType.value", NotFound(dto.statusType!!.value)))

         type
            ?: errors.add(ValidationError("type.value", NotFound(dto.type!!.value)))

         freightOnboardType
            ?: errors.add(ValidationError("freightOnboardType.value", NotFound(dto.freightOnboardType!!.value!!)))

         freightTermType
            ?: errors.add(ValidationError("freightTermType.value", NotFound(dto.freightTermType!!.value!!)))

         shipLocationType
            ?: errors.add(ValidationError("shipLocationType.value", NotFound(dto.shipLocationType!!.value!!)))

         approvedBy
            ?: errors.add(ValidationError("approvedBy.number", NotFound(dto.approvedBy!!.number!!)))

         purchaseAgent
            ?: errors.add(ValidationError("purchaseAgent.number", NotFound(dto.purchaseAgent!!.number!!)))

         shipVia
            ?: errors.add(ValidationError("shipVia.id", NotFound(dto.shipVia!!.id!!)))

         shipTo
            ?: errors.add(ValidationError("shipTo.id", NotFound(dto.shipTo!!.id!!)))

         paymentTermType
            ?: errors.add(ValidationError("paymentTermType.id", NotFound(dto.paymentTermType!!.id!!)))

         exceptionIndicatorType
            ?: errors.add(ValidationError("exceptionIndicatorType.value", NotFound(dto.exceptionIndicatorType!!.value)))

         if (dto.vendorSubmittedEmployee?.number != null && vendorSubmittedEmployee == null) {
            errors.add(ValidationError("vendorSubmittedEmployee.number", NotFound(dto.vendorSubmittedEmployee!!.number!!)))
         }
      }

      return PurchaseOrderEntity(
         existingEntity = existingEntity,
         dto = dto,
         vendor = vendor!!,
         statusType = statusType!!,
         type = type!!,
         freightOnboardType = freightOnboardType!!,
         freightTermType = freightTermType!!,
         shipLocationType = shipLocationType!!,
         approvedBy = approvedBy!!,
         purchaseAgent = purchaseAgent!!,
         shipVia = shipVia!!,
         shipTo = shipTo!!,
         paymentTermType = paymentTermType!!,
         exceptionIndicatorType = exceptionIndicatorType!!,
         vendorSubmittedEmployee = vendorSubmittedEmployee,
      )
   }
}
