package com.cynergisuite.middleware.company

import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.department.DepartmentEntity
import io.micronaut.context.annotation.Requires
import java.lang.Exception
import javax.inject.Singleton

object CompanyFactory {

   @JvmStatic
   private val companies: List<CompanyEntity> = listOf(
      CompanyEntity(id = 4, number = 0, name = "RENTAL CITY",  dataset = "tstds1"),
      CompanyEntity(id = 1, number = 0, name = "Pelham Trading Post, Inc.", dataset = "tstds2")
   )

   @JvmStatic
   fun findByDataset(dataset: String) = companies.first { it.dataset == dataset }

   @JvmStatic
   fun random() = companies.random()

   @JvmStatic
   fun all(): List<CompanyEntity> = companies
}

@Singleton
@Requires(env = ["develop", "test"])
class CompanyFactoryService(
   private val companyRepository: CompanyRepository
) {

   fun department(dataset: String): CompanyEntity =
      companyRepository.findByDataset(dataset) ?: throw Exception("Unable to find Company $dataset")

   fun random(): CompanyEntity =
      department(CompanyFactory.random().dataset)
}
