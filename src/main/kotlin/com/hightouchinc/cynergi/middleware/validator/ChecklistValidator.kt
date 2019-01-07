package com.hightouchinc.cynergi.middleware.validator

import com.hightouchinc.cynergi.middleware.entity.ChecklistDto
import com.hightouchinc.cynergi.middleware.exception.ValidationError
import com.hightouchinc.cynergi.middleware.validator.spi.NestedValidatorBase
import javax.inject.Singleton

@Singleton
class ChecklistValidator: NestedValidatorBase<ChecklistDto, String>() {
   override fun doValidateSave(dto: ChecklistDto, parent: String): List<ValidationError> {
      return emptyList()
   }

   override fun doValidateUpdate(dto: ChecklistDto, parent: String): List<ValidationError> {
      return emptyList()
   }
}
