package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.<%= entityname %>
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.ofPairs
import org.apache.commons.lang3.StringUtils.EMPTY
import org.eclipse.collections.impl.factory.Maps
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class <%= entityname %>Repository(
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<<%= entityname %>> {
   private companion object {
      val logger: Logger = LoggerFactory.getLogger(<%= entityname %>::class.java)
   }
}
