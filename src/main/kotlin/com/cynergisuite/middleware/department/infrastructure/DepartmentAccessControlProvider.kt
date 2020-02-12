package com.cynergisuite.middleware.department.infrastructure

import com.cynergisuite.middleware.authentication.infrastructure.DatasetLimitingAccessControlProvider
import javax.inject.Singleton

@Singleton
class DepartmentAccessControlProvider(
   private val departmentRepository: DepartmentRepository
): DatasetLimitingAccessControlProvider(departmentRepository)
