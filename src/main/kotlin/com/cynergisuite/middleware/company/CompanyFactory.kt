package com.cynergisuite.middleware.company

import com.cynergisuite.middleware.address.AddressEntity
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
         name = "RENTAL CITY",
         doingBusinessAs = null,
         clientCode = "RCT",
         clientId = 1234,
         datasetCode = "tstds1",
         federalIdNumber = "A1000B200"
      ),
      CompanyEntity(
         name = "Pelham Trading Post, Inc.",
         doingBusinessAs = "RentACenter",
         clientCode = "PTP",
         clientId = 4321,
         datasetCode = "tstds2",
         federalIdNumber = "BX101010"
      )
   )

   @JvmStatic
   private val companiesDevData = companies
      .map {
         company ->
         when (company.datasetCode) {
            "tstds1" -> company.copy(datasetCode = "corrto")
            "tstds2" -> company.copy(datasetCode = "corptp")
            else -> company
         }
      }
      .toList()

   @JvmStatic
   fun stream(numberIn: Int = 1, addressIn: AddressEntity? = null): Stream<CompanyEntity> {
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
            clientCode = lorem.characters(3).toUpperCase(),
            clientId = random.nextInt(1000, 10000),
            datasetCode = lorem.characters(6, false),
            federalIdNumber = numbers.valid(),
            address = addressIn
         )
      }
   }

   @JvmStatic
   fun predefined(): List<CompanyEntity> = companies

   @JvmStatic
   fun predefinedDevData(): List<CompanyEntity> = companiesDevData

   @JvmStatic
   fun random() = companies.random()

   @JvmStatic
   fun tstds1() = companies.first { it.datasetCode == "tstds1" }

   @JvmStatic
   fun tstds2() = companies.first { it.datasetCode == "tstds2" }

   @JvmStatic
   fun corrto() = companiesDevData.first { it.datasetCode == "corrto" }

   @JvmStatic
   fun corptp() = companiesDevData.first { it.datasetCode == "corptp" }
}

@Singleton
@Requires(env = ["develop", "test"])
class CompanyFactoryService(
   private val companyRepository: CompanyRepository
) {

   fun streamPredefined(): Stream<CompanyEntity> =
      CompanyFactory.predefined().stream()
         .map { companyRepository.insert(it) }

   fun stream(numberIn: Int = 1, addressIn: AddressEntity? = null): Stream<CompanyEntity> =
      CompanyFactory.stream(numberIn, addressIn)
         .map { companyRepository.insert(it) }

   fun single(): CompanyEntity =
      single(addressIn = null)

   fun single(addressIn: AddressEntity? = null): CompanyEntity =
      stream(numberIn = 1, addressIn = addressIn).findFirst().orElseThrow { Exception("Unable to create CompanyEntity") }

   fun forDatasetCode(datasetCode: String): CompanyEntity {
      return companyRepository.findByDataset(datasetCode) ?: throw Exception("Unable to find CompanyEntity")
   }

   fun random(): CompanyEntity =
      forDatasetCode(CompanyFactory.random().datasetCode)
}
