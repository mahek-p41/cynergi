package com.hightouchinc.cynergi.middleware.service

import com.hightouchinc.cynergi.middleware.entity.Verification
import com.hightouchinc.cynergi.middleware.entity.VerificationDto
import com.hightouchinc.cynergi.middleware.repository.VerificationRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Defines the business service for handling the loading and processing of the [Verification] entity through the [VerificationDto]
 * that is provided by the web tier
 *
 * @property verificationRepository holds the database access definition for dealing with the verification table
 */
@Singleton
class VerificationService @Inject constructor(
   private val verificationRepository: VerificationRepository
) : IdentifiableService<VerificationDto> {
   override fun fetchById(id: Long): VerificationDto? =
      verificationRepository.findOne(id = id)?.let { VerificationDto(entity = it) }

   fun fetchByCustomerAccount(customerAccount: String): VerificationDto? =
      verificationRepository.findByCustomerAccount(customerAccount = customerAccount)?.let { VerificationDto(entity = it) }

   override fun exists(id: Long): Boolean =
      verificationRepository.exists(id = id)

   fun exists(customerAccount: String): Boolean =
      verificationRepository.exists(customerAccount = customerAccount)

   fun create(dto: VerificationDto, parent: String): VerificationDto =
      VerificationDto(
         entity = verificationRepository.insert(entity = Verification(dto = dto, company = parent))
      )

   fun update(dto: VerificationDto, parent: String): VerificationDto =
      VerificationDto(
         entity = verificationRepository.update(entity = Verification(dto = dto, company = parent))
      )
}
