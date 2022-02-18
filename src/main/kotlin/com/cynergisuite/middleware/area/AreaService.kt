package com.cynergisuite.middleware.area

import com.cynergisuite.middleware.area.infrastructure.AreaRepository
import com.cynergisuite.middleware.area.infrastructure.MenuRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.localization.LocalizationService
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.Locale

@Singleton
class AreaService @Inject constructor(
   private val areaRepository: AreaRepository,
   private val localizationService: LocalizationService,
   private val menuRepository: MenuRepository,
   private val validator: AreaValidator
) {

   fun isDarwillEnabledFor(company: CompanyEntity): Boolean =
      areaRepository.existsByCompanyAndAreaType(company, DarwillUpload.toAreaTypeEntity())

   fun fetchAllVisibleWithMenusAndAreas(company: CompanyEntity, locale: Locale): List<AreaDTO> {
      val areas = areaRepository.findAllVisibleByCompany(company).map { transformEntity(it, locale) }

      return areas
   }

   fun enableArea(company: CompanyEntity, areaTypeId: Int) {
      validator.validateAreaTypeId(company, areaTypeId)
      areaRepository.enable(company, areaTypeId)
   }

   fun disableArea(company: CompanyEntity, areaType: AreaTypeEntity) {
      validator.validateAreaTypeId(company, areaType.id)
      areaRepository.deleteByCompanyAndAreaType(company, areaType)
   }

   private fun transformEntity(area: AreaEntity, locale: Locale): AreaDTO =
      AreaDTO(
         area = area,
         localizedDescription = area.areaType.localizeMyDescription(locale, localizationService),
         // menus = convertMenus(area.areaType.menus, locale)
      )
}
