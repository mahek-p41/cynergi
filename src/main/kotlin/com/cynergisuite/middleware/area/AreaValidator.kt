package com.cynergisuite.middleware.area

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.area.infrastructure.AreaRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationException
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Singleton
class AreaValidator @Inject constructor(
   private val areaRepository: AreaRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AreaValidator::class.java)

   @Throws(ValidationException::class)
   fun validateAreaTypeId(company: CompanyEntity, areaId: Int) {
      logger.trace("Validating AreaTypeId {}", areaId)

      doValidation {
         if (!areaRepository.exists(areaId)) {
            throw NotFoundException(areaId)
         }
      }
   }
}
