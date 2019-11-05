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
import com.lowagie.text.FontFactory
import com.lowagie.text.PageSize
import com.lowagie.text.Phrase
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import io.micronaut.validation.Validated
import org.apache.commons.lang3.StringUtils.EMPTY
import java.awt.Color
import java.awt.Font
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
               document.add(buildExceptionReport(updated, document.pageSize.width))
            }
         }
      }

      return AuditValueObject(updated, locale, localizationService)
   }

   private fun buildExceptionReport(audit: Audit, pageWidth: Float): PdfPTable {
      val table = PdfPTable(10)
      val evenColor = Color(204, 204, 204)
      val oddColor = Color.WHITE
      val headerFont = FontFactory.getFont(FontFactory.COURIER, 10F, Font.BOLD)
      val rowFont = FontFactory.getFont(FontFactory.COURIER, 10F, Font.PLAIN)

      table.totalWidth = pageWidth - 10
      table.isLockedWidth = true
      table.headerRows = 1
      table.defaultCell.border = 0

      table.addCell(Phrase("Scan Area", headerFont))
      table.addCell(Phrase("Product Code", headerFont))
      table.addCell(Phrase("Brand", headerFont))
      table.addCell(Phrase("Model #", headerFont))
      table.addCell(Phrase("Bar Code", headerFont))
      table.addCell(Phrase("Alt ID", headerFont))
      table.addCell(Phrase("Serial #", headerFont))
      table.addCell(Phrase("Employee", headerFont))
      table.addCell(Phrase("Scanned", headerFont))
      table.addCell(Phrase("Exception", headerFont))

      auditExceptionRepository.forEach(audit) { exception: AuditExceptionEntity, even: Boolean ->
         table.defaultCell.backgroundColor = if (even) evenColor else oddColor

         table.addCell(Phrase(exception.scanArea?.myDescription() ?: EMPTY, rowFont))
         table.addCell(Phrase(exception.productCode ?: EMPTY, rowFont))
         table.addCell(Phrase(exception.inventoryBrand ?: EMPTY, rowFont))
         table.addCell(Phrase(exception.inventoryModel ?: EMPTY, rowFont))
         table.addCell(Phrase(exception.barcode, rowFont))
         table.addCell(Phrase(exception.altId ?: EMPTY, rowFont))
         table.addCell(Phrase(exception.serialNumber ?: EMPTY, rowFont))
         table.addCell(Phrase(exception.scannedBy.displayName(), rowFont))
         table.addCell(Phrase(exception.timeCreated.format(REPORT_CREATED_TIME_FORMAT), rowFont))
         table.addCell(Phrase(exception.exceptionCode, rowFont))
      }

      return table
   }
}
