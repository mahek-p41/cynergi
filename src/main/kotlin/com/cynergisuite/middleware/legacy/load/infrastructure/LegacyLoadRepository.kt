package com.cynergisuite.middleware.legacy.load.infrastructure

import com.cynergisuite.middleware.legacy.load.LegacyLoad
import io.micronaut.spring.tx.annotation.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.math.BigInteger
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LegacyLoadRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(LegacyLoadRepository::class.java)

   fun exists(hash: BigInteger): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT hash FROM legacy_importation WHERE hash = :hash)", mapOf("hash" to hash), Boolean::class.java)!!

      logger.trace("Checking if LegacyImportation: {} exists resulted in {}", hash, exists)

      return exists
   }

   fun exists(filename: Path): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT filename FROM legacy_importation WHERE filename = :filename)", mapOf("filename" to filename.toRealPath(NOFOLLOW_LINKS).toString()), Boolean::class.java)!!

      logger.trace("Checking if LegacyImportation: {} exists resulted in {}", filename, exists)

      return exists
   }

   @Transactional
   fun insert(entity: LegacyLoad) {
      logger.debug("Inserting legacy_importation {}", entity)

      jdbc.update("""
         INSERT INTO legacy_importation(filename, hash)
         VALUES (
            :filename,
            :hash
         )
         """.trimIndent(),
         mapOf<String, Any>(
            "filename" to entity.filename.toRealPath(NOFOLLOW_LINKS).toString(),
            "hash" to entity.hash
         )
      )
   }
}
