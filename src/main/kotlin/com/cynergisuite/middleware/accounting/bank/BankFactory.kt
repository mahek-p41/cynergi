package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.address.AddressEntity
import com.cynergisuite.middleware.address.AddressValueObject
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreEntity
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

object BankFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, company: Company, store: Store, accountEntity: AccountEntity, currencyType: BankCurrencyType): Stream<BankEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()

      return IntStream.range(0, number).mapToObj {
         BankEntity(
            company = CompanyEntity.create(company)!!,
            address = AddressEntity(
               name = faker.address().firstName(),
               address1 = faker.address().streetName(),
               address2 = faker.address().secondaryAddress(),
               city = faker.address().cityName(),
               state = faker.address().stateAbbr(),
               postalCode = faker.address().zipCode(),
               latitude = "%.6f".format(Random.nextDouble(30.0, 44.0)).toDouble(),
               longitude = "%.6f".format(Random.nextDouble(-120.0, -70.0)).toDouble(),
               country = "USA",
               county = "Sedgwich County",
               phone = faker.phoneNumber().phoneNumber(),
               fax = faker.phoneNumber().phoneNumber()
            ),
            name = faker.company().name(),
            generalLedgerProfitCenter = StoreEntity(store.myId(), store.myNumber(), store.myName(), store.myRegion(), store.myCompany()),
            generalLedgerAccount = accountEntity,
            accountNumber = Random.nextInt(1000, 1000000).toString(),
            currency = currencyType
         )
      }
   }

   @JvmStatic
   fun streamDTO(numberIn: Int = 1, store: Store, accountEntity: AccountEntity, currencyType: BankCurrencyType): Stream<BankDTO> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()

      return IntStream.range(0, number).mapToObj {
         BankDTO(
            address = AddressValueObject(
               name = faker.address().firstName(),
               address1 = faker.address().streetName(),
               address2 = faker.address().secondaryAddress(),
               city = faker.address().cityName(),
               state = faker.address().stateAbbr(),
               postalCode = faker.address().zipCode(),
               latitude = "%.6f".format(Random.nextDouble(30.0, 44.0)).toDouble(),
               longitude = "%.6f".format(Random.nextDouble(-120.0, -70.0)).toDouble(),
               country = "USA",
               county = "Sedgwich County",
               phone = faker.phoneNumber().phoneNumber(),
               fax = faker.phoneNumber().phoneNumber()
            ),
            name = faker.company().name(),
            generalLedgerProfitCenter = SimpleIdentifiableDTO(store.myId()),
            generalLedgerAccount = SimpleIdentifiableDTO(accountEntity.id),
            accountNumber = Random.nextInt(1000, 1000000).toString(),
            currency = BankCurrencyTypeValueObject(currencyType)
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class BankFactoryService @Inject constructor(
   private val bankRepository: BankRepository,
   private val currencyFactoryService: CurrencyFactoryService
) {

   fun stream(numberIn: Int = 1, company: Company, store: Store, account: AccountEntity): Stream<BankEntity> {
      return BankFactory.stream(numberIn, company, store, account,  currencyFactoryService.random()).map {
         bankRepository.insert(it)
      }
   }

   fun single(company: Company, store: Store, account: AccountEntity): BankEntity {
      return stream(1, company, store, account).findFirst().orElseThrow { Exception("Unable to create Bank")}
   }

   fun singleDTO(store: Store, account: AccountEntity): BankDTO {
      return BankFactory.streamDTO(1,  store, account,  currencyFactoryService.random()).findFirst().orElseThrow { Exception("Unable to create Bank")}
   }
}
