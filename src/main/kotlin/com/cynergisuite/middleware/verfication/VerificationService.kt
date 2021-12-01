package com.cynergisuite.middleware.verfication

import com.cynergisuite.middleware.verfication.infrastructure.VerificationRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

/**
 * Defines the business service for handling the loading and processing of the [Verification] entity through the [VerificationValueObject]
 * that is provided by the web tier
 *
 * @property verificationRepository holds the database access definition for dealing with the verification table
 */
@Singleton
class VerificationService @Inject constructor(
   private val verificationRepository: VerificationRepository
) {
   fun fetchById(id: Long): VerificationValueObject? =
      verificationRepository.findOne(id = id)?.let { VerificationValueObject(entity = it) }

   fun fetchByCustomerAccount(customerAccount: String): VerificationValueObject? =
      verificationRepository.findByCustomerAccount(customerAccount = customerAccount)?.let { VerificationValueObject(entity = it) }

   fun exists(id: Long): Boolean =
      verificationRepository.exists(id = id)

   fun exists(customerAccount: String): Boolean =
      verificationRepository.exists(customerAccount = customerAccount)

   fun create(dto: VerificationValueObject, parent: String): VerificationValueObject =
      VerificationValueObject(
         entity = verificationRepository.insert(entity = Verification(dto = dto, company = parent))
      )

   fun update(dto: VerificationValueObject, parent: String): VerificationValueObject =
      VerificationValueObject(
         entity = verificationRepository.update(entity = Verification(dto = dto, company = parent))
      )
}
