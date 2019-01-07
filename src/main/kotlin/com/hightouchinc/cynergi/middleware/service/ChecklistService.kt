package com.hightouchinc.cynergi.middleware.service

import com.hightouchinc.cynergi.middleware.entity.Checklist
import com.hightouchinc.cynergi.middleware.entity.ChecklistDto
import com.hightouchinc.cynergi.middleware.repository.ChecklistRepository
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Defines the business service for handling the loading and processing of the [Checklist] entity through the [ChecklistDto]
 * that is provided by the web tier
 *
 * @property checklistRepository holds the database access definition for dealing with the checklist table
 */
@Validated
@Singleton
class ChecklistService @Inject constructor(
   private val checklistRepository: ChecklistRepository
): NestedCrudService<ChecklistDto, String> {
   override fun fetchById(id: Long): ChecklistDto? =
      checklistRepository.findOne(id = id)?.let { ChecklistDto(checklist = it) }

   fun fetchByCustomerAccount(customerAccount: String): ChecklistDto? =
      checklistRepository.findByCustomerAccount(customerAccount = customerAccount)?.let { ChecklistDto(checklist = it) }

   override fun exists(id: Long): Boolean =
      checklistRepository.exists(id = id)

   fun exists(customerAccount: String): Boolean =
      checklistRepository.exists(customerAccount = customerAccount)

   override fun create(dto: ChecklistDto, parent: String): ChecklistDto =
      ChecklistDto(
         checklist = checklistRepository.insert(entity = Checklist(dto = dto, company = parent))
      )

   override fun update(dto: ChecklistDto, parent: String): ChecklistDto =
      ChecklistDto(
         checklist = checklistRepository.update(entity = Checklist(dto = dto, company = parent))
      )
}
