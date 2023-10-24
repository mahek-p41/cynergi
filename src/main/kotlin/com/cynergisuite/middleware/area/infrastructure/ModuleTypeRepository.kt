package com.cynergisuite.middleware.area.infrastructure

import com.cynergisuite.middleware.area.ModuleTypeEntity
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.repository.CrudRepository

@JdbcRepository
interface ModuleTypeRepository : CrudRepository<ModuleTypeEntity, Int>
