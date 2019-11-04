package com.cynergisuite.middleware.audit

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.middleware.audit.exception.AuditExceptionEntity
import com.cynergisuite.middleware.audit.exception.infrastructure.AuditExceptionRepository
import com.cynergisuite.middleware.audit.infrastructure.AuditPageRequest
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.reportal.ReportalService
import com.lowagie.text.Document
import com.lowagie.text.PageSize
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import io.micronaut.validation.Validated
import org.apache.commons.lang3.StringUtils.EMPTY
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class AuditService @Inject constructor(
   private val auditRepository: AuditRepository,
   private val auditExceptionRepository: AuditExceptionRepository,
   private val auditValidator: AuditValidator,
   private val localizationService: LocalizationService,
   private val reportalService: ReportalService
) {
   private companion object {
      val REPORT_CREATED_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
   }

   fun fetchById(id: Long, locale: Locale): AuditValueObject? =
      auditRepository.findOne(id)?.let { AuditValueObject(it, locale, localizationService) }

   @Validated
   fun fetchAll(@Valid pageRequest: AuditPageRequest, locale: Locale): Page<AuditValueObject> {
      val validaPageRequest = auditValidator.validationFetchAll(pageRequest)
      val found: RepositoryPage<Audit> = auditRepository.findAll(validaPageRequest)

      return found.toPage {
         AuditValueObject(it, locale, localizationService)
      }
   }

   fun exists(id: Long): Boolean =
      auditRepository.exists(id = id)

   fun findAuditStatusCounts(@Valid pageRequest: AuditPageRequest, locale: Locale): List<AuditStatusCountDataTransferObject> {
      val validPageRequest = auditValidator.validateFindAuditStatusCounts(pageRequest)

      return auditRepository
         .findAuditStatusCounts(validPageRequest)
         .map { auditStatusCount ->
            AuditStatusCountDataTransferObject(auditStatusCount, locale, localizationService)
         }
   }

   @Validated
   fun create(@Valid vo: AuditCreateValueObject, @Valid employee: EmployeeValueObject, locale: Locale): AuditValueObject {
      val validAudit = auditValidator.validateCreate(vo, employee)

      val audit = auditRepository.insert(validAudit)

      return AuditValueObject(audit, locale, localizationService)
   }

   @Validated
   fun update(@Valid audit: AuditUpdateValueObject, @Valid employee: EmployeeValueObject, locale: Locale): AuditValueObject {
      val (validAuditAction, existingAudit) = auditValidator.validateUpdate(audit, employee, locale)

      existingAudit.actions.add(validAuditAction)

      val updated = auditRepository.update(existingAudit)

      if (updated.currentStatus().value == "SIGNED-OFF") {
         reportalService.generateReportalDocument { reportalOutputStream ->
            Document(PageSize.LEGAL.rotate(), 0.25F, 0.25F, 36F, 0.25F).use { document ->
               PdfWriter.getInstance(document, reportalOutputStream)

               document.open()
               document.add(buildExceptionReport(updated))
            }
         }
      }

      return AuditValueObject(updated, locale, localizationService)
   }

   private fun buildExceptionReport(audit: Audit): PdfPTable {
      val table = PdfPTable(10)

      table.totalWidth = 1000F
      table.headerRows = 1
      table.addCell("Scan Area")
      table.addCell("Product Code")
      table.addCell("Brand")
      table.addCell("Model #")
      table.addCell("Bar Code")
      table.addCell("Alt ID")
      table.addCell("Serial #")
      table.addCell("Employee")
      table.addCell("Scanned")
      table.addCell("Exception")

      auditExceptionRepository.forEach(audit) { exception: AuditExceptionEntity ->
         table.addCell(exception.scanArea?.myDescription() ?: EMPTY)
         table.addCell(exception.productCode ?: EMPTY)
         table.addCell(exception.inventoryBrand ?: EMPTY)
         table.addCell(exception.inventoryModel ?: EMPTY)
         table.addCell(exception.barcode)
         table.addCell(exception.altId ?: EMPTY)
         table.addCell(exception.serialNumber ?: EMPTY)
         table.addCell(exception.scannedBy.displayName())
         table.addCell(exception.timeCreated.format(REPORT_CREATED_TIME_FORMAT))
         table.addCell(exception.exceptionCode)
      }

      return table
   }
}
