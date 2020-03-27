package com.cynergisuite.middleware.vendor.payment.terms

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.reportal.ReportalService
import com.cynergisuite.middleware.vendor.payment.terms.infrastructure.VendorPaymentTermsRepository
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class VendorPaymentTermsService @Inject constructor(
   private val vendorPaymentTermsRepository: VendorPaymentTermsRepository,
   private val vendorPaymentTermsValidator: VendorPaymentTermsValidator
) {

   fun fetchById(id: Long, company: Company): VendorPaymentTermsValueObject? =
      vendorPaymentTermsRepository.findOne(id)?.let{VendorPaymentTermsValueObject(entity = it)}

   @Validated
   fun fetchAll(@Valid pageRequest: PageRequest, company: Company): Page<VendorPaymentTermsValueObject> {
      val found = vendorPaymentTermsRepository.findAll(pageRequest, company)

      return found.toPage { vendorPaymentTerms: VendorPaymentTermsEntity ->
         VendorPaymentTermsValueObject(vendorPaymentTerms)
      }
   }

   fun exists(id: Long): Boolean =
      vendorPaymentTermsRepository.exists(id = id)

   @Validated
   fun create(@Valid vo: VendorPaymentTermsValueObject, company: Company): VendorPaymentTermsValueObject {
      vendorPaymentTermsValidator.validateCreate(vo)

      return VendorPaymentTermsValueObject(
         entity = vendorPaymentTermsRepository.insert(entity = VendorPaymentTermsEntity(vo, company))
      )
   }

   @Validated
   fun update(@Valid vo: VendorPaymentTermsValueObject, company: Company): VendorPaymentTermsValueObject {
      vendorPaymentTermsValidator.validateUpdate(vo)

      return VendorPaymentTermsValueObject(
         entity = vendorPaymentTermsRepository.update(entity = VendorPaymentTermsEntity(vo, company))
      )
   }
}
