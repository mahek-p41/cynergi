package com.cynergisuite.middleware.store.infrastructure

import com.cynergisuite.middleware.store.RegionToStoreEntity
import io.micronaut.spring.tx.annotation.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegionToStoreRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(RegionToStoreRepository::class.java)

   @Transactional
   fun insert(entity: RegionToStoreEntity): Int? {
      logger.debug("Inserting RegionToStore {}", entity)
      return jdbc.update(
         """
               INSERT INTO public.region_to_store(region_id, store_number)
               VALUES (:region_id, :store_number)
            """.trimIndent(),
         mapOf(
            "region_id" to entity.region.id,
            "store_number" to entity.store.number
         )
      )
   }

}
