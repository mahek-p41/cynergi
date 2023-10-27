package com.cynergisuite.middleware.area.infrastructure

import com.cynergisuite.middleware.area.AreaTypeEntity
import com.cynergisuite.middleware.area.MenuTypeEntity
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.repository.CrudRepository

@JdbcRepository
interface MenuRepository : CrudRepository<MenuTypeEntity, Int> {
   fun findByAreaTypeInList(areaTypes: List<AreaTypeEntity>): List<MenuTypeEntity>
   fun findByParent(parent: MenuTypeEntity): List<MenuTypeEntity>
}
