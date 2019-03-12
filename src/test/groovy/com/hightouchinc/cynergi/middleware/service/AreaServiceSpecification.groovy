package com.hightouchinc.cynergi.middleware.service

import com.hightouchinc.cynergi.middleware.service.spi.ServiceSpecificationBase
import com.hightouchinc.cynergi.test.data.loader.AreaDataLoaderService
import com.hightouchinc.cynergi.test.data.loader.CompanyDataLoaderService
import com.hightouchinc.cynergi.test.data.loader.CompanyModuleAccessDataLoaderService
import com.hightouchinc.cynergi.test.data.loader.DepartmentDataLoaderService
import com.hightouchinc.cynergi.test.data.loader.EmployeeDataLoaderService
import com.hightouchinc.cynergi.test.data.loader.MenuDataLoaderService
import com.hightouchinc.cynergi.test.data.loader.ModuleDataLoaderService

import static java.util.stream.Collectors.toList

class AreaServiceSpecification extends ServiceSpecificationBase {
   final def areaService = applicationContext.getBean(AreaService)
   final def areaDataLoaderService = applicationContext.getBean(AreaDataLoaderService)
   final def companyDataLoaderService = applicationContext.getBean(CompanyDataLoaderService)
   final def companyModuleAccessService = applicationContext.getBean(CompanyModuleAccessDataLoaderService)
   final def departmentDataLoaderService = applicationContext.getBean(DepartmentDataLoaderService)
   final def employeeDataLoaderService = applicationContext.getBean(EmployeeDataLoaderService)
   final def menuDataLoaderService = applicationContext.getBean(MenuDataLoaderService)
   final def moduleDataLoaderService = applicationContext.getBean(ModuleDataLoaderService)

   void "test with area level 10 and module level 10" () {
      given:
         final company = companyDataLoaderService.single()
         final menus = menuDataLoaderService.stream(2).collect(toList())
         final department = departmentDataLoaderService.single(10) // create department with level 10
         final modulesMenuOne = moduleDataLoaderService.stream(4, menus[0]).collect(toList()) // create 4 modules associated with the first menu
         final modulesMenuTwo = moduleDataLoaderService.stream(4, menus[1]).collect(toList()) // create 4 modules associated with the second menu
         final areaMenuOne = areaDataLoaderService.single(10, menus[0], company) // create an area and associate it with the first menu
         final areaMenuTwo = areaDataLoaderService.single(10, menus[1], company) // create a second area and associate it with the second menu
         final employee = employeeDataLoaderService.single(department, company) // create an employee in the company associated with a department
         final companyModuleAccessOne = companyModuleAccessService.associate(modulesMenuOne, company, 10)
         final companyModuleAccessTwo = companyModuleAccessService.associate(modulesMenuTwo, company, 10)
      when:
         final areas = areaService.findAreasByLevel(10)
      then:
         areas.size() == 2
   }
}
