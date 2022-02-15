package com.cynergisuite.middleware.area.infrastructure

import com.cynergisuite.middleware.area.AreaTypeEntity
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.repository.CrudRepository

@JdbcRepository
interface AreaTypeRepository: CrudRepository<AreaTypeEntity, Int>
