package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.Company
import com.hightouchinc.cynergi.middleware.entity.CompanyModuleAccess
import com.hightouchinc.cynergi.middleware.entity.Module
import com.hightouchinc.cynergi.middleware.repository.CompanyModuleAccessRepository
import com.hightouchinc.cynergi.middleware.service.ModuleService
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Singleton

object CompanyModuleAccessTestDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1, levelIn: Int = 10, moduleIn: Module, companyIn: Company? = null): Stream<CompanyModuleAccess> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val num = faker.number()
      val level = if (levelIn in 1..99) levelIn else num.numberBetween(0, 100)
      val company = companyIn ?: CompanyTestDataLoader.single()

      return IntStream.range(0, number).mapToObj {
         CompanyModuleAccess(
            level = level,
            company = company,
            module = moduleIn
         )
      }
   }

   fun single(moduleIn: Module): CompanyModuleAccess {
      return stream(moduleIn = moduleIn).findFirst().orElseThrow { Exception("Unable to create CompanyModuleAccess") }
   }
}

@Singleton
class CompanyModuleAccessDataLoaderService(
   val companyDataLoaderService: CompanyDataLoaderService,
   val companyModuleAccessRepository: CompanyModuleAccessRepository,
   val moduleService: ModuleService
) {

   fun stream(numberIn: Int = 1, levelIn: Int = 10, companyIn: Company? = null, moduleIn: Module? = null): Stream<CompanyModuleAccess> {
      val company = companyIn ?: companyDataLoaderService.single()
      val module = moduleIn ?: moduleService.randomModule()

      return CompanyModuleAccessTestDataLoader.stream(numberIn, levelIn, module, company).map {
         companyModuleAccessRepository.insert(it)
      }
   }

   fun single(levelIn: Int = 10, companyIn: Company? = null, moduleIn: Module? = null): CompanyModuleAccess {
      return stream(levelIn = levelIn, companyIn = companyIn, moduleIn = moduleIn).findFirst().orElseThrow { Exception("Unable to create CompanyModuleAccess") }
   }
}
