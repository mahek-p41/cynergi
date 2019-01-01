package com.hightouchinc.cynergi.middleware.validator

import com.hightouchinc.cynergi.middleware.entity.CustomerDto
import com.hightouchinc.cynergi.middleware.exception.ValidationError
import com.hightouchinc.cynergi.middleware.validator.spi.ValidatorBase
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class CustomerValidator: ValidatorBase<CustomerDto>() {
   override fun doValidateSave(dto: CustomerDto): List<ValidationError> {
      return listOf() //TODO
   }

   override fun doValidateUpdate(dto: CustomerDto): List<ValidationError> {
      return listOf() //TODO
   }
}
