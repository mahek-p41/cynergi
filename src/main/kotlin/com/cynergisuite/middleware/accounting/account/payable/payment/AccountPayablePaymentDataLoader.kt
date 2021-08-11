package com.cynergisuite.middleware.accounting.account.payable.payment

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentRepository
import com.cynergisuite.middleware.accounting.bank.BankEntity
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.vendor.VendorDTO
import com.cynergisuite.middleware.vendor.VendorEntity
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.time.LocalDate
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

object AccountPayablePaymentDataLoader {

   @JvmStatic
   fun stream(
      numberIn: Int = 1,
      bank: BankEntity,
      vendor: VendorEntity,
      status: AccountPayablePaymentStatusType?,
      type: AccountPayablePaymentTypeType?
   ): Stream<AccountPayablePaymentEntity> {
      val number = if (numberIn < 0) 1 else numberIn
      val faker = Faker()
      val random = faker.random()

      return IntStream.range(0, number).mapToObj {
         AccountPayablePaymentEntity(
            bank = bank,
            vendor = vendor,
            status = status ?: AccountPayablePaymentStatusTypeDataLoader.random(),
            type = type ?: AccountPayablePaymentTypeTypeDataLoader.random(),
            paymentDate = LocalDate.now().minusDays(Random.nextLong(15)),
            dateCleared = LocalDate.now().minusDays(Random.nextLong(10)),
            dateVoided = LocalDate.now().minusDays(Random.nextLong(5)),
            paymentNumber = random.nextInt(1000).toString(),
            amount = random.nextInt(1000).toBigDecimal()
         )
      }
   }

   @JvmStatic
   fun streamDTO(
      numberIn: Int = 1,
      bank: BankEntity,
      vendor: VendorEntity
   ): Stream<AccountPayablePaymentDTO> {
      val number = if (numberIn < 0) 1 else numberIn
      val faker = Faker()
      val random = faker.random()

      return IntStream.range(0, number).mapToObj {
         AccountPayablePaymentDTO(
            bank = SimpleIdentifiableDTO(bank),
            vendor = VendorDTO(vendor),
            status = AccountPayablePaymentStatusTypeDTO(AccountPayablePaymentStatusTypeDataLoader.random()),
            type = AccountPayablePaymentTypeTypeDTO(AccountPayablePaymentTypeTypeDataLoader.random()),
            paymentDate = LocalDate.now().minusDays(Random.nextLong(60)),
            dateCleared = LocalDate.now().minusDays(Random.nextLong(60)),
            dateVoided = LocalDate.now().minusDays(Random.nextLong(60)),
            paymentNumber = random.nextInt(1000).toString(),
            amount = random.nextInt(10000).toBigDecimal()
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AccountPayablePaymentDataLoaderService @Inject constructor(
   private val apPaymentRepository: AccountPayablePaymentRepository
) {

   fun stream(
      numberIn: Int = 1,
      company: Company,
      bank: BankEntity,
      vendor: VendorEntity,
      status: AccountPayablePaymentStatusType? = null,
      type: AccountPayablePaymentTypeType? = null
   ): Stream<AccountPayablePaymentEntity> {
      return AccountPayablePaymentDataLoader.stream(numberIn, bank, vendor, status, type)
         .map { apPaymentRepository.insert(it, company) }
   }

   fun stream(
      numberIn: Int = 1,
      company: Company,
      bank: BankEntity,
      vendor: VendorEntity
   ): Stream<AccountPayablePaymentEntity> {
      return stream(numberIn, company, bank, vendor, null, null)
   }

   fun single(
      company: Company,
      bank: BankEntity,
      vendor: VendorEntity
   ): AccountPayablePaymentEntity {
      return stream(1, company, bank, vendor).findFirst().orElseThrow { Exception("Unable to create Account Payable Payment") }
   }

   fun single(
      company: Company,
      bank: BankEntity,
      vendor: VendorEntity,
      status: AccountPayablePaymentStatusType,
      type: AccountPayablePaymentTypeType,
   ): AccountPayablePaymentEntity {
      return stream(1, company, bank, vendor, status, type).findFirst().orElseThrow { Exception("Unable to create Account Payable Payment") }
   }

   fun singleDTO(
      bank: BankEntity,
      vendor: VendorEntity
   ): AccountPayablePaymentDTO {
      return AccountPayablePaymentDataLoader.streamDTO(1, bank, vendor).findFirst().orElseThrow { Exception("Unable to create Account Payable Payment") }
   }
}
