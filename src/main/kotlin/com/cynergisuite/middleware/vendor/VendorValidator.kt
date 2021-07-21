package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.AddressNeedsUpdated
import com.cynergisuite.middleware.localization.InvalidPayToVendor
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
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

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
   fun validateCreate(dto: VendorDTO, company: Company): VendorEntity {
      logger.trace("Validating Save Vendor {}", dto)

      val vendorGroup = dto.vendorGroup?.id?.let { vendorGroupRepository.findOne(it, company) }
      val shipVia = dto.shipVia?.id?.let { shipViaRepository.findOne(it, company) }
      val vendorPaymentTerm = dto.paymentTerm?.id?.let { vendorPaymentTermRepository.findOne(it, company) }
      val freightOnboardType = dto.freightOnboardType?.value?.let { freightOnboardTypeRepository.findOne(value = it) }
      val freightMethodType = dto.freightCalcMethodType?.value?.let { freightCalcMethodTypeRepository.findOne(value = it) }
      val payTo = dto.payTo?.id?.let { vendorRepository.findOne(it, company) }

      doValidation { errors ->
         doSharedValidation(errors, dto = dto, company = company)
      }

      val toCreate: VendorEntity? = VendorEntity(
         dto = dto,
         vendorPaymentTerm = vendorPaymentTerm!!,
         shipVia = shipVia!!,
         company = company,
         vendorGroup = vendorGroup,
         freightOnboardType = freightOnboardType!!,
         freightMethodType = freightMethodType!!,
         payTo = payTo
      )

      return toCreate!!
   }

   @Throws(ValidationException::class, NotFoundException::class)
   fun validateUpdate(id: UUID, dto: VendorDTO, company: Company): Pair<VendorEntity, VendorEntity> {
      logger.trace("Validating Update Vendor {}", dto)

      val vendorGroup = dto.vendorGroup?.id?.let { vendorGroupRepository.findOne(it, company) }
      val shipVia = dto.shipVia?.id?.let { shipViaRepository.findOne(it, company) }
      val vendorPaymentTerm = dto.paymentTerm?.id?.let { vendorPaymentTermRepository.findOne(it, company) }
      val freightOnboardType = dto.freightOnboardType?.value?.let { freightOnboardTypeRepository.findOne(value = it) }
      val freightMethodType = dto.freightCalcMethodType?.value?.let { freightCalcMethodTypeRepository.findOne(value = it) }
      val payTo = dto.payTo?.id?.let { vendorRepository.findOne(it, company) }

      val existingVendor = vendorRepository.findOne(id, company) ?: throw NotFoundException(id)

      doValidation { errors ->
         if (existingVendor.address?.id != null) { // have existing address
            if (dto.address?.id == null && !dto.address?.name.isNullOrBlank()) { // just update existing address rather than create new one
               errors.add(ValidationError("address.id", AddressNeedsUpdated()))
            }
         }

         doSharedValidation(errors, dto = dto, company = company)
      }

      val toUpdate: VendorEntity? = VendorEntity(
         existingVendor = existingVendor,
         dto = dto,
         vendorPaymentTerm = vendorPaymentTerm!!,
         shipVia = shipVia!!,
         vendorGroup = vendorGroup,
         freightOnboardType = freightOnboardType!!,
         freightMethodType = freightMethodType!!,
         payTo = payTo
      )

      return Pair(existingVendor, toUpdate!!)
   }

   private fun doSharedValidation(errors: MutableSet<ValidationError>, dto: VendorDTO, company: Company) {
      val vendorId = dto.id
      val vendorGroupId = dto.vendorGroup?.id
      val vendorPaymentTermId = dto.paymentTerm!!.id!!
      val payToId = dto.payTo?.id

      val vendorGroup = dto.vendorGroup?.id?.let { vendorGroupRepository.findOne(it, company) }
      val shipVia = dto.shipVia?.id?.let { shipViaRepository.findOne(it, company) }
      val vendorPaymentTerm = dto.paymentTerm?.id?.let { vendorPaymentTermRepository.findOne(it, company) }
      val freightOnboardType = dto.freightOnboardType?.value?.let { freightOnboardTypeRepository.findOne(value = it) }
      val freightMethodType = dto.freightCalcMethodType?.value?.let { freightCalcMethodTypeRepository.findOne(value = it) }
      val payTo = dto.payTo?.id?.let { vendorRepository.findOne(it, company) }
      val autoSubmitPurchaseOrder = dto.autoSubmitPurchaseOrder
      val emailAddress = dto.emailAddress
      val purchaseOrderSubmitEmailAddress = dto.purchaseOrderSubmitEmailAddress

      if (vendorGroupId != null && vendorGroup == null) {
         errors.add(ValidationError("vendorGroup.id", NotFound(vendorGroupId)))
      }

      if (vendorPaymentTerm == null) {
         errors.add(ValidationError("paymentTerm.id", NotFound(vendorPaymentTermId)))
      }

      if (payToId != null && payTo == null) {
         errors.add(ValidationError("payTo.id", NotFound(payToId)))
      }

      if (payToId != null && payToId == vendorId) {
         errors.add(ValidationError("payTo.id", InvalidPayToVendor(payToId)))
      }

      shipVia ?: errors.add(ValidationError("shipVia.id", NotFound(dto.shipVia!!.id!!)))

      freightOnboardType ?: errors.add(ValidationError("freightOnboardType.value", NotFound(dto.freightOnboardType!!.value!!)))
      freightMethodType ?: errors.add(ValidationError("freightMethodType.value", NotFound(dto.freightCalcMethodType!!.value!!)))

      if (autoSubmitPurchaseOrder == true && emailAddress == null) {
         errors.add(ValidationError("emailAddress", NotNull("emailAddress")))
      }

      if (autoSubmitPurchaseOrder == true && purchaseOrderSubmitEmailAddress == null) {
         errors.add(ValidationError("purchaseOrderSubmitEmailAddress", NotNull("purchaseOrderSubmitEmailAddress")))
      }
   }
}
