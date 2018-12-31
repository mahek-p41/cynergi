package com.hightouchinc.cynergi.middleware.validator

import com.hightouchinc.cynergi.middleware.domain.NotFound
import com.hightouchinc.cynergi.middleware.entity.CompanyDto
import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.middleware.service.CompanyService
import com.hightouchinc.cynergi.middleware.validator.spi.ValidatorBase
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class CompanyValidator @Inject constructor(
   private val companyService: CompanyService
): ValidatorBase<CompanyDto>(
   clazz = CompanyDto::class.java
) {

   override fun validateSave(dto: CompanyDto) {
      // TODO check name is not duplicated
   }

   override fun validateUpdate(dto: CompanyDto) {
      val id = dto.id

      if (id == null) {
         throw ConstraintViolationException("id cannot be null", setOf( super.validationConstraint(dto, "id") ))
      } else if( !companyService.exists(id = id) ) {
         throw NotFoundException(notFound = NotFound(requestedNotFound = id.toString()))
      }
   }
}
