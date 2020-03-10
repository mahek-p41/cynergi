package com.cynergisuite.middleware.department.infrastructure

import com.cynergisuite.middleware.authentication.infrastructure.DatasetLimitingAccessControlProvider
import javax.inject.Singleton

@Singleton
class DepartmentAccessControlProvider(
   departmentRepository: DepartmentRepository
): DatasetLimitingAccessControlProvider(departmentRepository)
