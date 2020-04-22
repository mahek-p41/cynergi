package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.SimpleIdentifiableDataTransferObject
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.makeCell
import com.cynergisuite.middleware.authentication.User
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeEntity.Companion.fromUser
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.StoreValueObject
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
   private val vendorValidator: VendorValidator,
   private val companyRepository: CompanyRepository,
   private val localizationService: LocalizationService
) {

   fun fetchById(id: Long, company: Company): VendorValueObject? =
      vendorRepository.findOne(id, company)?.let{ VendorValueObject(entity = it) }

   @Validated
   fun fetchAll(@Valid pageRequest: StandardPageRequest, dataset: String): Page<VendorValueObject> {
      val validaPageRequest = vendorValidator.validationFetchAll(pageRequest, dataset)
      val found: RepositoryPage<VendorEntity, StandardPageRequest> = vendorRepository.findAll(validaPageRequest, dataset)

      return found.toPage {
         VendorValueObject(it)
      }
   }

   fun exists(id: Long): Boolean =
      vendorRepository.exists(id = id)

   @Validated
   fun create(@Valid vo: VendorCreateValueObject, user: User): VendorValueObject {
      val validVendor = vendorValidator.validateCreate(vo, user)
      val vendor = vendorRepository.insert(validVendor)

      return VendorValueObject(vendor)
   }

   fun findOrCreate(store: StoreEntity, employee: EmployeeEntity): VendorValueObject {
      val createdOrInProgressVendor = vendorRepository.findOneCreatedOrInProgress(store)

      return if (createdOrInProgressVendor != null) {
         VendorValueObject(createdOrInProgressVendor)
      } else {
         create(VendorCreateValueObject(StoreValueObject(store)))
      }
   }

}
