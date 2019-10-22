package com.cynergisuite.middleware.schedule.type.infrastructure

import com.cynergisuite.middleware.schedule.type.ScheduleTypeEntity
import java.sql.ResultSet
import javax.inject.Singleton

@Singleton
class ScheduleTypeRepository {
   fun mapRow(rs: ResultSet, columnPrefix: String = "std_"): ScheduleTypeEntity =
      ScheduleTypeEntity(
         id = rs.getLong("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
