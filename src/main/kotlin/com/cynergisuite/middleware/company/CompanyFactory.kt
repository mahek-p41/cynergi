package com.cynergisuite.middleware.company

import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Singleton

object CompanyFactory {

   @JvmStatic
   private val companies: List<CompanyEntity> = listOf(
      CompanyEntity(
         id = 4,
         name = "RENTAL CITY",
         doingBusinessAs = null,
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
   fun stream(numberIn: Int = 1): Stream<CompanyEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val company = faker.company()
      val lorem = faker.lorem()
      val numbers = faker.idNumber()
      val random = faker.random()

      return IntStream.range(0, number).mapToObj {
         CompanyEntity(
            id = null,
            name = company.name(),
            doingBusinessAs = company.buzzword(),
            clientCode = lorem.characters(3, 3).toUpperCase(),
            clientId = random.nextInt(1000, 10000),
            datasetCode = lorem.characters(6, 6, false),
            federalTaxNumber = numbers.valid()
         )
      }
   }

   @JvmStatic
   fun all(): List<CompanyEntity> = companies

   @JvmStatic
   fun random() = companies.random()

   @JvmStatic
   fun tstds1() = companies.first { it.datasetCode == "tstds1" }

   @JvmStatic
   fun tstds2() = companies.first { it.datasetCode == "tstds2" }
}

@Singleton
@Requires(env = ["develop", "test"])
class CompanyFactoryService(
   private val companyRepository: CompanyRepository
) {

   fun streamPredfined(): Stream<CompanyEntity> =
      CompanyFactory.all().stream().map { companyRepository.in }

   fun forDatasetCode(datasetCode: String): CompanyEntity =
      companyRepository.findByDataset(datasetCode) ?: throw Exception("Unable to find Company $datasetCode")

   fun random(): CompanyEntity =
      forDatasetCode(CompanyFactory.random().datasetCode)
}
