package com.cynergisuite.middleware.audit

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.makeCell
import com.cynergisuite.middleware.audit.action.AuditActionEntity
import com.cynergisuite.middleware.audit.exception.AuditExceptionEntity
import com.cynergisuite.middleware.audit.exception.infrastructure.AuditExceptionRepository
import com.cynergisuite.middleware.audit.infrastructure.AuditPageRequest
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.audit.status.APPROVED
import com.cynergisuite.middleware.audit.status.COMPLETED
import com.cynergisuite.middleware.audit.status.CREATED
import com.cynergisuite.middleware.audit.status.IN_PROGRESS
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.inventory.infrastructure.InventoryRepository
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.reportal.ReportalService
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreDTO
import com.cynergisuite.middleware.store.StoreEntity
import com.lowagie.text.Document
import com.lowagie.text.Element
import com.lowagie.text.Element.ALIGN_CENTER
import com.lowagie.text.Element.ALIGN_LEFT
import com.lowagie.text.Element.ALIGN_RIGHT
import com.lowagie.text.Element.ALIGN_TOP
import com.lowagie.text.Font
import com.lowagie.text.Font.BOLD
import com.lowagie.text.FontFactory
import com.lowagie.text.FontFactory.COURIER
import com.lowagie.text.PageSize
import com.lowagie.text.Phrase
import com.lowagie.text.Rectangle
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfPageEventHelper
import com.lowagie.text.pdf.PdfWriter
import io.micronaut.validation.Validated
import org.apache.commons.lang3.StringUtils.EMPTY
import java.awt.Color
import java.awt.Color.BLACK
import java.awt.Color.WHITE
import java.io.OutputStream
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
   private val inventoryRepository: InventoryRepository,
   private val auditValidator: AuditValidator,
   private val companyRepository: CompanyRepository,
   private val employeeRepository: EmployeeRepository,
   private val localizationService: LocalizationService,
   private val reportalService: ReportalService
) {

   fun fetchById(id: Long, company: Company, locale: Locale): AuditValueObject? =
      auditRepository.findOne(id, company)?.let { AuditValueObject(it, locale, localizationService) }

   @Validated
   fun fetchAll(@Valid pageRequest: AuditPageRequest, user: User, locale: Locale): Page<AuditValueObject> {
      val validaPageRequest = auditValidator.validationFetchAll(pageRequest, user.myCompany())
      val found: RepositoryPage<AuditEntity, AuditPageRequest> = auditRepository.findAll(validaPageRequest, user)

      return found.toPage {
         AuditValueObject(it, locale, localizationService)
      }
   }

   fun fetchAuditExceptionReport(id: Long, company: Company, os: OutputStream) {
      val audit = auditRepository.findOne(id, company) ?: throw NotFoundException("Unable to find Audit $id")

      generateAuditExceptionReport(os, audit, true)
   }

   fun fetchUnscannedIdleInventoryReport(id: Long, company: Company, os: OutputStream) {
      val audit = auditRepository.findOne(id, company) ?: throw NotFoundException("Unable to find Audit $id")

      generateUnscannedIdleInventoryReport(os, audit, true)
   }

   fun exists(id: Long): Boolean =
      auditRepository.exists(id = id)

   fun findAuditStatusCounts(@Valid pageRequest: AuditPageRequest, user: User, locale: Locale): List<AuditStatusCountDataTransferObject> {
      val validPageRequest = auditValidator.validateFindAuditStatusCounts(pageRequest, user.myCompany())

      return auditRepository
         .findAuditStatusCounts(validPageRequest, user)
         .map { auditStatusCount ->
            AuditStatusCountDataTransferObject(auditStatusCount, locale, localizationService)
         }
   }

   @Validated
   fun create(@Valid vo: AuditCreateValueObject, user: User, locale: Locale): AuditValueObject {
      val validAudit = auditValidator.validateCreate(vo, user)
      val audit = auditRepository.insert(validAudit)

      return AuditValueObject(audit, locale, localizationService)
   }

   fun findOrCreate(store: StoreEntity, user: User, locale: Locale): AuditValueObject {
      val createdOrInProgressAudit = auditRepository.findOneCreatedOrInProgress(store)

      return if (createdOrInProgressAudit != null) {
         AuditValueObject(createdOrInProgressAudit, locale, localizationService)
      } else {
         create(AuditCreateValueObject(StoreDTO(store)), user, locale)
      }
   }

   fun findOneCreatedOrInProgress(store: Store, user: User, locale: Locale): AuditValueObject? {
      return auditRepository.findOneCreatedOrInProgress(store)?.let { AuditValueObject(it, locale, localizationService) }
   }

   @Validated
   fun completeOrCancel(@Valid audit: AuditUpdateValueObject, user: User, locale: Locale): AuditValueObject {
      val (validAuditAction, existingAudit) = auditValidator.validateCompleteOrCancel(audit, user, locale)

      existingAudit.actions.add(validAuditAction)

      val updated = auditRepository.update(existingAudit)

      return AuditValueObject(updated, locale, localizationService)
   }

   @Validated
   fun approve(@Valid audit: SimpleIdentifiableDTO, user: User, locale: Locale): AuditValueObject {
      val existing = auditValidator.validateApproved(audit, user.myCompany(), user, locale)
      val actions = existing.actions.toMutableSet()
      val changedBy = employeeRepository.findOne(user) ?: throw NotFoundException(user)

      actions.add(AuditActionEntity(status = APPROVED, changedBy = changedBy))

      val updated = auditRepository.update(existing.copy(actions = actions))

      if (updated.currentStatus() == APPROVED) {
         auditExceptionRepository.approveAllExceptions(updated, user)

         reportalService.generateReportalDocument(updated.store, "IdleInventoryReport${updated.number}", "pdf") { reportalOutputStream ->
            generateAuditExceptionReport(reportalOutputStream, updated, false)
         }
      }

      return AuditValueObject(updated, locale, localizationService)
   }

   private fun generateAuditExceptionReport(outputStream: OutputStream, audit: AuditEntity, onDemand: Boolean) {
      Document(PageSize.LEGAL.rotate(), 0.25F, 0.25F, 100F, 0.25F).use { document ->
         val writer = PdfWriter.getInstance(document, outputStream)

         writer.pageEvent = object : PdfPageEventHelper() {
            override fun onStartPage(writer: PdfWriter, document: Document) = buildExceptionReportHeader(audit, onDemand, writer, document)
         }
         document.open()
         document.add(buildExceptionReport(audit, document.pageSize.width))
      }
   }

   private fun generateUnscannedIdleInventoryReport(outputStream: OutputStream, audit: AuditEntity, onDemand: Boolean) {
      Document(PageSize.LEGAL.rotate(), 0.25F, 0.25F, 100F, 0.25F).use { document ->
         val writer = PdfWriter.getInstance(document, outputStream)

         writer.pageEvent = object : PdfPageEventHelper() {
            override fun onStartPage(writer: PdfWriter, document: Document) = buildUnscannedIdleInventoryReportHeader(audit, onDemand, writer, document)
         }
         document.open()
         document.add(buildUnscannedIdleInventoryReport(audit, document.pageSize.width))
      }
   }

   @Validated
   fun approveAllExceptions(@Valid audit: SimpleIdentifiableDTO, user: User): AuditApproveAllExceptionsDataTransferObject {
      val toApprove = auditValidator.validateApproveAll(audit, user.myCompany())

      return AuditApproveAllExceptionsDataTransferObject(
         auditExceptionRepository.approveAllExceptions(toApprove, user)
      )
   }

   private fun buildExceptionReportHeader(audit: AuditEntity, onDemand: Boolean, writer: PdfWriter, document: Document) {
      val headerFont = FontFactory.getFont(COURIER, 10F, BOLD)
      val padding = 0f
      val leading = headerFont.size * 1.2F
      val ascender = true
      val descender = true
      val currentDate = LocalDate.now()

      val companyName = companyRepository.findCompanyByStore(audit.store)!!.name

      val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

      val beginAction = audit.actions.asSequence().sortedByDescending { it.id }.first { it.status == IN_PROGRESS || it.status == CREATED }
      val beginDate = dateFormatter.format(beginAction.timeCreated)

      val endAction = audit.actions.asSequence().firstOrNull { it.status == COMPLETED }
      val endDate = endAction?.let { dateFormatter.format(it.timeCreated) }
      val endEmployee = endAction?.changedBy?.getEmpName() ?: beginAction.changedBy.getEmpName()

      val headerBorder = Rectangle(0f, 0f)
      headerBorder.borderWidthLeft = 0f
      headerBorder.borderWidthBottom = 0f
      headerBorder.borderWidthRight = 0f
      headerBorder.borderWidthTop = 0f
      headerBorder.borderColorLeft = BLACK
      headerBorder.borderColorBottom = BLACK
      headerBorder.borderColorRight = BLACK
      headerBorder.borderColorTop = BLACK

      val header = PdfPTable(3)
      header.totalWidth = document.pageSize.width - 10
      header.isLockedWidth = true
      header.headerRows = 0
      header.defaultCell.border = 0
      header.widthPercentage = 100f

      header.makeCell("DATE: $currentDate", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(companyName, ALIGN_TOP, ALIGN_CENTER, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell("PAGE ${document.pageNumber}", ALIGN_TOP, ALIGN_RIGHT, headerFont, leading, padding, headerBorder, ascender, descender)

      header.makeCell("TIME: ${timeFormatter.format(LocalDateTime.now())}", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell("IDLE INVENTORY AUDIT EXCEPTION REPORT", ALIGN_TOP, ALIGN_CENTER, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(EMPTY, ALIGN_TOP, ALIGN_RIGHT, headerFont, leading, padding, headerBorder, ascender, descender)

      header.makeCell("Location: ${audit.printLocation()}", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell("(By Product)", ALIGN_TOP, ALIGN_CENTER, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(if (onDemand) "(On-Demand)" else "(Final-Reprint)", ALIGN_TOP, ALIGN_RIGHT, headerFont, leading, padding, headerBorder, ascender, descender)

      val beginDateHeader = if (beginAction.status == CREATED) "Created " else "Started "

      header.makeCell("$beginDateHeader: $beginDate", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(EMPTY, ALIGN_TOP, ALIGN_CENTER, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(EMPTY, ALIGN_TOP, ALIGN_RIGHT, headerFont, leading, padding, headerBorder, ascender, descender)

      header.makeCell("Completed: ${endDate ?: "N/A"}", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(EMPTY, ALIGN_TOP, ALIGN_CENTER, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(EMPTY, ALIGN_TOP, ALIGN_RIGHT, headerFont, leading, padding, headerBorder, ascender, descender)

      header.makeCell("Employee: $endEmployee", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(EMPTY, ALIGN_TOP, ALIGN_CENTER, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(EMPTY, ALIGN_TOP, ALIGN_RIGHT, headerFont, leading, padding, headerBorder, ascender, descender)

      header.makeCell("Audit #: ${audit.number}", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, headerBorder, ascender, descender)
      header.addCell(Phrase(EMPTY, headerFont))
      header.addCell(Phrase(EMPTY, headerFont))

      header.writeSelectedRows(0, -1, 5f, document.pageSize.height - 5, writer.directContent)
   }

   private fun buildUnscannedIdleInventoryReportHeader(audit: AuditEntity, onDemand: Boolean, writer: PdfWriter, document: Document) {
      val headerFont = FontFactory.getFont(COURIER, 10F, BOLD)
      val padding = 0f
      val leading = headerFont.getSize() * 1.2F
      val ascender = true
      val descender = true
      val currentDate = LocalDate.now()

      val companyName = companyRepository.findCompanyByStore(audit.store)!!.name

      val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

      val beginAction = audit.actions.asSequence().sortedByDescending { it.id }.first { it.status == IN_PROGRESS || it.status == CREATED }
      val beginDate = dateFormatter.format(beginAction.timeCreated)

      val endAction = audit.actions.asSequence().firstOrNull { it.status == COMPLETED }
      val endDate = endAction?.let { dateFormatter.format(it.timeCreated) }
      val endEmployee = endAction?.changedBy?.getEmpName() ?: beginAction.changedBy.getEmpName()

      val headerBorder = Rectangle(0f, 0f)
      headerBorder.borderWidthLeft = 0f
      headerBorder.borderWidthBottom = 0f
      headerBorder.borderWidthRight = 0f
      headerBorder.borderWidthTop = 0f
      headerBorder.borderColorLeft = BLACK
      headerBorder.borderColorBottom = BLACK
      headerBorder.borderColorRight = BLACK
      headerBorder.borderColorTop = BLACK

      val header = PdfPTable(3)
      header.totalWidth = document.pageSize.width - 10
      header.isLockedWidth = true
      header.headerRows = 0
      header.defaultCell.border = 0
      header.setWidthPercentage(100f)

      header.makeCell("DATE: $currentDate", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(companyName, ALIGN_TOP, ALIGN_CENTER, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell("PAGE ${document.pageNumber}", ALIGN_TOP, ALIGN_RIGHT, headerFont, leading, padding, headerBorder, ascender, descender)

      header.makeCell("TIME: ${timeFormatter.format(LocalDateTime.now())}", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell("UNSCANNED IDLE INVENTORY REPORT", ALIGN_TOP, ALIGN_CENTER, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(EMPTY, ALIGN_TOP, ALIGN_RIGHT, headerFont, leading, padding, headerBorder, ascender, descender)

      header.makeCell("Location: ${audit.printLocation()}", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell("(By Product)", ALIGN_TOP, ALIGN_CENTER, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell("${if (onDemand) "(On-Demand)" else "(Final-Reprint)"}", ALIGN_TOP, ALIGN_RIGHT, headerFont, leading, padding, headerBorder, ascender, descender)

      val beginDateHeader = if (beginAction.status == CREATED) "Created " else "Started "

      header.makeCell("$beginDateHeader: $beginDate", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(EMPTY, ALIGN_TOP, ALIGN_CENTER, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(EMPTY, ALIGN_TOP, ALIGN_RIGHT, headerFont, leading, padding, headerBorder, ascender, descender)

      header.makeCell("Completed: ${endDate ?: "N/A"}", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(EMPTY, ALIGN_TOP, ALIGN_CENTER, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(EMPTY, ALIGN_TOP, ALIGN_RIGHT, headerFont, leading, padding, headerBorder, ascender, descender)

      header.makeCell("Employee: $endEmployee", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(EMPTY, ALIGN_TOP, ALIGN_CENTER, headerFont, leading, padding, headerBorder, ascender, descender)
      header.makeCell(EMPTY, ALIGN_TOP, ALIGN_RIGHT, headerFont, leading, padding, headerBorder, ascender, descender)

      header.makeCell("Audit #: ${audit.number}", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, headerBorder, ascender, descender)
      header.addCell(Phrase(EMPTY, headerFont))
      header.addCell(Phrase(EMPTY, headerFont))

      header.writeSelectedRows(0, -1, 5f, document.pageSize.height - 5, writer.directContent)
   }

   private fun buildExceptionReport(audit: AuditEntity, pageWidth: Float): PdfPTable {
      val headerFont = FontFactory.getFont(COURIER, 10F, BOLD)
      val rowFont = FontFactory.getFont(COURIER, 10F, Font.NORMAL)

      val table = PdfPTable(8)
      val evenColor = Color(204, 204, 204)
      val oddColor = WHITE
      val padding = 0f
      val leading = headerFont.size * 1.2F
      val ascender = true
      val descender = true

      val border = Rectangle(0f, 0f)
      border.borderWidthLeft = 0f
      border.borderWidthBottom = 2f
      border.borderWidthRight = 0f
      border.borderWidthTop = 0f
      border.borderColorLeft = BLACK
      border.borderColorBottom = BLACK
      border.borderColorRight = BLACK
      border.borderColorTop = BLACK

      table.totalWidth = pageWidth - 10
      table.isLockedWidth = true
      table.headerRows = 1
      table.defaultCell.border = 0
      table.widthPercentage = 100f

      val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

      val widthPercentage: Float = (pageWidth - 10) / 8
      val widths = floatArrayOf(widthPercentage, widthPercentage, widthPercentage, widthPercentage, widthPercentage, widthPercentage, widthPercentage, widthPercentage)
      widths[0] = 75f // Scan Area
      widths[1] = 115f // Model
      widths[2] = 75f // Bar Code
      widths[3] = 75f // Alt ID
      widths[4] = 75f // Serial#
      widths[5] = 135f // Employee
      widths[6] = 135f // Scanned
      widths[7] = 200f // Exception
      table.setWidths(widths)

      table.makeCell("Scan Area", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)
      table.makeCell("Model #", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)
      table.makeCell("Bar Code", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)
      table.makeCell("Alt ID", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)
      table.makeCell("Serial #", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)
      table.makeCell("Employee", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)
      table.makeCell("Scanned", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)
      table.makeCell("Exception", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)

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

         exception.notes.forEach {
            table.addCell(EMPTY)
            table.defaultCell.colspan = 4
            table.addCell(Phrase(it.note, rowFont))
            table.defaultCell.colspan = 1
            table.defaultCell.horizontalAlignment = Element.ALIGN_LEFT
            table.addCell(Phrase(it.enteredBy.displayName(), rowFont))
            table.defaultCell.colspan = 1
            table.addCell(Phrase(dateTimeFormatter.format(it.timeUpdated), rowFont))
            table.addCell(EMPTY)
         }
      }
      return table
   }

   private fun buildUnscannedIdleInventoryReport(audit: AuditEntity, pageWidth: Float): PdfPTable {
      val headerFont = FontFactory.getFont(COURIER, 10F, BOLD)
      val rowFont = FontFactory.getFont(COURIER, 10F, Font.NORMAL)
      val columnCount = 10
      val table = PdfPTable(columnCount)
      val evenColor = Color(223, 223, 223)
      val oddColor = WHITE
      val padding = 0f
      val leading = headerFont.getSize() * 1.2F
      val ascender = true
      val descender = true

      val border = Rectangle(0f, 0f)
      border.borderWidthLeft = 0f
      border.borderWidthBottom = 2f
      border.borderWidthRight = 0f
      border.borderWidthTop = 0f
      border.borderColorLeft = BLACK
      border.borderColorBottom = BLACK
      border.borderColorRight = BLACK
      border.borderColorTop = BLACK

      table.totalWidth = pageWidth - 10
      table.isLockedWidth = true
      table.headerRows = 1
      table.defaultCell.border = 0
      table.setWidthPercentage(100f)

      val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

      val widthPercentage: Float = table.totalWidth / columnCount
      val widths = floatArrayOf(widthPercentage, widthPercentage, widthPercentage, widthPercentage, widthPercentage, widthPercentage, widthPercentage, widthPercentage, widthPercentage, widthPercentage)
      widths[0] = 60f
      widths[1] = 60f
      widths[2] = 100f
      widths[3] = 40f
      widths[4] = 175f
      widths[5] = 175f
      widths[6] = 75f
      widths[7] = 50f
      widths[8] = 50f
      widths[9] = 30f

      table.setWidths(widths)

      table.makeCell("Serial #", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)
      table.makeCell("Barcode", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)
      table.makeCell("Brand", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)
      table.makeCell("Model #", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)
      table.makeCell("Product Code", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)
      table.makeCell("Description", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)
      table.makeCell("Received Date", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)
      table.makeCell("Idle Days", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)
      table.makeCell("Condition", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)
      table.makeCell("Status", ALIGN_TOP, ALIGN_LEFT, headerFont, leading, padding, border, ascender, descender)

      var unscannedIdleInventory = inventoryRepository.findUnscannedIdleInventory(audit)

      unscannedIdleInventory.forEachIndexed { index, it ->
         table.defaultCell.backgroundColor = if (index % 2 == 0) evenColor else oddColor
         table.addCell(Phrase(it.serialNumber, rowFont))
         table.addCell(Phrase(it.barcode, rowFont))
         table.addCell(Phrase(it.brand, rowFont))
         table.addCell(Phrase(it.modelNumber, rowFont))
         table.addCell(Phrase(it.productCode, rowFont))
         table.addCell(Phrase(it.description, rowFont))
         table.addCell(Phrase(dateTimeFormatter.format(it.receivedDate), rowFont))
         table.addCell(Phrase(it.idleDays.toString(), rowFont))
         table.addCell(Phrase(it.condition, rowFont))
         table.addCell(Phrase(it.status, rowFont))
      }

      return table
   }
}
