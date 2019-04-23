package com.hightouchinc.cynergi.middleware.service

import com.hightouchinc.cynergi.middleware.dto.MenuTreeDto
import com.hightouchinc.cynergi.middleware.repository.CompanyRepository
import com.hightouchinc.cynergi.middleware.repository.MenuRepository
import javax.inject.Singleton

@Singleton
class MenuService(
   private val companyRepository: CompanyRepository,
   private val menuRepository: MenuRepository
) {
   fun findAllBy(level: Int, companyId: Long): Set<MenuTreeDto> {
      val company = companyRepository.findOne(id = companyId)!! // validation should have handled that this company exists

      return menuRepository.findAll(level = level, company = company).map { MenuTreeDto(it) }.toHashSet()
   }
}
