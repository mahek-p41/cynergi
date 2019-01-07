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
   override fun findById(id: Long): ChecklistDto? =
      checklistRepository.fetchOne(id = id)?.let { ChecklistDto(checklist = it) }

   override fun exists(id: Long): Boolean =
      checklistRepository.exists(id = id)

   override fun save(dto: ChecklistDto, parent: String): ChecklistDto =
      ChecklistDto(
         checklist = checklistRepository.save(entity = Checklist(dto = dto, company = parent))
      )

   override fun update(dto: ChecklistDto, parent: String): ChecklistDto =
      ChecklistDto(
         checklist = checklistRepository.update(entity = Checklist(dto = dto, company = parent))
      )
}
