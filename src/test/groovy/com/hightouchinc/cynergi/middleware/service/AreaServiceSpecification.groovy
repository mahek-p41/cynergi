package com.hightouchinc.cynergi.middleware.service

import com.hightouchinc.cynergi.middleware.service.spi.ServiceSpecificationBase
import com.hightouchinc.cynergi.test.data.loader.AreaDataLoaderService
import com.hightouchinc.cynergi.test.data.loader.CompanyDataLoaderService
import com.hightouchinc.cynergi.test.data.loader.MenuDataLoaderService
import com.hightouchinc.cynergi.test.data.loader.ModuleDataLoaderService

class AreaServiceSpecification extends ServiceSpecificationBase {
   final def areaService = applicationContext.getBean(AreaService)
   final def areaDataLoaderServce = applicationContext.getBean(AreaDataLoaderService)
   final def companyDataLoaderService = applicationContext.getBean(CompanyDataLoaderService)
   final def menuDataLoaderService = applicationContext.getBean(MenuDataLoaderService)
   final def moduleDataLoaderService = applicationContext.getBean(ModuleDataLoaderService)

   void "test" () {
      given:
         final menus = menuDataLoaderService.stream(6)
   }
}
