package com.hightouchinc.cynergi.middleware.data.access

import org.springframework.jdbc.core.JdbcTemplate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressDataAccessObject @Inject constructor(
   private val jdbcTemplate: JdbcTemplate
) {

}
