package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.middleware.area.infrastructure.AreaRepository
import com.cynergisuite.middleware.authentication.AccessException
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.localization.AccessDenied
import io.micronaut.core.type.MutableArgumentValue
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AreaControlProvider @Inject constructor(
   private val areaRepository: AreaRepository
) : AccessControlProvider {

     fun canUserAccess(user: User, asset: Array<String>, parameters: MutableMap<String, MutableArgumentValue<*>>): Boolean {
      val areaPermission = areaRepository.findAllVisibleByCompany(user.myCompany())

      return user.isCynergiAdmin() || // if user is high touch admin then allow no questions asked
         areaPermission.any {area ->  // if permissions have been setup and the company's area is contained in that permission then allow
            asset.contains(area.areaType.value)
         }
   }

   fun generateException(user: User, asset: Array<String>?, parameters: MutableMap<String, MutableArgumentValue<*>>): Exception {
      return AccessException(AccessDenied(), user.myEmployeeNumber().toString())
   }
}
