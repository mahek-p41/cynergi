package com.cynergisuite.middleware.schedule.command.infrastructure

import com.cynergisuite.middleware.schedule.command.ScheduleCommandTypeEntity
import java.sql.ResultSet
import javax.inject.Singleton

@Singleton
class ScheduleCommandTypeRepository {

   fun mapRow(rs: ResultSet, columnPrefix: String = "sctd_"): ScheduleCommandTypeEntity =
      ScheduleCommandTypeEntity(
         id = rs.getLong("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
