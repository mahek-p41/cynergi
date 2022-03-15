package com.cynergisuite.middleware.darwill.schedule.spi

import com.cynergisuite.middleware.area.AreaService
import com.cynergisuite.middleware.area.DarwillUpload
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.darwill.schedule.DarwillJobResult
import com.cynergisuite.middleware.schedule.AreaEnabledJob
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.ScheduleProcessingException
import com.cynergisuite.middleware.ssh.SftpClientCredentials
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

abstract class DarwillScheduledJob(
   areaService: AreaService,
) : AreaEnabledJob(areaService, DarwillUpload) {
   protected abstract fun process(company: CompanyEntity, sftpClientCredentials: SftpClientCredentials, time: OffsetDateTime, fileDate: String): DarwillJobResult

   private val fileDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

   @Throws(ScheduleProcessingException::class)
   override fun process(schedule: ScheduleEntity, time: OffsetDateTime): DarwillJobResult {
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
         time,
         LocalDateTime.now().format(fileDateFormat)
      )
   }
}
