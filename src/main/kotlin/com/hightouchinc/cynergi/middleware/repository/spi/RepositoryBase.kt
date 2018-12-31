package com.hightouchinc.cynergi.middleware.repository.spi

import com.hightouchinc.cynergi.middleware.entity.IdentifieableEntity
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.ofPairs
import com.hightouchinc.cynergi.middleware.repository.Repository
import io.micronaut.spring.tx.annotation.Transactional
import org.eclipse.collections.api.map.MutableMap
import org.eclipse.collections.impl.factory.Maps
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

abstract class RepositoryBase<ENTITY: IdentifieableEntity> (
   protected val tableName: String,
   protected val jdbc: NamedParameterJdbcTemplate,
   private val entityRowMapper: RowMapper<ENTITY>,
   private val fetchOneQuery: String,
   private val saveOneQuery: String,
   private val updateOneQuery: String
): Repository<ENTITY> {
   private companion object {
      val logger: Logger = LoggerFactory.getLogger(RepositoryBase::class.java)
   }

   protected abstract fun mapOfSaveParameters(entity: ENTITY): MutableMap<String, Any?>

   override fun fetchOne(id: Long): ENTITY? {
      val fetched: ENTITY? = jdbc.findFirstOrNull(fetchOneQuery, Maps.mutable.ofPairs("id" to id), entityRowMapper)

      logger.trace("fetched {} resulted in {}", id, fetched)

      return fetched
   }

   override fun exists(id: Long): Boolean {
      return jdbc.queryForObject("SELECT EXISTS(SELECT id FROM $tableName WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!
   }

   @Transactional
   override fun save(entity: ENTITY): ENTITY {
      return jdbc.queryForObject(
         saveOneQuery,
         mapOfSaveParameters(entity = entity),
         entityRowMapper
      )!!
   }

   override fun update(entity: ENTITY): ENTITY {
      val params = mapOfSaveParameters(entity = entity)

      params.putIfAbsent("id", entity.entityId())

      return jdbc.queryForObject(
         updateOneQuery,
         params,
         entityRowMapper
      )!!
   }
}
