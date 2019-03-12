package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.Area
import com.hightouchinc.cynergi.middleware.entity.Company
import com.hightouchinc.cynergi.middleware.entity.Menu
import com.hightouchinc.cynergi.middleware.repository.AreaRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class AreaTestDataLoader {
   static Stream<Area> stream(int number = 1, Integer levelIn = null, Menu menuIn = null, Company companyIn = null) {
      final int value = number > 0 ? number : 1
      final Faker faker = new Faker()
      final num = faker.number()
      final lorem = faker.lorem()
      final level = levelIn != null && levelIn < 100 && levelIn > 0 ? levelIn : num.numberBetween(1, 99)
      final company = companyIn ?: CompanyTestDataLoader.single()
      final menu = menuIn ?: MenuTestDataLoader.single()

      return IntStream.range(0, value).mapToObj {
         new Area(
            company,
            menu,
            lorem.characters(),
            level
         )
      }
   }

   static Area single(Integer level = null, Menu menu = null, Company company = null) {
      return stream(1, level, menu, company).findFirst().orElseThrow { new Exception("Unable to create Area") }
   }
}

@Singleton
@CompileStatic
class AreaDataLoaderService {
   private final AreaRepository areaRepository
   private final CompanyDataLoaderService companyDataLoaderService
   private final MenuDataLoaderService menuDataLoaderService

   AreaDataLoaderService(
      AreaRepository areaRepository,
      CompanyDataLoaderService companyDataLoaderService,
      MenuDataLoaderService menuDataLoaderService
   ) {
      this.areaRepository = areaRepository
      this.companyDataLoaderService = companyDataLoaderService
      this.menuDataLoaderService = menuDataLoaderService
   }

   Stream<Area> stream(int number = 1, Integer level = null, Menu menu = null, Company company = null) {
      final Menu areaMenu = menu != null ? menu : menuDataLoaderService.single()
      final Company areaCompany = company != null ? company : companyDataLoaderService.single()

      return AreaTestDataLoader.stream(number, level, areaMenu, areaCompany)
         .map {
            areaRepository.insert(it)
         }
   }

   Area single(Integer level = null, Menu menu = null, Company company = null) {
      return stream(1, level, menu, company).findFirst().orElseThrow { new Exception("Unable to create Area") }
   }
}
