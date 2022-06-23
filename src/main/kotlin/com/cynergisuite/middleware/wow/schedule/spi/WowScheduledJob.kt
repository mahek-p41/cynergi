package com.cynergisuite.middleware.wow.schedule.spi

import com.cynergisuite.middleware.area.AreaService
import com.cynergisuite.middleware.area.WowUpload
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.wow.schedule.WowJobResult
import com.cynergisuite.middleware.schedule.AreaEnabledJob
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.ScheduleProcessingException
import com.cynergisuite.middleware.ssh.SftpClientCredentials
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

abstract class WowScheduledJob(
   areaService: AreaService,
) : AreaEnabledJob(areaService, WowUpload) {
   protected abstract fun process(company: CompanyEntity, sftpClientCredentials: SftpClientCredentials, time: OffsetDateTime, fileDate: String): WowJobResult

   private val fileDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

   @Throws(ScheduleProcessingException::class)
   override fun process(schedule: ScheduleEntity, time: OffsetDateTime): WowJobResult {
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
