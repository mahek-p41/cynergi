package com.cynergisuite.middleware.company

import com.cynergisuite.middleware.address.AddressEntity
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

import java.util.stream.IntStream
import java.util.stream.Stream

class CompanyFactory {

   private static List<CompanyEntity> companies = [
      new CompanyEntity(
         null,
         "ADVANCED RTO, LLC",
         "testDba Stuff",
         "cst",
         1812,
         "coravt",
         null,
         null,
         false,
      ),
      new CompanyEntity(
         null,
         "AMAZING RTO",
         null,
         "cst",
         5124,
         "corrto",
         "48-1256789",
         null,
         true,
      )
   ]

   private static List<CompanyEntity> companiesDevData = companies
      .collect {company ->
         switch (company.datasetCode) {
            case "coravt": return company.copyMeWithNewDatasetCode("coravt")
            case "corrto": return company.copyMeWithNewDatasetCode("corrto")
            default: return company
         }
      }
      .toList()

   static Stream<CompanyEntity> stream(int numberIn = 1, AddressEntity addressIn = null) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final company = faker.company()
      final lorem = faker.lorem()
      final numbers = faker.idNumber()
      final random = faker.random()

      return IntStream.range(0, number).mapToObj {
          new CompanyEntity(
              null,
              company.name(),
              company.buzzword(),
              lorem.characters(3).toUpperCase(),
              random.nextInt(1000, 10000),
              lorem.characters(6, false),
              numbers.valid(),
              addressIn,
              random.nextBoolean(),
          )
      }
   }

   static List<CompanyEntity> predefined() { companies }

   static CompanyEntity random() { companies.random() }

   static CompanyEntity tstds1() { companies.find { it.datasetCode == "coravt" } }

   static CompanyEntity tstds2() { companies.find { it.datasetCode == "corrto" } }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class CompanyFactoryService {
   private final CompanyRepository companyRepository

   CompanyFactoryService(CompanyRepository companyRepository) {
      this.companyRepository = companyRepository
   }

   Stream<CompanyEntity> stream(int numberIn = 1, AddressEntity addressIn = null) {
      return CompanyFactory.stream(numberIn, addressIn)
         .map { companyRepository.insert(it) }
   }

   Stream<CompanyEntity> streamPredefined() {
      return CompanyFactory.predefined().stream()
         .map {companyRepository.insert(it) }
   }

   CompanyEntity single(AddressEntity addressIn = null) {
      stream(1, addressIn).findFirst().orElseThrow { new Exception("Unable to create CompanyEntity") }
   }

   CompanyEntity forDatasetCode(String datasetCode) {
      final company = companyRepository.findByDataset(datasetCode)

      if (company != null) {
         return company
      } else {
         throw new Exception("Unable to find CompanyEntity")
      }
   }

   CompanyEntity random() {
      forDatasetCode(CompanyFactory.random().datasetCode)
   }
}
