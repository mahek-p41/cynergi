package com.cynergisuite.middleware.department

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.department.infrastructure.DepartmentRepository
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object DepartmentFactory {

   @JvmStatic
   private val departments: List<DepartmentEntity> = listOf(
      DepartmentEntity(id = 1, code = "AM", description = "ASST MGR", company = CompanyFactory.tstds1()),
      DepartmentEntity(id = 2, code = "AR", description = "ACCOUNT REP", company = CompanyFactory.tstds1()),
      DepartmentEntity(id = 3, code = "DE", description = "DELIVERY DVR", company = CompanyFactory.tstds1()),
      DepartmentEntity(id = 4, code = "EX", description = "EXECUTIVE", company = CompanyFactory.tstds1()),
      DepartmentEntity(id = 5, code = "MM", description = "MARKET MGR", company = CompanyFactory.tstds1()),
      DepartmentEntity(id = 6, code = "RM", description = "REGIONAL MGR", company = CompanyFactory.tstds1()),
      DepartmentEntity(id = 7, code = "SA", description = "SALES ASSOC", company = CompanyFactory.tstds1()),
      DepartmentEntity(id = 8, code = "SM", description = "STORE MGR", company = CompanyFactory.tstds1()),
      DepartmentEntity(id = 9, code = "TE", description = "TERMINATED", company = CompanyFactory.tstds1()),
      DepartmentEntity(id = 10, code = "NO", description = null, company = CompanyFactory.tstds1()),
      DepartmentEntity(id = 11, code = "CY", description = null, company = CompanyFactory.tstds1()),

      DepartmentEntity(id = 12, code = "AM", description = "ACCOUNT MGR", company = CompanyFactory.tstds2()),
      DepartmentEntity(id = 13, code = "CO", description = "COLLECTIONS", company = CompanyFactory.tstds2()),
      DepartmentEntity(id = 14, code = "DE", description = "DELIVERY", company = CompanyFactory.tstds2()),
      DepartmentEntity(id = 15, code = "DM", description = "DISTRICT MGR", company = CompanyFactory.tstds2()),
      DepartmentEntity(id = 16, code = "MG", description = "MANAGEMENT", company = CompanyFactory.tstds2()),
      DepartmentEntity(id = 17, code = "OF", description = "OFFICE", company = CompanyFactory.tstds2()),
      DepartmentEntity(id = 18, code = "SA", description = "SALES ASSOCI", company = CompanyFactory.tstds2()),
      DepartmentEntity(id = 19, code = "SM", description = "STORE MANAGE", company = CompanyFactory.tstds2()),
      DepartmentEntity(id = 20, code = "WH", description = "WAREHOUSE", company = CompanyFactory.tstds2()),
      DepartmentEntity(id = 21, code = "HO", description = null, company = CompanyFactory.tstds2())
   )

   @JvmStatic
   fun random() = departments.random()

   @JvmStatic
   fun random(company: Company) =
      departments.filter { it.company.myDataset() == company.myDataset() }.random()

   @JvmStatic
   fun randomNotMatchingDataset(company: Company) =
      departments.filter { it.company.myDataset() != company.myDataset() }.random()

   @JvmStatic
   fun forThese(company: Company, code: String): DepartmentEntity =
      departments.first { it.company.myDataset() == company.myDataset() && it.code == code }

   @JvmStatic
   fun all(): List<DepartmentEntity> =
      all(CompanyFactory.tstds1())

   @JvmStatic
   fun all(company: Company = CompanyFactory.tstds1()): List<DepartmentEntity> =
      departments.filter { it.company.myDataset() == company.myDataset() }
}

@Singleton
@Requires(env = ["develop", "test"])
class DepartmentFactoryService(
   private val companyRepository: CompanyRepository,
   private val departmentRepository: DepartmentRepository
) {

   fun department(code: String, company: Company): DepartmentEntity =
      departmentRepository.findOneByCodeAndDataset(code, company) ?: throw Exception("Unable to find department $code")

   fun random(): DepartmentEntity {
      val department = DepartmentFactory.random()

      return departmentRepository.findOneByCodeAndDataset(department.code, department.company) ?: throw Exception("Unable to find random DepartmentEntity")
   }

   fun random(company: Company): DepartmentEntity {
      val department = DepartmentFactory.random(company).copy(company = company)

      return departmentRepository.findOneByCodeAndDataset(department.code, department.company) ?: throw Exception("Unable to find random DepartmentEntity")
   }

   fun randomNotMatchingDataset(company: Company): DepartmentEntity {
      val randomDepartment = DepartmentFactory.randomNotMatchingDataset(company) // find predfined departments using the provided company
      val departmentCompany = companyRepository.findByDataset(randomDepartment.company.myDataset())!! // look up the Company saved in the company table
      val department = randomDepartment.copy(company = departmentCompany) // copy that found company to the final department that will be used for the lookup

      return departmentRepository.findOneByCodeAndDataset(department.code, department.company) ?: throw Exception("Unable to find random DepartmentEntity")
   }

   fun forThese(company: Company, code: String): DepartmentEntity =
      departmentRepository.findOneByCodeAndDataset(code = code, company = company)?.also { it.copy(company = company) } ?: throw Exception("Unable to find random DepartmentEntity")
}
