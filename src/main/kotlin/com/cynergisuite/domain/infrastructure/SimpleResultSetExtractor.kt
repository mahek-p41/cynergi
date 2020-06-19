package com.cynergisuite.domain.infrastructure

import com.cynergisuite.domain.Identifiable
import org.springframework.jdbc.core.ResultSetExtractor
import java.sql.ResultSet

class SimpleResultSetExtractor<ENTITY : Identifiable>(
   private val mapper: (rs: ResultSet, elements: MutableList<ENTITY>) -> Unit
) : ResultSetExtractor<List<ENTITY>> {

   override fun extractData(rs: ResultSet): List<ENTITY> {
      val elements = mutableListOf<ENTITY>()

      if (rs.next()) {
         mapper(rs, elements)
      }

      return elements
   }
}
