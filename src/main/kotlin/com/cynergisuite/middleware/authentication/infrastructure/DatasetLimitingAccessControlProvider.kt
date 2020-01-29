package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.domain.infrastructure.DatasetRepository
import com.cynergisuite.middleware.authentication.AuthenticatedUser
import io.micronaut.core.type.Argument

abstract class DatasetLimitingAccessControlProvider(
   private val datasetRepository: DatasetRepository
): AccessControlProvider {

   override final fun canUserAccess(user: AuthenticatedUser, asset: String, arguments: Array<Argument<Any>>): Boolean {
      return when {
         asset.endsWith("fetchOne") -> processFetchOne(user, arguments.find { it.name == "id" }.let { it as Long })
         asset.endsWith("fetchAll") -> true
         else -> super.canUserAccess(user, asset, arguments)
      }
   }
   protected final fun processFetchOne(user: AuthenticatedUser, id: Long): Boolean {
      val dataset = datasetRepository.findDataset(id)

      return if (dataset != null) {
         dataset == user.myDataset()
      } else {
         true
      }
   }
}
