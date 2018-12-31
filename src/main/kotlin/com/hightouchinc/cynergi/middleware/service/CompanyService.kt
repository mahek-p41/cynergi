package com.hightouchinc.cynergi.middleware.service

import com.hightouchinc.cynergi.middleware.repository.CompanyRepository
import com.hightouchinc.cynergi.middleware.data.transfer.Company
import javax.inject.Singleton

@Singleton
class CompanyService(
   private val companyDataAccessObject: CompanyRepository
): IdentityService<Company> {

   override fun findById(id: Long): Company? =
      companyDataAccessObject.fetchOne(id = id)

   fun save(company: Company): Company =
      companyDataAccessObject.save(entity = company)
}
