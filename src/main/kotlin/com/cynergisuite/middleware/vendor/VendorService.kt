package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableDataTransferObject
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.ValidatorBase.Companion.logger
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.makeCell
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermValueObject
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import io.micronaut.validation.Validated
import org.apache.commons.lang3.StringUtils.EMPTY
import java.awt.Color
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class VendorService @Inject constructor(
   private val vendorRepository: VendorRepository,
   private val vendorPaymentTermRepository: VendorPaymentTermRepository,
   private val vendorValidator: VendorValidator
) {

   fun fetchById(id: Long, company: Company): VendorValueObject? =
      vendorRepository.findOne(id, company)?.let{ VendorValueObject(entity = it) }

   @Validated
   fun create(@Valid vo: VendorValueObject, company: Company): VendorValueObject {
      logger.debug("Vendor Create Before Validation VendorVO {}", vo)
      val toCreate = vendorValidator.validateCreate(vo, company)
      logger.debug("Vendor Create After Validation VendorEntity {}", toCreate)
      return VendorValueObject(
         entity = vendorRepository.insert(entity = toCreate)
      )
   }

   fun fetchAll(company: Company, pageRequest: PageRequest): Page<VendorValueObject> {
      val found = vendorRepository.findAll(company, pageRequest)

      return found.toPage { vendor: VendorEntity ->
         VendorValueObject(vendor)
      }
   }

   @Validated
   fun update(id: Long, @Valid vo: VendorValueObject, company: Company): VendorValueObject {
      val toUpdate = vendorValidator.validateUpdate(id, vo, company)

      return VendorValueObject(
         entity = vendorRepository.update(entity = toUpdate)
      )
   }

}
