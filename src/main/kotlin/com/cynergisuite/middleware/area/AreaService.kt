package com.cynergisuite.middleware.area

import com.cynergisuite.middleware.area.infrastructure.AreaRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.localization.LocalizationService
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AreaService @Inject constructor(
   private val repository: AreaRepository,
   private val localizationService: LocalizationService,
   private val validator: AreaValidator
) {

   fun fetchAll(company: Company, locale: Locale): List<AreaTypeDTO> =
      repository.findAll(company).map { transformEntity(it, locale) }

   fun enableArea(company: Company, areaTypeId: Long) {
      validator.validateAreaId(company, areaTypeId)
      repository.enable(company, areaTypeId)
   }

   fun disableArea(company: Company, areaTypeId: Long) {
      validator.validateAreaId(company, areaTypeId)
      repository.disable(company, areaTypeId)
   }

   private fun transformEntity(areaType: AreaType, locale: Locale): AreaTypeDTO =
      AreaTypeDTO(
         type = areaType,
         localizedDescription = areaType.localizeMyDescription(locale, localizationService),
         menus = areaType.menus
            .map { menuType ->
               val moduleTypes = menuType.modules
                  .map {
                     ModuleTypeDTO(
                        type = it,
                        localizedDescription = it.localizeMyDescription(locale, localizationService))
                  }

               MenuTypeDTO(
                  type = menuType,
                  localizedDescription = menuType.localizeMyDescription(locale, localizationService),
                  modules = moduleTypes)
            }
      )

}
