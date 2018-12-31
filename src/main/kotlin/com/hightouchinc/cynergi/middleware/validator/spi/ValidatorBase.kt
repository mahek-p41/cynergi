package com.hightouchinc.cynergi.middleware.validator.spi

import com.hightouchinc.cynergi.middleware.validator.Validator
import org.hibernate.validator.internal.engine.ConstraintViolationImpl
import org.hibernate.validator.internal.engine.path.PathImpl
import javax.validation.ConstraintViolation

abstract class ValidatorBase<DTO>(
   private val clazz: Class<DTO>
): Validator<DTO> {

   protected fun validationConstraint(dto: DTO, invalidPropertyPath: String): ConstraintViolation<DTO> {
      return ConstraintViolationImpl.forBeanValidation(
         "", //TODO message template
         mapOf<String, Any>(), //TODO message parameters
         mapOf<String, Any>(), //TODO expression variables
         "", //TODO interpolated message
         clazz, // root bean class
         dto, // root bean,
      null, //TODO leaf bean instance
         null, //TODO value
         PathImpl.createPathFromString(invalidPropertyPath), // property path
      null, //TODO constraint descriptor
         null, //TODO element type
         null //TODO dynamic payload
      )
   }
}
