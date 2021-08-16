package com.cynergisuite.middleware.area

import com.cynergisuite.middleware.area.infrastructure.AreaRepository
import com.cynergisuite.middleware.company.CompanyEntity
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

   fun fetchAll(company: CompanyEntity, locale: Locale): List<AreaDTO> =
      repository.findAll(company).map { transformEntity(it, locale) }

   fun enableArea(company: CompanyEntity, areaTypeId: Int) {
      validator.validateAreaTypeId(company, areaTypeId)
      repository.enable(company, areaTypeId)
   }

   fun disableArea(company: CompanyEntity, areaTypeId: Int) {
      validator.validateAreaTypeId(company, areaTypeId)
      repository.disable(company, areaTypeId)
   }

   private fun transformEntity(areaType: AreaType, locale: Locale): AreaDTO =
      AreaDTO(
         type = areaType,
         localizedDescription = areaType.localizeMyDescription(locale, localizationService),
         menus = convertMenus(areaType.menus, locale)
      )

   private fun convertMenus(menuTypes: List<MenuType>, locale: Locale): List<MenuDTO> {
      return menuTypes.map { menuType ->
         val subMenus = if (menuType.menus.isNotEmpty()) convertMenus(menuType.menus, locale) else null

         val moduleTypes = menuType.modules
            .map {
               ModuleDTO(
                  type = it,
                  localizedDescription = it.localizeMyDescription(locale, localizationService)
               )
            }

         MenuDTO(
            type = menuType,
            localizedDescription = menuType.localizeMyDescription(locale, localizationService),
            menus = subMenus,
            modules = moduleTypes
         )
      }
   }
}
