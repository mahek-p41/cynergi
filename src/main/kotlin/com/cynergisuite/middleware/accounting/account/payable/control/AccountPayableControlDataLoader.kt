package com.cynergisuite.middleware.accounting.account.payable.control

import com.cynergisuite.middleware.accounting.account.payable.PrintCurrencyIndicatorTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.PrintCurrencyIndicatorTypeDataLoader
import com.cynergisuite.middleware.accounting.account.payable.PurchaseOrderNumberRequiredIndicatorTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.PurchaseOrderNumberRequiredIndicatorTypeDataLoader
import com.cynergisuite.middleware.accounting.account.payable.control.infrastructure.AccountPayableControlRepository
import com.cynergisuite.middleware.company.Company
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

object AccountPayableControlDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1): Stream<AccountPayableControlEntity> {
      val number = if (numberIn > 0) numberIn else 1

      return IntStream.range(0, number).mapToObj {
         AccountPayableControlEntity(
            id = Random.nextLong(),
            payAfterDiscountDate = Random.nextBoolean(),
            resetExpense = Random.nextBoolean(),
            useRebatesIndicator = Random.nextBoolean(),
            tradeCompanyIndicator = Random.nextBoolean(),
            printCurrencyIndicatorType = PrintCurrencyIndicatorTypeDataLoader.random(),
            lockInventoryIndicator = Random.nextBoolean(),
            purchaseOrderNumberRequiredIndicatorType = PurchaseOrderNumberRequiredIndicatorTypeDataLoader.random()
         )
      }
   }

   @JvmStatic
   fun streamDTO(numberIn: Int = 1): Stream<AccountPayableControlDTO> {
      val number = if (numberIn > 0) numberIn else 1

      return IntStream.range(0, number).mapToObj {
         AccountPayableControlDTO(
            payAfterDiscountDate = Random.nextBoolean(),
            resetExpense = Random.nextBoolean(),
            useRebatesIndicator = Random.nextBoolean(),
            tradeCompanyIndicator = Random.nextBoolean(),
            printCurrencyIndicatorType = PrintCurrencyIndicatorTypeDTO(PrintCurrencyIndicatorTypeDataLoader.random()),
            lockInventoryIndicator = Random.nextBoolean(),
            purchaseOrderNumberRequiredIndicatorType = PurchaseOrderNumberRequiredIndicatorTypeDTO(PurchaseOrderNumberRequiredIndicatorTypeDataLoader.random())
         )
      }
   }

   @Singleton
   @Requires(env = ["develop", "test"])
   class AccountPayableControlDataLoaderService @Inject constructor(
      private val accountPayableControlRepository: AccountPayableControlRepository
   ) {
      fun stream(numberIn: Int = 1, company: Company): Stream<AccountPayableControlEntity> {
         return AccountPayableControlDataLoader.stream(numberIn).map {
            accountPayableControlRepository.insert(it, company)
         }
      }

      fun single(company: Company): AccountPayableControlEntity {
         return stream(1, company).findFirst().orElseThrow { Exception("Unable to create AccountPayableControl") }
      }

      fun singleDTO(): AccountPayableControlDTO {
         return AccountPayableControlDataLoader.streamDTO(1).findFirst().orElseThrow { Exception("Unable to create AccountPayableControl") }
      }
   }
}
