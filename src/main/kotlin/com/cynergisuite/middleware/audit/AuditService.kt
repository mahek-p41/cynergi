package com.cynergisuite.middleware.audit

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.makeCell
import com.cynergisuite.middleware.audit.exception.AuditExceptionEntity
import com.cynergisuite.middleware.audit.exception.infrastructure.AuditExceptionRepository
import com.cynergisuite.middleware.audit.infrastructure.AuditPageRequest
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.audit.status.COMPLETED
import com.cynergisuite.middleware.audit.status.IN_PROGRESS
import com.cynergisuite.middleware.audit.status.SIGNED_OFF
import com.cynergisuite.middleware.authentication.User
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.reportal.ReportalService
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.StoreValueObject
import com.lowagie.text.Document
import com.lowagie.text.Element
import com.lowagie.text.Font
import com.lowagie.text.FontFactory
import com.lowagie.text.PageSize
import com.lowagie.text.Phrase
import com.lowagie.text.Rectangle
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfPageEventHelper
import com.lowagie.text.pdf.PdfWriter
import io.micronaut.validation.Validated
import org.apache.commons.lang3.StringUtils.EMPTY
import java.awt.Color
import java.time.LocalDate
import java.time.LocalDateTime
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
   private val companyRepository: CompanyRepository,
   private val localizationService: LocalizationService,
   private val reportalService: ReportalService
) {

   fun fetchById(id: Long, dataset: String, locale: Locale): AuditValueObject? =
      auditRepository.findOne(id, dataset)?.let { AuditValueObject(it, locale, localizationService) }

   @Validated
   fun fetchAll(@Valid pageRequest: AuditPageRequest, dataset: String, locale: Locale): Page<AuditValueObject> {
      val validaPageRequest = auditValidator.validationFetchAll(pageRequest, dataset)
      val found: RepositoryPage<AuditEntity, AuditPageRequest> = auditRepository.findAll(validaPageRequest, dataset)

      return found.toPage {
         AuditValueObject(it, locale, localizationService)
      }
   }

   fun exists(id: Long): Boolean =
      auditRepository.exists(id = id)

   fun findAuditStatusCounts(@Valid pageRequest: AuditPageRequest, dataset: String, locale: Locale): List<AuditStatusCountDataTransferObject> {
      val validPageRequest = auditValidator.validateFindAuditStatusCounts(pageRequest, dataset)

      return auditRepository
         .findAuditStatusCounts(validPageRequest, dataset)
         .map { auditStatusCount ->
            AuditStatusCountDataTransferObject(auditStatusCount, locale, localizationService)
         }
   }

   @Validated
   fun create(@Valid vo: AuditCreateValueObject, employee: User, locale: Locale): AuditValueObject {
      val validAudit = auditValidator.validateCreate(vo, employee)
      val audit = auditRepository.insert(validAudit)

      return AuditValueObject(audit, locale, localizationService)
   }

   fun findOrCreate(store: StoreEntity, employee: EmployeeEntity, locale: Locale): AuditValueObject {
      val createdOrInProgressAudit = auditRepository.findOneCreatedOrInProgress(store)

      return if (createdOrInProgressAudit != null) {
         AuditValueObject(createdOrInProgressAudit, locale, localizationService)
      } else {
         create(AuditCreateValueObject(StoreValueObject(store)), employee, locale)
      }
   }

   @Validated
   fun update(@Valid audit: AuditUpdateValueObject, user: User, locale: Locale): AuditValueObject {
      val (validAuditAction, existingAudit) = auditValidator.validateUpdate(audit, user, locale)

      existingAudit.actions.add(validAuditAction)

      val updated = auditRepository.update(existingAudit)

      if (updated.currentStatus() == SIGNED_OFF) {
         auditExceptionRepository.signOffAllExceptions(updated, user)
         reportalService.generateReportalDocument(updated.store, "IdleInventoryReport${updated.number}","pdf") { reportalOutputStream ->
            Document(PageSize.LEGAL.rotate(), 0.25F, 0.25F, 100F, 0.25F).use { document ->
               val writer = PdfWriter.getInstance(document, reportalOutputStream)

               writer.pageEvent = object : PdfPageEventHelper() { override fun onStartPage(writer: PdfWriter, document: Document) = buildHeader(updated, writer, document) }
               document.open()
               document.add(buildExceptionReport(updated, document.pageSize.width))
            }
         }
      }

      return AuditValueObject(updated, locale, localizationService)
   }

   private fun buildHeader(audit: AuditEntity, writer: PdfWriter, document: Document) {
      val headerFont = FontFactory.getFont(FontFactory.COURIER, 10F, Font.BOLD)
      val padding = 0f
      val leading = headerFont.getSize() * 1.2F
      val ascender = true
      val descender = true
      val currentDate = LocalDate.now()

      val companyName = companyRepository.findCompanyByStore(audit.store)!!.name

      val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

      val beginAction =  audit.actions.asSequence()
         .first { it.status == IN_PROGRESS}
      val beginDate = dateFormatter.format(beginAction.timeCreated)

      val endAction =  audit.actions.asSequence()
         .first { it.status == COMPLETED}
      val endDate = dateFormatter.format(endAction.timeCreated)
      val endEmployee = endAction.changedBy.getEmpName()

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
      header.totalWidth = document.pageSize.width - 10
      header.isLockedWidth = true
      header.headerRows = 0
      header.defaultCell.border = 0
      header.setWidthPercentage(100f)

      header.makeCell("DATE: ${currentDate}", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(companyName, Element.ALIGN_TOP, Element.ALIGN_CENTER, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell("PAGE ${document.pageNumber}", Element.ALIGN_TOP, Element.ALIGN_RIGHT, headerFont, leading, padding, headerBorder, ascender, descender)

      header.makeCell("TIME: ${timeFormatter.format(LocalDateTime.now())}", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell("IDLE INVENTORY AUDIT EXCEPTION REPORT", Element.ALIGN_TOP, Element.ALIGN_CENTER, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell("BCIDLERP", Element.ALIGN_TOP, Element.ALIGN_RIGHT, headerFont, leading, padding, headerBorder, ascender, descender)

      header.makeCell("Location: ${audit.printLocation()}", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell("(By Product)", Element.ALIGN_TOP, Element.ALIGN_CENTER, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell("(Final-Reprint)", Element.ALIGN_TOP, Element.ALIGN_RIGHT, headerFont, leading, padding, headerBorder, ascender, descender)

      header.makeCell("Started: ${beginDate}", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(EMPTY, Element.ALIGN_TOP, Element.ALIGN_CENTER, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(EMPTY, Element.ALIGN_TOP, Element.ALIGN_RIGHT, headerFont, leading, padding, headerBorder, ascender, descender)

      header.makeCell("Completed: ${endDate}", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(EMPTY, Element.ALIGN_TOP, Element.ALIGN_CENTER, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(EMPTY, Element.ALIGN_TOP, Element.ALIGN_RIGHT, headerFont, leading, padding, headerBorder, ascender, descender)

      header.makeCell("Employee: ${endEmployee}", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(EMPTY, Element.ALIGN_TOP, Element.ALIGN_CENTER, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(EMPTY, Element.ALIGN_TOP, Element.ALIGN_RIGHT, headerFont, leading, padding, headerBorder, ascender, descender)

      header.addCell(Phrase(EMPTY, headerFont))
      header.addCell(Phrase(EMPTY, headerFont))
      header.addCell(Phrase(EMPTY, headerFont))

      header.writeSelectedRows(0,-1,5f,document.pageSize.height-5,writer.directContent)
   }

   private fun buildExceptionReport(audit: AuditEntity, pageWidth: Float): PdfPTable {
      val headerFont = FontFactory.getFont(FontFactory.COURIER, 10F, Font.BOLD)
      val rowFont = FontFactory.getFont(FontFactory.COURIER, 10F, Font.NORMAL)

      val table = PdfPTable(8)
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

      val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

      val widthPercentage: Float = (pageWidth - 10) / 8
      val widths = floatArrayOf(widthPercentage, widthPercentage, widthPercentage, widthPercentage, widthPercentage, widthPercentage, widthPercentage, widthPercentage)
      widths[0] = 75f //Scan Area
      widths[1] = 115f //Model
      widths[2] = 75f //Bar Code
      widths[3] = 75f //Alt ID
      widths[4] = 75f //Serial#
      widths[5] = 135f //Employee
      widths[6] = 135f //Scanned
      widths[7] = 200f //Exception
      table.setWidths(widths)

      table.makeCell("Scan Area", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)
      table.makeCell("Model #", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)
      table.makeCell("Bar Code", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)
      table.makeCell("Alt ID", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)
      table.makeCell("Serial #", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)
      table.makeCell("Employee", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)
      table.makeCell("Scanned", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)
      table.makeCell("Exception", Element.ALIGN_TOP, Element.ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)

      auditExceptionRepository.forEach(audit) { exception: AuditExceptionEntity, even: Boolean ->
         table.defaultCell.backgroundColor = if (even) evenColor else oddColor

         table.addCell(Phrase(exception.scanArea?.myDescription() ?: EMPTY, rowFont))
         table.addCell(Phrase(exception.inventoryModel ?: EMPTY, rowFont))
         table.addCell(Phrase(exception.barcode, rowFont))
         table.addCell(Phrase(exception.altId ?: EMPTY, rowFont))
         table.addCell(Phrase(exception.serialNumber ?: EMPTY, rowFont))
         table.addCell(Phrase(exception.scannedBy.displayName(), rowFont))
         table.addCell(Phrase(dateTimeFormatter.format(exception.timeCreated), rowFont))
         table.addCell(Phrase(exception.exceptionCode, rowFont))
      }

      return table
   }
}
