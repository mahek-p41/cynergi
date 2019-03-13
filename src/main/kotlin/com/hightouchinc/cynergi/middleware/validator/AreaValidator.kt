package com.hightouchinc.cynergi.middleware.validator

import com.hightouchinc.cynergi.middleware.authentication.AuthenticatedCynergiUser
import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.middleware.service.CompanyService
import com.hightouchinc.cynergi.middleware.service.EmployeeService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AreaValidator @Inject constructor(
   private val companyService: CompanyService,
   private val employeeService: EmployeeService
) {
   fun validateUser(cynergiUser: AuthenticatedCynergiUser) { // this needs to be done as part of micronaut's security mechanism rather than here
      if ( !companyService.exists(id = cynergiUser.companyId) ) {
         throw NotFoundException(cynergiUser.companyId)
      } else if ( !employeeService.exists(id = cynergiUser.userId) ) {
         throw NotFoundException(id = cynergiUser.userId)
      }
   }
}
