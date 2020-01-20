package com.cynergisuite.middleware.company

import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object CompanyFactory {

   @JvmStatic
   private val companies: List<CompanyEntity> = listOf(
      CompanyEntity(
         id = 4,
         name = "RENTAL CITY",
         doingBusinessAs = null!!,
         clientCode = "RCT",
         clientId = 1234,
         datasetCode = "tstds1",
         federalTaxNumber = "A1000B200"),
      CompanyEntity(
         id = 1,
         name = "Pelham Trading Post, Inc.",
         doingBusinessAs = "RentACenter",
         clientCode = "PTP",
         clientId = 4321,
         datasetCode = "tstds2",
         federalTaxNumber = "BX101010")
   )

   @JvmStatic
   fun all(): List<CompanyEntity> = companies
}

@Singleton
@Requires(env = ["develop", "test"])
class CompanyFactoryService(
   private val companyRepository: CompanyRepository
) {

   fun forDatasetCode(datasetCode: String): CompanyEntity =
      companyRepository.findByDataset(datasetCode) ?: throw Exception("Unable to find Company $datasetCode")

   fun random(): CompanyEntity =
      forDatasetCode(CompanyFactory.random().datasetCode)
}
