package com.hightouchinc.cynergi.middleware.validator

import com.hightouchinc.cynergi.middleware.entity.CompanyDto
import com.hightouchinc.cynergi.middleware.exception.ValidationError
import com.hightouchinc.cynergi.middleware.service.CompanyService
import com.hightouchinc.cynergi.middleware.validator.spi.ValidatorBase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompanyValidator @Inject constructor(
   private val companyService: CompanyService
): ValidatorBase<CompanyDto>() {
   override fun doValidateSave(dto: CompanyDto): List<ValidationError> {
      return if (companyService.exists(name = dto.name!!)) {
         listOf(ValidationError("save.dto.name", ErrorCodes.Validation.DUPLICATE, listOf(dto.name!!)))
      } else {
         listOf()
      }
   }

   override fun doValidateUpdate(dto: CompanyDto): List<ValidationError> {
      val errors = mutableListOf<ValidationError>()
      val id = dto.id

      if (id == null) {
         errors.add(element = ValidationError("update.dto.id", ErrorCodes.Validation.NOT_NULL, listOf("id")))
      } else if ( !companyService.exists(id = id) ) {
         errors.add(element = ValidationError("update.dto.id", ErrorCodes.System.NOT_FOUND, listOf(id)))
      }

      return errors
   }

   /*
   override fun validateUpdate(dto: CompanyDto) {
      val id = dto.id

      if (id == null) {
         throw ValidationException()
      } else if( !companyService.exists(id = id) ) {
         throw NotFoundException(id = id)
      }
   }*/
}
