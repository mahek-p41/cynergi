package com.cynergisuite.domain.infrastructure

import com.cynergisuite.domain.IdentifiableEntity
import org.springframework.jdbc.core.ResultSetExtractor
import java.sql.ResultSet

class PagedResultSetExtractor<T : IdentifiableEntity>(
   private val mapper: (rs: ResultSet, elements: MutableList<T>) -> Unit
) : ResultSetExtractor<RepositoryPage<T>> {

   override fun extractData(rs: ResultSet): RepositoryPage<T> {
      val elements = mutableListOf<T>()
      var totalElements: Long = 0

      if (rs.next()) {
         totalElements = rs.getString("total_elements")?.toLong() ?: 0
         mapper(rs, elements)
      }

      return RepositoryPage(
         elements = elements,
         totalElements = totalElements
      )
   }
}
