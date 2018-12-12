package com.hightouchinc.cynergi.middleware.service

import com.hightouchinc.cynergi.middleware.data.access.BusinessDataAccessObject
import com.hightouchinc.cynergi.middleware.data.transfer.Business
import javax.inject.Singleton

@Singleton
class BusinessService(
   private val businessDataAccessObject: BusinessDataAccessObject
): IdentityService<Business> {

   override fun findById(id: Long): Business? =
      businessDataAccessObject.fetchOne(id = id)
}
