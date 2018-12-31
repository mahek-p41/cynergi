package com.hightouchinc.cynergi.middleware.validator

import com.hightouchinc.cynergi.middleware.entity.CustomerDto
import com.hightouchinc.cynergi.middleware.validator.spi.ValidatorBase
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class CustomerValidator: ValidatorBase<CustomerDto>(
   clazz = CustomerDto::class.java
) {
   override fun validateSave(dto: CustomerDto) {
      //TODO validate customer save
   }

   override fun validateUpdate(dto: CustomerDto) {
      if (dto.id == null) {
         throw ConstraintViolationException(ErrorCodes.Validation.NOT_NULL, setOf( super.validationConstraint(dto, "id") ))
      }
   }
}
