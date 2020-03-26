package com.cynergisuite.middleware.shipvia

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.reportal.ReportalService
import com.cynergisuite.middleware.shipvia.infrastructure.ShipViaRepository
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class ShipViaService @Inject constructor(
   private val shipViaRepository: ShipViaRepository,
   private val shipViaValidator: ShipViaValidator,
   private val companyRepository: CompanyRepository,
   private val localizationService: LocalizationService,
   private val reportalService: ReportalService
) {

   fun fetchById(id: Long): ShipViaValueObject? =
      shipViaRepository.findOne(id)?.let{ShipViaValueObject(entity = it)}

   @Validated
   fun fetchAll(@Valid pageRequest: PageRequest, company: Company): Page<ShipViaValueObject> {
      val found = shipViaRepository.findAll(pageRequest, company)

      return found.toPage { shipVia: ShipViaEntity ->
         ShipViaValueObject(shipVia)
      }
   }

   fun exists(id: Long): Boolean =
      shipViaRepository.exists(id = id)

   @Validated
   fun create(@Valid vo: ShipViaValueObject, employee: User): ShipViaValueObject {
      shipViaValidator.validateCreate(vo)

      return ShipViaValueObject(
         entity = shipViaRepository.insert(entity = ShipViaEntity(vo, employee.myCompany()))
      )
   }

   @Validated
   fun update(@Valid vo: ShipViaValueObject, employee: User): ShipViaValueObject {
      shipViaValidator.validateUpdate(vo)

      return ShipViaValueObject(
         entity = shipViaRepository.update(entity = ShipViaEntity(vo, employee.myCompany()))
      )
   }
}
