package com.cynergisuite.middleware.area

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.area.infrastructure.AreaRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotFound
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class AreaValidator @Inject constructor(
   private val areaRepository: AreaRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AreaValidator::class.java)

   @Throws(ValidationException::class)
   fun validateAreaId(company: Company, @Valid areaId: Long) {
      logger.trace("Validating Save Account {}", areaId)

      doValidation { errors ->
         if (!areaRepository.exists(areaId)) {
            errors.add(ValidationError("areaId", NotFound(areaId)))
         }
      }
   }
}
