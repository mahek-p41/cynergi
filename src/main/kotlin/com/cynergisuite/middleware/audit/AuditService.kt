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
import com.lowagie.text.Element
import com.lowagie.text.Font
import com.lowagie.text.FontFactory
import com.lowagie.text.PageSize
import com.lowagie.text.Paragraph
import com.lowagie.text.Phrase
import com.lowagie.text.Rectangle
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import io.micronaut.validation.Validated
import org.apache.commons.lang3.StringUtils.EMPTY
import java.awt.Color
import java.time.LocalDate
//import java.awt.Font
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
               document.add(buildHeader(updated, document.pageSize.width))
               document.add(buildExceptionReport(updated, document.pageSize.width))
            }
         }
      }

      return AuditValueObject(updated, locale, localizationService)
   }

   private fun buildHeader(audit: Audit, pageWidth: Float): PdfPTable {
      val headerFont = FontFactory.getFont(FontFactory.COURIER, 10F, Font.BOLD)
      val padding = 0f
      val leading = headerFont.getSize() * 1.2F
      val ascender = true
      val descender = true
      val currentDate = LocalDate.now()

      val headerBorder = Rectangle(0f, 0f)
      headerBorder.borderWidthLeft = 0f
      headerBorder.borderWidthBottom = 0f
      headerBorder.borderWidthRight = 0f
      headerBorder.borderWidthTop = 0f
      headerBorder.borderColorLeft = Color.BLACK
      headerBorder.borderColorBottom = Color.BLACK
      headerBorder.borderColorRight = Color.BLACK
      headerBorder.borderColorTop = Color.BLACK

      val header = PdfPTable(3)
      header.totalWidth = pageWidth - 10
      header.isLockedWidth = true
      header.headerRows = 0
      header.defaultCell.border = 0
      header.setWidthPercentage(100f)

      val headerString = "DATE: " + currentDate.toString()
      header.addCell(makeCell(headerString, Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, headerBorder, ascender, descender))
      header.addCell(makeCell("BOLIN RENTAL PURCHASE", Element.ALIGN_TOP, Element.ALIGN_CENTER, headerFont, leading, padding, headerBorder, ascender, descender))
      header.addCell(makeCell("PAGE", Element.ALIGN_TOP, Element.ALIGN_RIGHT, headerFont, leading, padding, headerBorder, ascender, descender))

      header.addCell(makeCell("TIME", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, headerBorder, ascender, descender))
      header.addCell(makeCell("IDLE INVENTORY AUDIT EXCEPTION REPORT", Element.ALIGN_TOP, Element.ALIGN_CENTER, headerFont, leading, padding, headerBorder, ascender, descender))
      header.addCell(makeCell("BCIDLERP", Element.ALIGN_TOP, Element.ALIGN_RIGHT, headerFont, leading, padding, headerBorder, ascender, descender))

      val locationString = "LOCATION: " + audit.store.toString()
      header.addCell(makeCell(locationString, Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, headerBorder, ascender, descender))
      header.addCell(makeCell("(By Product)", Element.ALIGN_TOP, Element.ALIGN_CENTER, headerFont, leading, padding, headerBorder, ascender, descender))
      header.addCell(makeCell("(Final-Reprint)", Element.ALIGN_TOP, Element.ALIGN_RIGHT, headerFont, leading, padding, headerBorder, ascender, descender))

      header.addCell(Phrase(EMPTY, headerFont))
      header.addCell(Phrase(EMPTY, headerFont))
      header.addCell(Phrase(EMPTY, headerFont))

      return header
   }

   private fun buildExceptionReport(audit: Audit, pageWidth: Float): PdfPTable {
      val headerFont = FontFactory.getFont(FontFactory.COURIER, 10F, Font.BOLD)
      val rowFont = FontFactory.getFont(FontFactory.COURIER, 10F, Font.NORMAL)

      val table = PdfPTable(10)
      val evenColor = Color(204, 204, 204)
      val oddColor = Color.WHITE
      val padding = 0f
      val leading = headerFont.getSize() * 1.2F
      val ascender = true
      val descender = true

      val border = Rectangle(0f, 0f)
      border.borderWidthLeft = 0f
      border.borderWidthBottom = 2f
      border.borderWidthRight = 0f
      border.borderWidthTop = 0f
      border.borderColorLeft = Color.BLACK
      border.borderColorBottom = Color.BLACK
      border.borderColorRight = Color.BLACK
      border.borderColorTop = Color.BLACK

      table.totalWidth = pageWidth - 10
      table.isLockedWidth = true
      table.headerRows = 1
      table.defaultCell.border = 0
      table.setWidthPercentage(100f)

      val widthPercentage: Float = (pageWidth - 10) / 10
      val widths = floatArrayOf(widthPercentage, widthPercentage, widthPercentage, widthPercentage, widthPercentage, widthPercentage, widthPercentage, widthPercentage, widthPercentage, widthPercentage)
      //widths[0] = widths[0] / 2
      //widths[9] = widths[9] * 2
      table.setWidths(widths)

      //table.addCell(Phrase("Scan Area", headerFont))
      table.addCell(makeCell("Scan Area", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender))
      table.addCell(makeCell("Product Code", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender))
      table.addCell(makeCell("Brand", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender))
      table.addCell(makeCell("Model #", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender))
      table.addCell(makeCell("Bar Code", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender))
      table.addCell(makeCell("Alt ID", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender))
      table.addCell(makeCell("Serial #", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender))
      table.addCell(makeCell("Employee", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender))
      table.addCell(makeCell("Scanned", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender))
      table.addCell(makeCell("Exception", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender))
      //table.addCell(Phrase("Exception", headerFont))

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

   private fun makeCell(text: String, vAlignment: Int, hAlignment: Int, font: Font, leading: Float, padding: Float, borders: Rectangle, ascender: Boolean, descender: Boolean): PdfPCell {
      val p = Paragraph(text, font)
      p.setLeading(leading)

      val cell = PdfPCell(p)
      cell.setLeading(leading, 0f)
      cell.setVerticalAlignment(vAlignment)
      cell.setHorizontalAlignment(hAlignment)
      cell.cloneNonPositionParameters(borders)
      cell.setUseAscender(ascender)
      cell.setUseDescender(descender)
      cell.setUseBorderPadding(true)
      cell.setPadding(padding)
      return cell
   }
}
