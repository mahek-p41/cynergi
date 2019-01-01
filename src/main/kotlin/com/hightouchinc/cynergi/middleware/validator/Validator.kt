package com.hightouchinc.cynergi.middleware.validator

import javax.validation.ValidationException

interface Validator<DTO> {

   @Throws(ValidationException::class)
   fun validateSave(dto: DTO)

   @Throws(ValidationException::class)
   fun validateUpdate(dto: DTO)
}
