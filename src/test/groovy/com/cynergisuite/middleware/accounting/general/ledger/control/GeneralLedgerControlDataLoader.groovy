package com.cynergisuite.middleware.accounting.general.ledger.control

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.general.ledger.control.infrastructure.GeneralLedgerControlRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.Store
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Singleton

@CompileStatic
class GeneralLedgerControlDataLoader {

   static Stream<GeneralLedgerControlEntity> stream(
      int numberIn = 1,
      CompanyEntity company,
      Store defaultProfitCenter,
      AccountEntity defaultAccountPayableAccount,
      AccountEntity defaultAccountPayableDiscountAccount,
      AccountEntity defaultAccountReceivableAccount,
      AccountEntity defaultAccountReceivableDiscountAccount,
      AccountEntity defaultAccountMiscInventoryAccount,
      AccountEntity defaultAccountSerializedInventoryAccount,
      AccountEntity defaultAccountUnbilledInventoryAccount,
      AccountEntity defaultAccountFreightAccount
   ) {
      final number = numberIn > 0 ? numberIn : 1

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerControlEntity(
            null,
            company,
            defaultProfitCenter,
            defaultAccountPayableAccount,
            defaultAccountPayableDiscountAccount,
            defaultAccountReceivableAccount,
            defaultAccountReceivableDiscountAccount,
            defaultAccountMiscInventoryAccount,
            defaultAccountSerializedInventoryAccount,
            defaultAccountUnbilledInventoryAccount,
            defaultAccountFreightAccount
         )
      }
   }

   static Stream<GeneralLedgerControlDTO> streamDTO(
      int numberIn = 1,
      SimpleLegacyIdentifiableDTO defaultProfitCenter,
      AccountDTO defaultAccountPayableAccount,
      AccountDTO defaultAccountPayableDiscountAccount,
      AccountDTO defaultAccountReceivableAccount,
      AccountDTO defaultAccountReceivableDiscountAccount,
      AccountDTO defaultAccountMiscInventoryAccount,
      AccountDTO defaultAccountSerializedInventoryAccount,
      AccountDTO defaultAccountUnbilledInventoryAccount,
      AccountDTO defaultAccountFreightAccount
   ) {
      final number = numberIn > 0 ? numberIn : 1

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerControlDTO([
            'defaultProfitCenter': defaultProfitCenter,
            'defaultAccountPayableAccount': defaultAccountPayableAccount,
            'defaultAccountPayableDiscountAccount': defaultAccountPayableDiscountAccount,
            'defaultAccountReceivableAccount': defaultAccountReceivableAccount,
            'defaultAccountReceivableDiscountAccount': defaultAccountReceivableDiscountAccount,
            'defaultAccountMiscInventoryAccount': defaultAccountMiscInventoryAccount,
            'defaultAccountSerializedInventoryAccount': defaultAccountSerializedInventoryAccount,
            'defaultAccountUnbilledInventoryAccount': defaultAccountUnbilledInventoryAccount,
            'defaultAccountFreightAccount': defaultAccountFreightAccount
         ])
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class GeneralLedgerControlDataLoaderService {
   private final GeneralLedgerControlRepository generalLedgerControlRepository

   GeneralLedgerControlDataLoaderService(GeneralLedgerControlRepository generalLedgerControlRepository) {
      this.generalLedgerControlRepository = generalLedgerControlRepository
   }

   Stream<GeneralLedgerControlEntity> stream(
      int numberIn = 1,
      CompanyEntity company,
      Store store,
      AccountEntity defaultAccountPayableAccount,
      AccountEntity defaultAccountPayableDiscountAccount,
      AccountEntity defaultAccountReceivableAccount,
      AccountEntity defaultAccountReceivableDiscountAccount,
      AccountEntity defaultAccountMiscInventoryAccount,
      AccountEntity defaultAccountSerializedInventoryAccount,
      AccountEntity defaultAccountUnbilledInventoryAccount,
      AccountEntity defaultAccountFreightAccount
   ) {
      return GeneralLedgerControlDataLoader.stream(
         numberIn,
         company,
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

   GeneralLedgerControlEntity single(
      CompanyEntity company,
      Store store,
      AccountEntity defaultAccountPayableAccount,
      AccountEntity defaultAccountPayableDiscountAccount,
      AccountEntity defaultAccountReceivableAccount,
      AccountEntity defaultAccountReceivableDiscountAccount,
      AccountEntity defaultAccountMiscInventoryAccount,
      AccountEntity defaultAccountSerializedInventoryAccount,
      AccountEntity defaultAccountUnbilledInventoryAccount,
      AccountEntity defaultAccountFreightAccount
   ) {
      return stream(
         1,
         company,
         store,
         defaultAccountPayableAccount,
         defaultAccountPayableDiscountAccount,
         defaultAccountReceivableAccount,
         defaultAccountReceivableDiscountAccount,
         defaultAccountMiscInventoryAccount,
         defaultAccountSerializedInventoryAccount,
         defaultAccountUnbilledInventoryAccount,
         defaultAccountFreightAccount
      ).findFirst().orElseThrow { new Exception("Unable to create GeneralLedgerControl") }
   }

   GeneralLedgerControlDTO singleDTO(
      SimpleLegacyIdentifiableDTO defaultProfitCenter,
      AccountDTO defaultAccountPayableAccount,
      AccountDTO defaultAccountPayableDiscountAccount,
      AccountDTO defaultAccountReceivableAccount,
      AccountDTO defaultAccountReceivableDiscountAccount,
      AccountDTO defaultAccountMiscInventoryAccount,
      AccountDTO defaultAccountSerializedInventoryAccount,
      AccountDTO defaultAccountUnbilledInventoryAccount,
      AccountDTO defaultAccountFreightAccount
   ) {
      return GeneralLedgerControlDataLoader.streamDTO(
         1,
         defaultProfitCenter,
         defaultAccountPayableAccount,
         defaultAccountPayableDiscountAccount,
         defaultAccountReceivableAccount,
         defaultAccountReceivableDiscountAccount,
         defaultAccountMiscInventoryAccount,
         defaultAccountSerializedInventoryAccount,
         defaultAccountUnbilledInventoryAccount,
         defaultAccountFreightAccount
      ).findFirst().orElseThrow { new Exception("Unable to create GeneralLedgerControl") }
   }
}
