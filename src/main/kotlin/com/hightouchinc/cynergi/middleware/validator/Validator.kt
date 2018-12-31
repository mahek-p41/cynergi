package com.hightouchinc.cynergi.middleware.validator

import javax.validation.ConstraintViolationException

interface Validator<DTO> {

   @Throws(ConstraintViolationException::class)
   fun validateSave(dto: DTO)

   @Throws(ConstraintViolationException::class)
   fun validateUpdate(dto: DTO)
}
