package com.hightouchinc.cynergi.middleware.service

import com.hightouchinc.cynergi.middleware.entity.Company
import com.hightouchinc.cynergi.middleware.entity.CompanyDto
import com.hightouchinc.cynergi.middleware.repository.CompanyRepository
import org.springframework.validation.annotation.Validated
import javax.inject.Singleton

@Validated
@Singleton
class CompanyService(
   private val companyRepository: CompanyRepository
): CrudService<CompanyDto> {

   override fun findById(id: Long): CompanyDto? =
      companyRepository.findOne(id = id)?.let { CompanyDto(it) }

   override fun exists(id: Long): Boolean =
      companyRepository.exists(id = id)

   fun exists(name: String): Boolean =
      companyRepository.exists(name = name)

   override fun create(dto: CompanyDto): CompanyDto {
      return CompanyDto(
         companyRepository.insert(entity = Company(dto = dto))
      )
   }

   override fun update(dto: CompanyDto): CompanyDto {
      return CompanyDto(
         companyRepository.update(entity = Company(dto = dto))
      )
   }
}
