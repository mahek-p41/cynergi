package com.cynergisuite.middleware.address

import com.cynergisuite.extensions.getDoubleOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.softDelete
import com.cynergisuite.middleware.error.NotFoundException
import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.repository.CrudRepository
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.*
import javax.inject.Inject
import javax.transaction.Transactional

@JdbcRepository
abstract class AddressRepository @Inject constructor(
   private val jdbc: Jdbi
) : CrudRepository<AddressEntity, UUID> {
   private val logger: Logger = LoggerFactory.getLogger(AddressRepository::class.java)

   fun upsert(address: AddressEntity): AddressEntity =
      if (address.id != null) {
         update(address)
      } else {
         save(address)
      }

   @Query("SELECT * FROM address WHERE id = :id AND deleted = false")
   abstract override fun findById(id: UUID): Optional<AddressEntity>

   @Transactional
   override fun deleteById(id: UUID) {
      logger.debug("Deleting address with id={}", id)

      val rowsAffected = jdbc.softDelete(
         """
         UPDATE address
         SET deleted = TRUE
         WHERE id = :id
         """,
         mapOf("id" to id),
         "address"
      )

      logger.info("Row affected {}", rowsAffected)

      if (rowsAffected == 0) throw NotFoundException(id)
   }

   fun mapAddress(rs: ResultSet, columnPrefix: String = EMPTY): AddressEntity =
      AddressEntity(
         id = rs.getUuid("${columnPrefix}id"),
         name = rs.getString("${columnPrefix}name"),
         address1 = rs.getString("${columnPrefix}address1"),
         address2 = rs.getString("${columnPrefix}address2"),
         city = rs.getString("${columnPrefix}city"),
         state = rs.getString("${columnPrefix}state"),
         postalCode = rs.getString("${columnPrefix}postal_code"),
         latitude = rs.getDoubleOrNull("${columnPrefix}latitude"),
         longitude = rs.getDoubleOrNull("${columnPrefix}longitude"),
         country = rs.getString("${columnPrefix}country"),
         county = rs.getString("${columnPrefix}county"),
         phone = rs.getString("${columnPrefix}phone"),
         fax = rs.getString("${columnPrefix}fax")
      )

   fun mapAddressOrNull(rs: ResultSet, columnPrefix: String = EMPTY): AddressEntity? =
      if (rs.getString("${columnPrefix}id") != null) {
         mapAddress(rs, columnPrefix)
      } else {
         null
      }
}
