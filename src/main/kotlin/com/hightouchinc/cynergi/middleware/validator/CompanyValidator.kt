package com.hightouchinc.cynergi.middleware.validator

import com.hightouchinc.cynergi.middleware.entity.CompanyDto
import com.hightouchinc.cynergi.middleware.exception.ValidationError
import com.hightouchinc.cynergi.middleware.service.CompanyService
import com.hightouchinc.cynergi.middleware.validator.spi.ValidatorBase
import org.eclipse.collections.api.list.ImmutableList
import org.eclipse.collections.impl.factory.Lists
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompanyValidator @Inject constructor(
   private val companyService: CompanyService
): ValidatorBase<CompanyDto>() {

   override fun doValidateSave(dto: CompanyDto): ImmutableList<ValidationError> {
      return if (companyService.exists(name = dto.name!!)) {
         Lists.immutable.of(ValidationError("name", ErrorCodes.Validation.DUPLICATE, Lists.immutable.of(dto.name!!)))
      } else {
         Lists.immutable.empty()
      }
   }

   override fun doValidateUpdate(dto: CompanyDto): ImmutableList<ValidationError> {
      val errors = Lists.mutable.of<ValidationError>()
      val id = dto.id

      if (id == null) {
         errors.add(element = ValidationError("id", ErrorCodes.Validation.NOT_NULL, Lists.immutable.of("id")))
      } else if ( !companyService.exists(id = id) ) {
         errors.add(element = ValidationError("id", ErrorCodes.System.NOT_FOUND, Lists.immutable.of(id)))
      }

      return errors.toImmutable()
   }
}
