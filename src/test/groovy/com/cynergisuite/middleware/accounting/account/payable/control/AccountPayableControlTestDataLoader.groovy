package com.cynergisuite.middleware.accounting.account.payable.control


import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableCheckFormTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableCheckFormTypeDataLoader
import com.cynergisuite.middleware.accounting.account.payable.PrintCurrencyIndicatorTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.PrintCurrencyIndicatorTypeDataLoader
import com.cynergisuite.middleware.accounting.account.payable.PurchaseOrderNumberRequiredIndicatorTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.PurchaseOrderNumberRequiredIndicatorTypeDataLoader
import com.cynergisuite.middleware.accounting.account.payable.control.infrastructure.AccountPayableControlRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
import kotlin.jvm.JvmStatic

import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class AccountPayableControlTestDataLoader {

   static Stream<AccountPayableControlEntity> stream(int numberIn = 1, AccountEntity generalLedgerInventoryClearingAccount, AccountEntity generalLedgerInventoryAccount) {
      final number = numberIn > 0 ? numberIn : 1
      final random = new Faker().random()

      return IntStream.range(0, number).mapToObj {
         new AccountPayableControlEntity(
            UUID.randomUUID(),
            AccountPayableCheckFormTypeDataLoader.random(),
            random.nextBoolean(),
            random.nextBoolean(),
            random.nextBoolean(),
            random.nextBoolean(),
            PrintCurrencyIndicatorTypeDataLoader.random(),
            random.nextBoolean(),
            PurchaseOrderNumberRequiredIndicatorTypeDataLoader.random(),
            generalLedgerInventoryClearingAccount,
            generalLedgerInventoryAccount
         )
      }
   }

   @JvmStatic
   static Stream<AccountPayableControlDTO> streamDTO(int numberIn = 1, AccountDTO generalLedgerInventoryClearingAccount, AccountDTO generalLedgerInventoryAccount) {
      final number = numberIn > 0 ? numberIn : 1
      final random = new Faker().random()

      return IntStream.range(0, number).mapToObj {
         new AccountPayableControlDTO([
            'checkFormType' : new AccountPayableCheckFormTypeDTO(AccountPayableCheckFormTypeDataLoader.random()),
            'payAfterDiscountDate' : random.nextBoolean(),
            'resetExpense' : random.nextBoolean(),
            'useRebatesIndicator' : random.nextBoolean(),
            'tradeCompanyIndicator' : random.nextBoolean(),
            'printCurrencyIndicatorType' : new PrintCurrencyIndicatorTypeDTO(PrintCurrencyIndicatorTypeDataLoader.random()),
            'lockInventoryIndicator' : random.nextBoolean(),
            'purchaseOrderNumberRequiredIndicatorType' : new PurchaseOrderNumberRequiredIndicatorTypeDTO(PurchaseOrderNumberRequiredIndicatorTypeDataLoader.random()),
            'generalLedgerInventoryClearingAccount' : generalLedgerInventoryClearingAccount,
            'generalLedgerInventoryAccount' : generalLedgerInventoryAccount
         ])
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AccountPayableControlTestDataLoaderService {
   private final AccountPayableControlRepository accountPayableControlRepository

   AccountPayableControlTestDataLoaderService(AccountPayableControlRepository accountPayableControlRepository) {
      this.accountPayableControlRepository = accountPayableControlRepository
   }

   Stream<AccountPayableControlEntity> stream(int numberIn = 1, CompanyEntity company, AccountEntity generalLedgerInventoryClearingAccount, AccountEntity generalLedgerInventoryAccount) {
      return AccountPayableControlTestDataLoader.stream(numberIn, generalLedgerInventoryClearingAccount, generalLedgerInventoryAccount).map {
         accountPayableControlRepository.insert(it, company)
      }
   }

   AccountPayableControlEntity single(CompanyEntity company, AccountEntity generalLedgerInventoryClearingAccount, AccountEntity generalLedgerInventoryAccount) {
      return stream(1, company, generalLedgerInventoryClearingAccount, generalLedgerInventoryAccount).findFirst().orElseThrow { new Exception("Unable to create AccountPayableControl") }
   }

   AccountPayableControlDTO singleDTO(AccountDTO generalLedgerInventoryClearingAccount, AccountDTO generalLedgerInventoryAccount) {
      return AccountPayableControlTestDataLoader.streamDTO(1, generalLedgerInventoryClearingAccount, generalLedgerInventoryAccount).findFirst().orElseThrow { new Exception("Unable to create AccountPayableControl") }
   }
}
