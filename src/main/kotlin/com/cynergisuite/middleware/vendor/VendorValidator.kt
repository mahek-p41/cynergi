package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.NotNull
import com.cynergisuite.middleware.shipping.freight.calc.method.infrastructure.FreightCalcMethodTypeRepository
import com.cynergisuite.middleware.shipping.freight.onboard.infrastructure.FreightOnboardTypeRepository
import com.cynergisuite.middleware.shipping.shipvia.infrastructure.ShipViaRepository
import com.cynergisuite.middleware.vendor.group.infrastructure.VendorGroupRepository
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class VendorValidator @Inject constructor(
   private val freightCalcMethodTypeRepository: FreightCalcMethodTypeRepository,
   private val freightOnboardTypeRepository: FreightOnboardTypeRepository,
   private val shipViaRepository: ShipViaRepository,
   private val vendorGroupRepository: VendorGroupRepository,
   private val vendorPaymentTermRepository: VendorPaymentTermRepository,
   private val vendorRepository: VendorRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(VendorValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(@Valid vo: VendorDTO, company: Company): VendorEntity {
      logger.trace("Validating Save Vendor {}", vo)

      return doValidation(dto = vo, company = company)
   }

   @Throws(ValidationException::class, NotFoundException::class)
   fun validateUpdate(id: Long, dto: VendorDTO, company: Company): VendorEntity {
      logger.trace("Validating Update Vendor {}", dto)

      val existingVendor = vendorRepository.findOne(id, company) ?: throw NotFoundException(id)

      return doValidation(existingVendor, dto = dto, company = company)
   }

   private fun doValidation(existingVendor: VendorEntity? = null, dto: VendorDTO, company: Company): VendorEntity {
      val vendorGroupId = dto.vendorGroup?.id
      val vendorGroup = if (vendorGroupId != null) vendorGroupRepository.findOne(vendorGroupId, company) else null
      val shipViaId = dto.shipVia!!.id!!
      val shipVia = shipViaRepository.findOne(shipViaId, company)
      val vendorPaymentTermId = dto.paymentTerm!!.id!!
      val vendorPaymentTerm = vendorPaymentTermRepository.findOne(vendorPaymentTermId, company)
      val freightOnboardType = freightOnboardTypeRepository.findOne(value = dto.freightOnboardType!!.value!!)
      val freightMethodType = freightCalcMethodTypeRepository.findOne(value = dto.freightCalcMethodType!!.value!!)
      val payToId = dto.payTo?.id
      val payTo = if (payToId != null) vendorRepository.findOne(payToId, company) else null
      val autoSubmitPurchaseOrder = dto.autoSubmitPurchaseOrder
      val emailAddress = dto.emailAddress
      val purchaseOrderSubmitEmailAddress = dto.purchaseOrderSubmitEmailAddress

      doValidation { errors ->
         if (vendorGroupId != null && vendorGroup == null) {
            errors.add(ValidationError("vendorGroup.id", NotFound(vendorGroupId)))
         }

         if (vendorPaymentTerm == null) {
            errors.add(ValidationError("paymentTerm.id", NotFound(vendorPaymentTermId)))
         }

         if (payToId != null && payTo == null) {
            errors.add(ValidationError("payTo.id", NotFound(payToId)))
         }

         if (shipVia == null) {
            errors.add(ValidationError("shipVia.id", NotFound(dto.shipVia!!.id!!)))
         }

         freightOnboardType ?: errors.add(ValidationError("freightOnboardType.value", NotFound(dto.freightOnboardType!!.value!!)))
         freightMethodType ?: errors.add(ValidationError("freightMethodType.value", NotFound(dto.freightCalcMethodType!!.value!!)))

         if (autoSubmitPurchaseOrder == true && emailAddress == null) {
            errors.add(ValidationError("emailAddress", NotNull("emailAddress")))
         }

         if (autoSubmitPurchaseOrder == true && purchaseOrderSubmitEmailAddress == null) {
            errors.add(ValidationError("purchaseOrderSubmitEmailAddress", NotNull("purchaseOrderSubmitEmailAddress")))
         }
      }

      return if (existingVendor != null) {
         VendorEntity(
            existingVendor = existingVendor,
            dto = dto,
            vendorPaymentTerm = vendorPaymentTerm!!,
            shipVia = shipVia!!,
            vendorGroup = vendorGroup,
            freightOnboardType = freightOnboardType!!,
            freightMethodType = freightMethodType!!,
            payTo = payTo
         )
      } else {
         VendorEntity(
            dto = dto,
            vendorPaymentTerm = vendorPaymentTerm!!,
            shipVia = shipVia!!,
            company = company,
            vendorGroup = vendorGroup,
            freightOnboardType = freightOnboardType!!,
            freightMethodType = freightMethodType!!,
            payTo = payTo
         )
      }
   }
}
