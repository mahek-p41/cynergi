package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.SimpleIdentifiableDataTransferObject
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.audit.status.CREATED
import com.cynergisuite.middleware.audit.status.SIGNED_OFF
import com.cynergisuite.middleware.authentication.User
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.employee.EmployeeEntity.Companion.fromUser
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.InvalidDataset
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.ThruDateIsBeforeFrom
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorValidator @Inject constructor(
   private val vendorRepository: VendorRepository,
   private val companyRepository: CompanyRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(VendorValidator::class.java)

   @Throws(ValidationException::class)
   fun validationFetchAll(pageRequest: StandardPageRequest, dataset: String): StandardPageRequest {
      doValidation { errors ->
         // Do I need to create a VendorPageRequest so I have the below from/to for paging?
         val from = pageRequest.from
         val thru = pageRequest.thru

         //Validate the Shutdown from and thru dates?
         if (thru != null && from != null && thru.isBefore(from)) {
            errors.add(ValidationError("from", ThruDateIsBeforeFrom(from, thru)))
         }

         if (companyRepository.doesNotExist(dataset)) {
            errors.add(ValidationError("dataset", InvalidDataset(dataset)))
         }
      }

      return pageRequest
   }

   @Throws(ValidationException::class)
   fun validateCreate(vendor: VendorValueObject, user: User): VendorEntity {
      logger.debug("Validating Create Vendor {}", vendor)

      doValidation { errors ->
         val storeNumber = vendor.store?.number

         if (storeNumber != null && !storeRepository.exists(number = storeNumber)) {
            errors.add(ValidationError("storeNumber", NotFound(storeNumber)))
         }
      }

      return VendorEntity(
         store = storeRepository.findOne(number = vendor.store!!.number!!, dataset = user.myDataset())!!,
         actions = mutableSetOf(
            VendorActionEntity(
               status = CREATED,
               changedBy = fromUser(user)
            )
         ),
         dataset = user.myDataset()
      )
   }
}
