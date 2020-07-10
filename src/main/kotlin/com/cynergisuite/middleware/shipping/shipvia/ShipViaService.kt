package com.cynergisuite.middleware.shipping.shipvia

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.shipping.shipvia.infrastructure.ShipViaRepository
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class ShipViaService @Inject constructor(
   private val shipViaRepository: ShipViaRepository,
   private val shipViaValidator: ShipViaValidator
) {

   fun fetchById(id: Long, company: Company): ShipViaValueObject? =
      shipViaRepository.findOne(id, company)?.let{ ShipViaValueObject(entity = it) }

   @Validated
   fun fetchAll(@Valid pageRequest: PageRequest, company: Company): Page<ShipViaValueObject> {
      val found = shipViaRepository.findAll(pageRequest, company)

      return found.toPage { shipVia: ShipViaEntity ->
         ShipViaValueObject(shipVia)
      }
   }

   @Validated
   fun create(@Valid vo: ShipViaValueObject, company: Company): ShipViaValueObject {
      val toCreate = shipViaValidator.validateCreate(vo, company)

      return ShipViaValueObject(
         entity = shipViaRepository.insert(entity = toCreate)
      )
   }

   @Validated
   fun update(@Valid vo: ShipViaValueObject, company: Company): ShipViaValueObject {
      val id = vo.id ?: throw NotFoundException(vo.id?.toString() ?: "") // FIXME need to better handle getting ID.  Should alter the UI to pass id as a path param
      val toUpdate = shipViaValidator.validateUpdate(id, vo, company)

      return ShipViaValueObject(
         entity = shipViaRepository.update(entity = toUpdate)
      )
   }
}
