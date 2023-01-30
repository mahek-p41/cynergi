package com.cynergisuite.middleware.department

import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.department.infrastructure.DepartmentRepository
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Inject
import jakarta.inject.Singleton

class DepartmentFactory {

   private static final List<DepartmentEntity> departments = [
      new DepartmentEntity(1, "OP", "OPERATION MG", CompanyFactory.tstds1()),
      new DepartmentEntity(2, "SM", "STORE MGR", CompanyFactory.tstds1()),
      new DepartmentEntity(3, "AM", "ASSIST MGR", CompanyFactory.tstds1()),
      new DepartmentEntity(4, "SL", "SALES MGR", CompanyFactory.tstds1()),
      new DepartmentEntity(5, "AC", "ACCOUNT MGR", CompanyFactory.tstds1()),
      new DepartmentEntity(6, "DE", "DELIVERY DRV", CompanyFactory.tstds1()),
      new DepartmentEntity(7, "TT", "TIRE TECH", CompanyFactory.tstds1()),
      new DepartmentEntity(8, "HO", "HOME OFFICE", CompanyFactory.tstds1()),
      new DepartmentEntity(9, "NO", "NONE", CompanyFactory.tstds1()),
      new DepartmentEntity(10, "CY", "CYNERGI EMP", CompanyFactory.tstds1()),
      new DepartmentEntity(11, "TE", "TERMIN EMPL", CompanyFactory.tstds1()),

      new DepartmentEntity(1, "RF", "RACFI Ops", CompanyFactory.tstds2()),
      new DepartmentEntity(2, "MM", "MARKET MGR", CompanyFactory.tstds2()),
      new DepartmentEntity(3, "SM", "STORE MGR", CompanyFactory.tstds2()),
      new DepartmentEntity(4, "AM", "ASST MGR", CompanyFactory.tstds2()),
      new DepartmentEntity(5, "AR", "ACCOUNT REP", CompanyFactory.tstds2()),
      new DepartmentEntity(6, "SA", "SALES ASSOC", CompanyFactory.tstds2()),
      new DepartmentEntity(7, "DE", "DELIVERY DVR", CompanyFactory.tstds2()),
      new DepartmentEntity(8, "EX", "EXECUTIVE", CompanyFactory.tstds2()),
      new DepartmentEntity(9, "TE", "TERMINATED E", CompanyFactory.tstds2()),
      new DepartmentEntity(10, "RM", "REGIONAL MGR", CompanyFactory.tstds2()),
      new DepartmentEntity(11, "AC", "ACOUNTING MW", CompanyFactory.tstds2()),
      new DepartmentEntity(12, "NO", null, CompanyFactory.tstds2())
   ]

   static DepartmentEntity random() { departments.random() }

   static DepartmentEntity random(CompanyEntity company) {
      departments.findAll { it.company.datasetCode == company.datasetCode }.random()
   }

   static DepartmentEntity randomNotMatchingDataset(CompanyEntity company) {
      departments.findAll { it.company.datasetCode != company.datasetCode }.random()
   }

   static DepartmentEntity forThese(CompanyEntity company, String code) {
      departments.find { it.company.datasetCode == company.datasetCode && it.code == code }
   }

   static List<DepartmentEntity> all(CompanyEntity company = CompanyFactory.tstds1()) {
      departments.findAll { it.company.datasetCode == company.datasetCode }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class DepartmentFactoryService {
   private final CompanyRepository companyRepository
   private final DepartmentRepository departmentRepository

   @Inject
   DepartmentFactoryService(CompanyRepository companyRepository, DepartmentRepository departmentRepository) {
      this.companyRepository = companyRepository
      this.departmentRepository = departmentRepository
   }

   DepartmentEntity department(String code, CompanyEntity company) {
      final department = departmentRepository.findOneByCodeAndDataset(code, company)

      if (department != null) {
         return department
      } else {
         throw new Exception("Unable to find department $code")
      }
   }

   DepartmentEntity random() {
      final department = DepartmentFactory.random()
      final toReturn = departmentRepository.findOneByCodeAndDataset(department.code, department.company)

      if (toReturn != null) {
         return toReturn
      } else {
         throw new Exception("Unable to find random DepartmentEntity")
      }
   }

   DepartmentEntity random(CompanyEntity company) {
      final department = DepartmentFactory.random(company)
      final toReturn = departmentRepository.findOneByCodeAndDataset(department.code, company)

      if (toReturn != null) {
         return toReturn
      } else {
         throw new Exception("Unable to find random DepartmentEntity")
      }
   }

   DepartmentEntity randomNotMatchingDataset(CompanyEntity company) {
      final randomDepartment = DepartmentFactory.randomNotMatchingDataset(company) // find predfined departments using the provided company
      final departmentCompany = companyRepository.findByDataset(randomDepartment.company.datasetCode) // look up the Company saved in the company table
      final department =  new DepartmentEntity(randomDepartment.id, randomDepartment.code, randomDepartment.description, departmentCompany) // copy that found company to the final department that will be used for the lookup
      final toReturn = departmentRepository.findOneByCodeAndDataset(department.code, department.company)

      if (toReturn != null) {
         return toReturn
      } else {
         throw new Exception("Unable to find random DepartmentEntity")
      }
   }

   DepartmentEntity forThese(CompanyEntity company, String code) {
      final toReturn = departmentRepository.findOneByCodeAndDataset(code, company)?.with { new DepartmentEntity(it.id, it.code, it.description, company) }

      if (toReturn != null) {
         return toReturn
      } else {
         throw new Exception("Unable to find random DepartmentEntity")
      }
   }
}
