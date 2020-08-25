package com.cynergisuite.middleware.address

import com.cynergisuite.extensions.deleteReturning
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getDoubleOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(AddressRepository::class.java)

   private fun selectBaseQuery(): String {
      return """
         SELECT
            address.id                                AS address_id,
            address.name                              AS address_name,
            address.address1                          AS address_address1,
            address.address2                          AS address_address2,
            address.city                              AS address_city,
            address.state                             AS address_state,
            address.postal_code                       AS address_postal_code,
            address.latitude                          AS address_latitude,
            address.longitude                         AS address_longitude,
            address.country                           AS address_country,
            address.county                            AS address_county,
            address.phone                             AS address_phone,
            address.fax                               AS address_fax
         FROM  address
      """
   }

   fun findOne(id: Long): AddressEntity? {
      val params = mutableMapOf<String, Any?>("id" to id)
      val query = "${selectBaseQuery()} WHERE address.id = :id"
      val found = jdbc.findFirstOrNull(
         query, params,
         RowMapper { rs, _ ->
            mapAddress(rs, "address_")
         }
      )

      logger.trace("Searching for AuditDetail: {} resulted in {}", id, found)

      return found
   }

   fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM address WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if AccountStatusCode: {} exists resulted in {}", id, exists)

      return exists
   }

   @Transactional
   fun insert(address: AddressEntity): AddressEntity {
      logger.debug("Inserting address {}", address)

      return jdbc.insertReturning(
         """
         INSERT INTO address(name, address1, address2, city, state, postal_code, latitude, longitude, country, county, phone, fax)
	      VALUES (:name, :address1, :address2, :city, :state, :postal_code, :latitude, :longitude, :country, :county, :phone, :fax)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "name" to address.name,
            "address1" to address.address1,
            "address2" to address.address2,
            "city" to address.city,
            "state" to address.state,
            "postal_code" to address.postalCode,
            "latitude" to address.latitude,
            "longitude" to address.longitude,
            "country" to address.country,
            "county" to address.county,
            "phone" to address.phone,
            "fax" to address.fax
         ),
         RowMapper { rs, _ ->
            mapAddress(rs)
         }
      )
   }

   @Transactional
   fun update(address: AddressEntity): AddressEntity {
      logger.debug("Updating address {}", address)

      return jdbc.updateReturning(
         """
         UPDATE address
         SET
            name=:name,
            address1=:address1,
            address2=:address2,
            city=:city,
            state=:state,
            postal_code=:postal_code,
            latitude=:latitude,
            longitude=:longitude,
            country=:country,
            county=:county,
            phone=:phone,
            fax=:fax
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to address.id,
            "name" to address.name,
            "address1" to address.address1,
            "address2" to address.address2,
            "city" to address.city,
            "state" to address.state,
            "postal_code" to address.postalCode,
            "latitude" to address.latitude,
            "longitude" to address.longitude,
            "country" to address.country,
            "county" to address.county,
            "phone" to address.phone,
            "fax" to address.fax
         ),
         RowMapper { rs, _ ->
            mapAddress(rs)
         }
      )
   }

   fun upsert(address: AddressEntity): AddressEntity =
      if (address.id != null) {
         update(address)
      } else {
         insert(address)
      }

   fun delete(id: Long): AddressEntity? {
      logger.debug("Deleting AuditPermission using {}", id)

      val existingAddress = findOne(id)

      return if (existingAddress != null) {
         jdbc.deleteReturning(
            """
            DELETE FROM address
            WHERE id = :id
            RETURNING
               *
            """,
            mapOf("id" to id),
            RowMapper { rs, _ ->
               AddressEntity(
                  id = rs.getLong("id"),
                  name = existingAddress.name,
                  address1 = existingAddress.address1,
                  address2 = existingAddress.address2,
                  city = existingAddress.city,
                  state = existingAddress.state,
                  postalCode = existingAddress.postalCode,
                  latitude = existingAddress.latitude,
                  longitude = existingAddress.longitude,
                  country = existingAddress.country,
                  county = existingAddress.county,
                  phone = existingAddress.phone,
                  fax = existingAddress.fax
               )
            }
         )
      } else {
         null
      }
   }

   fun mapAddress(rs: ResultSet, columnPrefix: String = EMPTY): AddressEntity =
      AddressEntity(
         id = rs.getLong("${columnPrefix}id"),
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
         AddressEntity(
            id = rs.getLong("${columnPrefix}id"),
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
      } else {
         null
      }
}
