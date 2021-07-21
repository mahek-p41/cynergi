package com.cynergisuite.middleware.shipping.shipvia

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.shipping.shipvia.infrastructure.ShipViaRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShipViaService @Inject constructor(
   private val shipViaRepository: ShipViaRepository,
   private val shipViaValidator: ShipViaValidator
) {

   fun fetchById(id: UUID, company: Company): ShipViaDTO? =
      shipViaRepository.findOne(id, company)?.let { ShipViaDTO(entity = it) }

   fun fetchAll(pageRequest: PageRequest, company: Company): Page<ShipViaDTO> {
      val found = shipViaRepository.findAll(pageRequest, company)

      return found.toPage { shipVia: ShipViaEntity ->
         ShipViaDTO(shipVia)
      }
   }

   fun create(dto: ShipViaDTO, company: Company): ShipViaDTO {
      val toCreate = shipViaValidator.validateCreate(dto, company)

      return ShipViaDTO(
         entity = shipViaRepository.insert(entity = toCreate)
      )
   }

   fun delete(id: UUID, company: Company) {
      shipViaRepository.delete(id, company)
   }

   fun update(dto: ShipViaDTO, company: Company): ShipViaDTO {
      val id = dto.id ?: throw NotFoundException(dto.id?.toString() ?: "") // FIXME need to better handle getting ID.  Should alter the UI to pass id as a path param
      val toUpdate = shipViaValidator.validateUpdate(id, dto, company)

      return ShipViaDTO(
         entity = shipViaRepository.update(entity = toUpdate)
      )
   }
}
