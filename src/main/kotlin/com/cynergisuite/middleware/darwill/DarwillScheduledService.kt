package com.cynergisuite.middleware.darwill

import com.cynergisuite.middleware.area.AreaService
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.ScheduleProcessingException
import com.cynergisuite.middleware.ssh.SftpClientCredentials
import java.time.temporal.TemporalAccessor

abstract class DarwillScheduledService<in T : TemporalAccessor> (
   private val areaService: AreaService,
) {
   protected abstract fun shouldProcess(time: T): Boolean
   protected abstract fun process(company: CompanyEntity, sftpClientCredentials: SftpClientCredentials, time: T): DarwillJobResult

   @Throws(ScheduleProcessingException::class)
   fun shouldProcess(schedule: ScheduleEntity, time: T): Boolean {
      return areaService.isDarwillEnabledFor(schedule.company) && shouldProcess(time)
   }

   @Throws(ScheduleProcessingException::class)
   fun process(schedule: ScheduleEntity, time: T): DarwillJobResult {
      val sftpHost = schedule.arguments.first { it.description == "sftpHost" }.value
      val sftpPort = schedule.arguments.first { it.description == "sftpPort" }.value.toInt()
      val sftpUser = schedule.arguments.first { it.description == "sftpUsername" }.value
      val sftpPassword = schedule.arguments.first { it.description == "sftpPassword" }.value

      return process(
         schedule.company,
         SftpClientCredentials(
            username = sftpUser,
            password = sftpPassword,
            host = sftpHost,
            port = sftpPort,
         ),
         time
      )
   }
}
