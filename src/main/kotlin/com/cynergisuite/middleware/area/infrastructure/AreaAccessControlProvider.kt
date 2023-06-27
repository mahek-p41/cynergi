package com.cynergisuite.middleware.area.infrastructure

import com.cynergisuite.middleware.authentication.AccessException
import com.cynergisuite.middleware.authentication.infrastructure.AccessControlProvider
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.localization.AccessDenied
import io.micronaut.core.type.MutableArgumentValue
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AreaAccessControlProvider @Inject constructor(
   private val areaRepository: AreaRepository
) : AccessControlProvider {

   override fun canUserAccess(user: User, asset: String, parameters: MutableMap<String, MutableArgumentValue<*>>): Boolean {
      val areaPermission = areaRepository.findAllVisibleByCompany(user.myCompany())

      return user.isCynergiAdmin() || // if user is high touch admin then allow no questions asked
         areaPermission.any { it.areaType.value == asset } // if permissions have been setup and the company's area is contained in that permission then allow
   }

   override fun generateException(user: User, asset: String?, parameters: MutableMap<String, MutableArgumentValue<*>>): Exception {
      return AccessException(AccessDenied(), user.myEmployeeNumber().toString())
   }
}
