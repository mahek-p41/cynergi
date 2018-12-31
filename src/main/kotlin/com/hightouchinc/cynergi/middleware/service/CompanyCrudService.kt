package com.hightouchinc.cynergi.middleware.service

import com.hightouchinc.cynergi.middleware.entity.Company
import com.hightouchinc.cynergi.middleware.entity.CompanyDto
import com.hightouchinc.cynergi.middleware.repository.CompanyRepository
import javax.inject.Singleton

@Singleton
class CompanyCrudService(
   private val companyRepository: CompanyRepository
): CrudService<CompanyDto> {

   override fun findById(id: Long): CompanyDto? =
      companyRepository.fetchOne(id = id)?.let { CompanyDto(it) }

   override fun save(dto: CompanyDto): CompanyDto {
      return CompanyDto(
         companyRepository.save(entity = Company(dto = dto))
      )
   }

   override fun update(dto: CompanyDto): CompanyDto {
      TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
   }
}
