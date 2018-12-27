package com.hightouchinc.cynergi.middleware.data.access

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressDataAccessObject @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {

}
