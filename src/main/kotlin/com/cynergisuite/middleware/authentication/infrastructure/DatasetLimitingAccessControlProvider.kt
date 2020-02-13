package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.domain.infrastructure.DatasetRepository
import com.cynergisuite.middleware.authentication.user.User
import io.micronaut.core.type.MutableArgumentValue
import io.micronaut.http.annotation.QueryValue

abstract class DatasetLimitingAccessControlProvider(
   private val datasetRepository: DatasetRepository
): AccessControlProvider {

   override final fun canUserAccess(user: User, asset: String, parameters: MutableMap<String, MutableArgumentValue<*>>): Boolean {
      return when {
         asset.endsWith("fetchOne") -> processFetchOne(user, parameters)
         asset.endsWith("fetchAll") -> true // assumption here is that the user's dataset will be used when filtering the fetchAll query so just return true
         else -> super.canUserAccess(user, asset, parameters) // return the default value
      }
   }

   private fun processFetchOne(user: User, parameters: MutableMap<String, MutableArgumentValue<*>>): Boolean {
      val id = parameters
         .filter { it.value.isAnnotationPresent(QueryValue::class.java) }
         .filter { it.value.name == "id" }
         .filter { it.value.type == Long::class.java }
         .map { it.value.value as Long }
         .first()

      return datasetRepository.existsForCompany(id, user.myCompany())
   }
}
