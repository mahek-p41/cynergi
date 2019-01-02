package com.hightouchinc.cynergi.middleware.repository.spi

import com.hightouchinc.cynergi.middleware.entity.IdentifiableEntity
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.ofPairs
import com.hightouchinc.cynergi.middleware.repository.Repository
import org.eclipse.collections.impl.factory.Maps
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

abstract class RepositoryBase<ENTITY: IdentifiableEntity> (
   protected val tableName: String,
   protected val jdbc: NamedParameterJdbcTemplate,
   protected val entityRowMapper: RowMapper<ENTITY>,
   private val fetchOneQuery: String
): Repository<ENTITY> {
   private companion object {
      val logger: Logger = LoggerFactory.getLogger(RepositoryBase::class.java)
   }

   override fun fetchOne(id: Long): ENTITY? {
      val fetched: ENTITY? = jdbc.findFirstOrNull(fetchOneQuery, Maps.mutable.ofPairs("id" to id), entityRowMapper)

      logger.trace("fetched {} resulted in {}", id, fetched)

      return fetched
   }

   override fun exists(id: Long): Boolean =
      jdbc.queryForObject("SELECT EXISTS(SELECT id FROM $tableName WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!
}
