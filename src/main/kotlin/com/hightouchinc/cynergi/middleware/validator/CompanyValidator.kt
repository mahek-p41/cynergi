package com.hightouchinc.cynergi.middleware.validator

import com.hightouchinc.cynergi.middleware.entity.CompanyDto
import com.hightouchinc.cynergi.middleware.validator.spi.ValidatorBase
import javax.validation.ConstraintViolationException

class CompanyValidator: ValidatorBase<CompanyDto>(
   clazz = CompanyDto::class.java
) {

   override fun validateSave(dto: CompanyDto) {
      // TODO check name is not duplicated
   }

   override fun validateUpdate(dto: CompanyDto) {
      if (dto.id == null) {
         throw ConstraintViolationException("id cannot be null", setOf( super.validationConstraint(dto, "id") ))
      }
   }
}
