package com.hightouchinc.cynergi.middleware.service

import com.hightouchinc.cynergi.middleware.data.access.CompanyDataAccessObject
import com.hightouchinc.cynergi.middleware.data.transfer.Company
import javax.inject.Singleton

@Singleton
class CompanyService(
   private val companyDataAccessObject: CompanyDataAccessObject
): IdentityService<Company> {

   override fun findById(id: Long): Company? =
      companyDataAccessObject.fetchOne(id = id)

   fun save(company: Company): Company =
      companyDataAccessObject.save(company = company)
}
