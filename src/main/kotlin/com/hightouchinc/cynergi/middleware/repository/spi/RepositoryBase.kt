package com.hightouchinc.cynergi.middleware.repository.spi

import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.repository.Repository
import io.micronaut.spring.tx.annotation.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

abstract class RepositoryBase<ENTITY> (
   protected val jdbc: NamedParameterJdbcTemplate,
   private val entityRowMapper: RowMapper<ENTITY>,
   private val fetchOneQuery: String,
   private val saveOneQuery: String
): Repository<ENTITY> {
   private companion object {
      val logger: Logger = LoggerFactory.getLogger(RepositoryBase::class.java)
   }

   protected abstract fun mapOfSaveParameters(entity: ENTITY): Map<String, Any?>

   override fun fetchOne(id: Long): ENTITY? {
      val fetched: ENTITY? = jdbc.findFirstOrNull(fetchOneQuery, mapOf("id" to id), entityRowMapper)

      logger.trace("fetched {} resulted in {}", id, fetched)

      return fetched
   }

   @Transactional
   override fun save(entity: ENTITY): ENTITY {
      return jdbc.queryForObject(saveOneQuery, mapOfSaveParameters(entity), entityRowMapper)!!
   }
}
