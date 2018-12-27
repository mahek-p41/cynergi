package com.hightouchinc.cynergi.middleware.service

import com.hightouchinc.cynergi.middleware.data.access.CompanyDataAccessObject
import com.hightouchinc.cynergi.middleware.data.transfer.Business
import javax.inject.Singleton

@Singleton
class BusinessService(
   private val companyDataAccessObject: CompanyDataAccessObject
): IdentityService<Business> {

   override fun findById(id: Long): Business? =
      companyDataAccessObject.fetchOne(id = id)
}
