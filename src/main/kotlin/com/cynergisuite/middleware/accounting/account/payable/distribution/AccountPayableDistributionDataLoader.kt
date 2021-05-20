package com.cynergisuite.middleware.accounting.account.payable.distribution

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure.AccountPayableDistributionRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.store.Store
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.math.BigDecimal
import java.math.RoundingMode.HALF_EVEN
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

object AccountPayableDistributionDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1, profitCenter: Store, account: AccountEntity, name: String? = null): Stream<AccountPayableDistributionEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         AccountPayableDistributionEntity(
            name = name ?: lorem.characters(5, 10),
            profitCenter = profitCenter,
            account = account,
            percent = Random.nextInt(1, 100).toBigDecimal().divide(BigDecimal(100)).setScale(7, HALF_EVEN)
         )
      }
   }

   @JvmStatic
   fun streamDTO(numberIn: Int = 1, profitCenter: SimpleLegacyIdentifiableDTO, account: SimpleIdentifiableDTO, name: String? = null): Stream<AccountPayableDistributionDTO> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         AccountPayableDistributionDTO(
            name = name ?: lorem.characters(5, 10),
            profitCenter = profitCenter,
            account = account,
            percent = Random.nextInt(1, 100).toBigDecimal().divide(BigDecimal(100)).setScale(7, HALF_EVEN)
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AccountPayableDistributionDataLoaderService @Inject constructor(
   private val repository: AccountPayableDistributionRepository
) {

   fun stream(numberIn: Int = 1, profitCenterIn: Store, accountIn: AccountEntity, companyIn: Company): Stream<AccountPayableDistributionEntity> {
      return AccountPayableDistributionDataLoader.stream(numberIn, profitCenterIn, accountIn).map {
         repository.insert(it, companyIn)
      }
   }

   fun single(profitCenterIn: Store, accountIn: AccountEntity, companyIn: Company): AccountPayableDistributionEntity {
      return stream(1, profitCenterIn, accountIn, companyIn).findFirst().orElseThrow { Exception("Unable to create AccountPayableDistribution") }
   }

   fun single(profitCenterIn: Store, accountIn: AccountEntity, companyIn: Company, nameIn: String? = null): AccountPayableDistributionEntity {
      return AccountPayableDistributionDataLoader.stream(1, profitCenterIn, accountIn, nameIn)
         .map { repository.insert(it, companyIn) }
         .findFirst().orElseThrow { Exception("Unable to create AccountPayableDistribution") }
   }

   fun singleDTO(profitCenterIn: SimpleLegacyIdentifiableDTO, accountIn: SimpleIdentifiableDTO, nameIn: String? = null): AccountPayableDistributionDTO {
      return AccountPayableDistributionDataLoader.streamDTO(1, profitCenter = profitCenterIn, account = accountIn, name = nameIn).findFirst().orElseThrow { Exception("Unable to create AccountPayableDistribution") }
   }
}
