package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Checklist
import com.hightouchinc.cynergi.middleware.entity.Company
import com.hightouchinc.cynergi.middleware.repository.spi.RepositoryBase
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet

class ChecklistRepository(
   jdbc: NamedParameterJdbcTemplate
): RepositoryBase<Checklist>(
   tableName = "checklist",
   jdbc = jdbc,
   entityRowMapper = CHECKLIST_ROW_MAPPER
) {
   private companion object {
      val CHECKLIST_ROW_MAPPER: RowMapper<Company> = RowMapper { rs: ResultSet, _: Int ->
         Company(
            id = rs.getLong("id"),
            name = rs.getString("name")
         )
      }
   }

   override fun save(entity: Checklist): Checklist {
      TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
   }

   override fun update(entity: Checklist): Checklist {
      TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
   }
}
