package com.cynergisuite.domain.infrastructure

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.PageRequest
import org.springframework.jdbc.core.ResultSetExtractor
import java.sql.ResultSet

class PagedResultSetExtractor<IDENT : Identifiable, REQUESTED : PageRequest>(
   private val requested: REQUESTED,
   private val mapper: (rs: ResultSet, elements: MutableList<IDENT>) -> Unit
) : ResultSetExtractor<RepositoryPage<IDENT, REQUESTED>> {

   override fun extractData(rs: ResultSet): RepositoryPage<IDENT, REQUESTED> {
      val elements = mutableListOf<IDENT>()
      var totalElements: Long = 0

      if (rs.next()) {
         totalElements = rs.getString("total_elements")?.toLong() ?: 0
         mapper(rs, elements)
      }

      return RepositoryPage(
         requested = requested,
         elements = elements,
         totalElements = totalElements
      )
   }
}
