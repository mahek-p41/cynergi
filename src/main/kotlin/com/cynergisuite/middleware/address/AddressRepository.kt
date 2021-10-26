package com.cynergisuite.middleware.address

import com.cynergisuite.extensions.getDoubleOrNull
import com.cynergisuite.extensions.getUuid
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.repository.CrudRepository
import org.apache.commons.lang3.StringUtils.EMPTY
import java.sql.ResultSet
import java.util.UUID

@JdbcRepository
abstract class AddressRepository : CrudRepository<AddressEntity, UUID> {

   fun upsert(address: AddressEntity): AddressEntity =
      if (address.id != null) {
         update(address)
      } else {
         save(address)
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
