package com.hightouchinc.cynergi.middleware.validator

import javax.validation.ValidationException

interface NestedValidator<DTO, PARENT> {

   @Throws(ValidationException::class)
   fun validateSave(dto: DTO, parent: PARENT)

   @Throws(ValidationException::class)
   fun validateUpdate(dto: DTO, parent: PARENT)
}
