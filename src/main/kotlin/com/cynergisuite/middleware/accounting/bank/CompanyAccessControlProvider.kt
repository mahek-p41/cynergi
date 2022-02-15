package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.middleware.authentication.AccessException
import com.cynergisuite.middleware.authentication.infrastructure.AccessControlProvider
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.localization.AccessDenied
import io.micronaut.core.type.MutableArgumentValue
import io.micronaut.http.annotation.QueryValue
import java.util.UUID
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class CompanyAccessControlProvider @Inject constructor(
   private val companyRepository: CompanyRepository
) : AccessControlProvider {

   override fun canUserAccess(user: User, asset: String, parameters: MutableMap<String, MutableArgumentValue<*>>): Boolean {
      return when {
         asset.endsWith("fetchOne") -> processFetchOne(user, parameters)
         else -> super.canUserAccess(user, asset, parameters) // return the default value
      }
   }

   override fun generateException(user: User, asset: String?, parameters: MutableMap<String, MutableArgumentValue<*>>): Exception {
      return AccessException(AccessDenied(), user.myEmployeeNumber().toString())
   }

   private fun processFetchOne(user: User, parameters: MutableMap<String, MutableArgumentValue<*>>): Boolean {
      val authenticatedUserClientId = user.myCompany().clientId
      val id = parameters
         .filter { it.value.isAnnotationPresent(QueryValue::class.java) }
         .filter { it.value.name == "id" }
         .filter { it.value.type == UUID::class.java }
         .map { it.value.value as UUID }
         .first()
      val company = companyRepository.findOne(id)

      return authenticatedUserClientId == company?.clientId
   }
}
