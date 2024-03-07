package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.cynergisuite.domain.SimpleLegacyIdentifiableEntity
import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableInvoiceSelectedTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableInvoiceStatusTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableInvoiceTypeRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.purchase.order.infrastructure.PurchaseOrderRepository
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

@Singleton
class AccountPayableInvoiceValidator @Inject constructor(
   private val employeeRepository: EmployeeRepository,
   private val purchaseOrderRepository: PurchaseOrderRepository,
   private val selectedRepository: AccountPayableInvoiceSelectedTypeRepository,
   private val statusRepository: AccountPayableInvoiceStatusTypeRepository,
   private val storeRepository: StoreRepository,
   private val typeRepository: AccountPayableInvoiceTypeRepository,
   private val vendorRepository: VendorRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableInvoiceValidator::class.java)

   fun validateCreate(dto: AccountPayableInvoiceDTO, company: CompanyEntity): AccountPayableInvoiceEntity {
      logger.trace("Validating Create AccountPayableInvoice {}", dto)

      return doSharedValidation(dto, company)
   }

   fun validateUpdate(id: UUID, dto: AccountPayableInvoiceDTO, company: CompanyEntity): AccountPayableInvoiceEntity {
      logger.debug("Validating Update AccountPayableInvoice {}", dto)

      return doSharedValidation(dto, company)
   }

   private fun doSharedValidation(dto: AccountPayableInvoiceDTO, company: CompanyEntity): AccountPayableInvoiceEntity {
      val vendor = vendorRepository.findOne(dto.vendor!!.id!!, company)
      val purchaseOrder = dto.purchaseOrder?.id?.let { purchaseOrderRepository.findOne(it, company) }
      val employee = employeeRepository.findOne(dto.employee!!.number!!, dto.employee!!.type!!, company)
      val selected = selectedRepository.findOne(dto.selected!!.value)
      val type = typeRepository.findOne(dto.type!!.value)
      val status = statusRepository.findOne(dto.status!!.value)
      val payTo = vendorRepository.findOne(dto.payTo!!.id!!, company)
      val location = dto.location?.id?.let { storeRepository.findOne(it, company) }

      doValidation { errors ->
         // non-nullable validations
         vendor ?: errors.add(ValidationError("vendor.id", NotFound(dto.vendor!!.id!!)))
         employee ?: errors.add(ValidationError("employee.number", NotFound(dto.employee!!.number!!)))
         selected ?: errors.add(ValidationError("selected.value", NotFound(dto.selected!!.value)))
         type ?: errors.add(ValidationError("type.value", NotFound(dto.type!!.value)))
         status ?: errors.add(ValidationError("status.value", NotFound(dto.status!!.value)))
         payTo ?: errors.add(ValidationError("payTo.id", NotFound(dto.payTo!!.id!!)))

         // nullable validations
         if (dto.purchaseOrder?.id != null && purchaseOrder == null) {
            errors.add(ValidationError("purchaseOrder.id", NotFound(dto.purchaseOrder!!.id!!)))
         }

         if (dto.location?.id != null && location == null) {
            errors.add(ValidationError("location.id", NotFound(dto.location!!.id!!)))
         }
      }

      if (dto.separateCheckIndicator == null) dto.separateCheckIndicator = vendor!!.separateCheck

      return AccountPayableInvoiceEntity(
         dto,
         vendor!!,
         purchaseOrder,
         employee!!,
         selected!!,
         type!!,
         status!!,
         payTo!!,
         location?.let { SimpleLegacyIdentifiableEntity(it.id) }
      )
   }
}
