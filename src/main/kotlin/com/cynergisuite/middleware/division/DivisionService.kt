package com.cynergisuite.middleware.division

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.division.infrastructure.DivisionRepository
import java.util.UUID
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class DivisionService @Inject constructor(
   private val divisionRepository: DivisionRepository,
   private val divisionValidator: DivisionValidator
) {

   fun fetchById(id: UUID, company: CompanyEntity): DivisionDTO? =
      divisionRepository.findOne(id, company)?.let { DivisionDTO(it) }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<DivisionDTO> {
      val found = divisionRepository.findAll(company, pageRequest)

      return found.toPage { division: DivisionEntity ->
         DivisionDTO(division)
      }
   }

   fun create(dto: DivisionDTO, company: CompanyEntity): DivisionDTO {
      val toCreate = divisionValidator.validateCreate(dto, company)

      return DivisionDTO(divisionRepository.insert(toCreate))
   }

   fun update(id: UUID, dto: DivisionDTO, company: CompanyEntity): DivisionDTO {
      val toUpdate = divisionValidator.validateUpdate(id, dto, company)

      return DivisionDTO(divisionRepository.update(id, toUpdate))
   }

   fun delete(id: UUID, company: CompanyEntity): DivisionDTO? {
      return divisionRepository.delete(id, company)?.let { DivisionDTO(it) }
   }
}
