package com.cynergisuite.middleware.division

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.division.infrastructure.DivisionRepository
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class DivisionService @Inject constructor(
   private val divisionRepository: DivisionRepository,
   private val divisionValidator: DivisionValidator
) {


   fun fetchById(id: Long, company: Company): DivisionDTO? =
      divisionRepository.findOne(id, company)?.let { DivisionDTO(it) }

   @Validated
   fun fetchAll(company: Company, @Valid pageRequest: PageRequest): Page<DivisionDTO> {
      val found = divisionRepository.findAll(company, pageRequest)

      return found.toPage { division: DivisionEntity ->
         DivisionDTO(division)
      }
   }

   @Validated
   fun create(@Valid divisionDTO: DivisionDTO, company: Company): DivisionDTO {
      val toCreate = divisionValidator.validateCreate(divisionDTO, company)

      return DivisionDTO(divisionRepository.insert(toCreate))
   }

   @Validated
   fun update(id: Long, @Valid divisionDTO: DivisionDTO, company: Company): DivisionDTO {
      val toUpdate = divisionValidator.validateUpdate(id, divisionDTO, company)

      return DivisionDTO(divisionRepository.update(toUpdate))
   }

   fun delete(id: Long, company: Company): DivisionDTO? {
      return divisionRepository.delete(id, company)?.let { DivisionDTO(it) }
   }
}
