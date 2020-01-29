package com.cynergisuite.middleware.department.infrastructure

import com.cynergisuite.middleware.authentication.AuthenticatedUser
import com.cynergisuite.middleware.authentication.infrastructure.AccessControlProvider
import com.cynergisuite.middleware.authentication.infrastructure.DatasetLimitingAccessControlProvider
import io.micronaut.core.type.Argument
import javax.inject.Singleton

@Singleton
class DepartmentAccessControlProvider(
   private val departmentRepository: DepartmentRepository
): DatasetLimitingAccessControlProvider(departmentRepository)
