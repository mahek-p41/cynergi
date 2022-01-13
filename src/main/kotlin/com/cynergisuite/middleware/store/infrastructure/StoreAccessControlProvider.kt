package com.cynergisuite.middleware.store.infrastructure

import com.cynergisuite.middleware.authentication.infrastructure.DatasetLimitingAccessControlProvider
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class StoreAccessControlProvider @Inject constructor(
   storeRepository: StoreRepository
) : DatasetLimitingAccessControlProvider(storeRepository)
