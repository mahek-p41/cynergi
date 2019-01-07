package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Checklist
import com.hightouchinc.cynergi.middleware.repository.spi.RepositoryBase
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Singleton

@Singleton
class ChecklistRepository(
   jdbc: NamedParameterJdbcTemplate
): RepositoryBase<Checklist>(
   tableName = "checklist",
   jdbc = jdbc,
   entityRowMapper = CHECKLIST_ROW_MAPPER,
   fetchOneQuery = ""
) {
   private companion object {
      val CHECKLIST_ROW_MAPPER: RowMapper<Checklist> = RowMapper { rs: ResultSet, _: Int ->
         Checklist(
            id = rs.getLong("id")
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
