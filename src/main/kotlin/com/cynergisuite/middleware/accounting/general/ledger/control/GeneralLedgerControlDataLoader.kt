package com.cynergisuite.middleware.accounting.general.ledger.control

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.general.ledger.control.infrastructure.GeneralLedgerControlRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreEntity
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

object GeneralLedgerControlDataLoader {

   @JvmStatic
   fun stream(
      numberIn: Int = 1,
      periodFrom: LocalDate? = null,
      periodTo: LocalDate? = null,
      defaultProfitCenter: Store,
      defaultAccountPayableAccount: AccountEntity,
      defaultAccountPayableDiscountAccount: AccountEntity,
      defaultAccountReceivableAccount: AccountEntity,
      defaultAccountReceivableDiscountAccount: AccountEntity,
      defaultAccountMiscInventoryAccount: AccountEntity,
      defaultAccountSerializedInventoryAccount: AccountEntity,
      defaultAccountUnbilledInventoryAccount: AccountEntity,
      defaultAccountFreightAccount: AccountEntity
   ): Stream<GeneralLedgerControlEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val date = faker.date()
      val fromDate = if (periodFrom != null) Date.from(periodFrom.atStartOfDay(ZoneId.systemDefault()).toInstant()) else date.future(5, TimeUnit.DAYS)
      val fromLocalDate = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
      val toLocalDate = periodTo ?: date.future(100, TimeUnit.DAYS, fromDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

      return IntStream.range(0, number).mapToObj {
         GeneralLedgerControlEntity(
            id = Random.nextLong(),
            periodFrom = fromLocalDate,
            periodTo = toLocalDate,
            defaultProfitCenter = StoreEntity(defaultProfitCenter.myId(), defaultProfitCenter.myNumber(), defaultProfitCenter.myName(), defaultProfitCenter.myRegion(), defaultProfitCenter.myCompany()),
            defaultAccountPayableAccount = defaultAccountPayableAccount,
            defaultAccountPayableDiscountAccount = defaultAccountPayableDiscountAccount,
            defaultAccountReceivableAccount = defaultAccountReceivableAccount,
            defaultAccountReceivableDiscountAccount = defaultAccountReceivableDiscountAccount,
            defaultAccountMiscInventoryAccount = defaultAccountMiscInventoryAccount,
            defaultAccountSerializedInventoryAccount = defaultAccountSerializedInventoryAccount,
            defaultAccountUnbilledInventoryAccount = defaultAccountUnbilledInventoryAccount,
            defaultAccountFreightAccount = defaultAccountFreightAccount
         )
      }
   }

   @JvmStatic
   fun streamDTO(
      numberIn: Int = 1,
      periodFrom: LocalDate? = null,
      periodTo: LocalDate? = null,
      defaultProfitCenter: SimpleIdentifiableDTO,
      defaultAccountPayableAccount: SimpleIdentifiableDTO,
      defaultAccountPayableDiscountAccount: SimpleIdentifiableDTO,
      defaultAccountReceivableAccount: SimpleIdentifiableDTO,
      defaultAccountReceivableDiscountAccount: SimpleIdentifiableDTO,
      defaultAccountMiscInventoryAccount: SimpleIdentifiableDTO,
      defaultAccountSerializedInventoryAccount: SimpleIdentifiableDTO,
      defaultAccountUnbilledInventoryAccount: SimpleIdentifiableDTO,
      defaultAccountFreightAccount: SimpleIdentifiableDTO
   ): Stream<GeneralLedgerControlDTO> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val date = faker.date()
      val fromDate = if (periodFrom != null) Date.from(periodFrom.atStartOfDay(ZoneId.systemDefault()).toInstant()) else date.future(5, TimeUnit.DAYS)
      val fromLocalDate = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
      val toLocalDate = periodTo ?: date.future(100, TimeUnit.DAYS, fromDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

      return IntStream.range(0, number).mapToObj {
         GeneralLedgerControlDTO(
            periodFrom = fromLocalDate,
            periodTo = toLocalDate,
            defaultProfitCenter = defaultProfitCenter,
            defaultAccountPayableAccount = defaultAccountPayableAccount,
            defaultAccountPayableDiscountAccount = defaultAccountPayableDiscountAccount,
            defaultAccountReceivableAccount = defaultAccountReceivableAccount,
            defaultAccountReceivableDiscountAccount = defaultAccountReceivableDiscountAccount,
            defaultAccountMiscInventoryAccount = defaultAccountMiscInventoryAccount,
            defaultAccountSerializedInventoryAccount = defaultAccountSerializedInventoryAccount,
            defaultAccountUnbilledInventoryAccount = defaultAccountUnbilledInventoryAccount,
            defaultAccountFreightAccount = defaultAccountFreightAccount
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class GeneralLedgerControlDataLoaderService @Inject constructor(
   private val generalLedgerControlRepository: GeneralLedgerControlRepository
) {
   fun stream(
      numberIn: Int = 1,
      company: Company,
      periodFrom: LocalDate? = null,
      periodTo: LocalDate? = null,
      store: Store,
      defaultAccountPayableAccount: AccountEntity,
      defaultAccountPayableDiscountAccount: AccountEntity,
      defaultAccountReceivableAccount: AccountEntity,
      defaultAccountReceivableDiscountAccount: AccountEntity,
      defaultAccountMiscInventoryAccount: AccountEntity,
      defaultAccountSerializedInventoryAccount: AccountEntity,
      defaultAccountUnbilledInventoryAccount: AccountEntity,
      defaultAccountFreightAccount: AccountEntity
   ): Stream<GeneralLedgerControlEntity> {
      return GeneralLedgerControlDataLoader.stream(
         numberIn,
         periodFrom,
         periodTo,
         store,
         defaultAccountPayableAccount,
         defaultAccountPayableDiscountAccount,
         defaultAccountReceivableAccount,
         defaultAccountReceivableDiscountAccount,
         defaultAccountMiscInventoryAccount,
         defaultAccountSerializedInventoryAccount,
         defaultAccountUnbilledInventoryAccount,
         defaultAccountFreightAccount
      ).map {
         generalLedgerControlRepository.insert(it, company)
      }
   }

   fun single(
      company: Company,
      periodFrom: LocalDate? = null,
      periodTo: LocalDate? = null,
      store: Store,
      defaultAccountPayableAccount: AccountEntity,
      defaultAccountPayableDiscountAccount: AccountEntity,
      defaultAccountReceivableAccount: AccountEntity,
      defaultAccountReceivableDiscountAccount: AccountEntity,
      defaultAccountMiscInventoryAccount: AccountEntity,
      defaultAccountSerializedInventoryAccount: AccountEntity,
      defaultAccountUnbilledInventoryAccount: AccountEntity,
      defaultAccountFreightAccount: AccountEntity
   ): GeneralLedgerControlEntity {
      return stream(
         1,
         company,
         periodFrom,
         periodTo,
         store,
         defaultAccountPayableAccount,
         defaultAccountPayableDiscountAccount,
         defaultAccountReceivableAccount,
         defaultAccountReceivableDiscountAccount,
         defaultAccountMiscInventoryAccount,
         defaultAccountSerializedInventoryAccount,
         defaultAccountUnbilledInventoryAccount,
         defaultAccountFreightAccount
      ).findFirst().orElseThrow { Exception("Unable to create GeneralLedgerControl") }
   }

   fun singleDTO(
      periodFrom: LocalDate? = null,
      periodTo: LocalDate? = null,
      defaultProfitCenter: SimpleIdentifiableDTO,
      defaultAccountPayableAccount: SimpleIdentifiableDTO,
      defaultAccountPayableDiscountAccount: SimpleIdentifiableDTO,
      defaultAccountReceivableAccount: SimpleIdentifiableDTO,
      defaultAccountReceivableDiscountAccount: SimpleIdentifiableDTO,
      defaultAccountMiscInventoryAccount: SimpleIdentifiableDTO,
      defaultAccountSerializedInventoryAccount: SimpleIdentifiableDTO,
      defaultAccountUnbilledInventoryAccount: SimpleIdentifiableDTO,
      defaultAccountFreightAccount: SimpleIdentifiableDTO
   ): GeneralLedgerControlDTO {
      return GeneralLedgerControlDataLoader.streamDTO(
         1,
         periodFrom,
         periodTo,
         defaultProfitCenter,
         defaultAccountPayableAccount,
         defaultAccountPayableDiscountAccount,
         defaultAccountReceivableAccount,
         defaultAccountReceivableDiscountAccount,
         defaultAccountMiscInventoryAccount,
         defaultAccountSerializedInventoryAccount,
         defaultAccountUnbilledInventoryAccount,
         defaultAccountFreightAccount
      ).findFirst().orElseThrow { Exception("Unable to create GeneralLedgerControl") }
   }
}
