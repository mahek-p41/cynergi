package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.middleware.address.AddressEntity
import com.cynergisuite.middleware.address.AddressValueObject
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyValueObject
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreDTO
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

object BankFactory {

   @JvmStatic
   private val intCounter = AtomicInteger(1)

   @JvmStatic
   fun stream(numberIn: Int = 1, company: Company, store: Store, currencyType: BankCurrencyType): Stream<BankEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()

      return IntStream.range(0, number).mapToObj {
         BankEntity(
            company = company,
            address = AddressEntity(
               number = Random.nextInt(from = 1, until = 10),
               name = faker.address().firstName(),
               address1 = faker.address().streetName(),
               address2 = faker.address().secondaryAddress(),
               city = faker.address().cityName(),
               state = faker.address().stateAbbr(),
               postalCode = faker.address().zipCode(),
               latitude = "%.6f".format(Random.nextDouble(30.0, 44.0)).toDouble(),
               longitude = "%.6f".format(Random.nextDouble(-120.0, -70.0)).toDouble(),
               country = "USA",
               county = "Sedgwich County"
            ),
            name = faker.company().name(),
            number = intCounter.getAndIncrement(),
            generalLedgerProfitCenter = store,
            accountNumber = Random.nextInt(1, 100),
            currency = currencyType
         )
      }
   }

   @JvmStatic
   fun streamDTO(numberIn: Int = 1, company: Company, store: Store, currencyType: BankCurrencyType): Stream<BankDTO> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()

      return IntStream.range(0, number).mapToObj {
         BankDTO(
            company = CompanyValueObject.create(company)!!,
            address = AddressValueObject(
               number = Random.nextInt(from = 1, until = 10),
               name = faker.address().firstName(),
               address1 = faker.address().streetName(),
               address2 = faker.address().secondaryAddress(),
               city = faker.address().cityName(),
               state = faker.address().stateAbbr(),
               postalCode = faker.address().zipCode(),
               latitude = "%.6f".format(Random.nextDouble(30.0, 44.0)).toDouble(),
               longitude = "%.6f".format(Random.nextDouble(-120.0, -70.0)).toDouble(),
               country = "USA",
               county = "Sedgwich County"
            ),
            name = faker.company().name(),
            number = intCounter.getAndIncrement(),
            generalLedgerProfitCenter = StoreDTO.create(store)!!,
            accountNumber = Random.nextInt(1, 100),
            currency = currencyType
         )
      }
   }
}

@Singleton
@Requires(env = ["demo", "test"])
class BankFactoryService @Inject constructor(
   private val bankRepository: BankRepository,
   private val currencyFactoryService: CurrencyFactoryService
) {

   fun stream(numberIn: Int = 1, company: Company, store: Store): Stream<BankEntity> {
      return BankFactory.stream(numberIn, company, store,  currencyFactoryService.random()).map {
         bankRepository.insert(it)
      }
   }

   fun single(company: Company, store: Store): BankEntity {
      return stream(1, company, store).findFirst().orElseThrow { Exception("Unable to create Bank")}
   }

   // TODO need a better name to distinguidh return object without
   fun singleDTO(company: Company, store: Store): BankDTO {
      return BankFactory.streamDTO(1, company, store,  currencyFactoryService.random()).findFirst().orElseThrow { Exception("Unable to create Bank")}
   }
}
