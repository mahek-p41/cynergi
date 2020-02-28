package com.cynergisuite.middleware.store.infrastructure

import com.cynergisuite.middleware.authentication.infrastructure.DatasetLimitingAccessControlProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreAccessControlProvider @Inject constructor(
   storeRepository: StoreRepository
) : DatasetLimitingAccessControlProvider(storeRepository)
