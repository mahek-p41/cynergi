package com.cynergisuite.middleware.vendor.payment.term

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.account.AccountService
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import com.cynergisuite.middleware.vendor.payment.term.schedule.VendorPaymentTermScheduleEntity
import io.micronaut.context.annotation.Value
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.io.File
import java.io.FileWriter
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import java.util.UUID
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor


@Singleton
class VendorPaymentTermService @Inject constructor(
   private val vendorPaymentTermRepository: VendorPaymentTermRepository,
   private val vendorPaymentTermValidator: VendorPaymentTermValidator,
   @Value("\${cynergi.process.update.isam.vendterm}") private val processUpdateIsamVendterm: Boolean
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountService::class.java)

   fun fetchById(id: UUID, company: CompanyEntity): VendorPaymentTermDTO? =
      vendorPaymentTermRepository.findOne(id, company)?.let { VendorPaymentTermDTO(entity = it) }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<VendorPaymentTermDTO> {
      val found = vendorPaymentTermRepository.findAll(company, pageRequest)

      return found.toPage { vendorPaymentTerm: VendorPaymentTermEntity ->
         VendorPaymentTermDTO(vendorPaymentTerm)
      }
   }

   fun create(dto: VendorPaymentTermDTO, company: CompanyEntity): VendorPaymentTermDTO {
      val toCreate = vendorPaymentTermValidator.validateCreate(dto, company)

      val vendorPaymentTermEntity = vendorPaymentTermRepository.insert(entity = toCreate)
      if (processUpdateIsamVendterm) {
         vendtermToISAM("I", vendorPaymentTermEntity, company)
      }
      return VendorPaymentTermDTO(
         vendorPaymentTermEntity
      )
   }

   fun update(id: UUID, dto: VendorPaymentTermDTO, company: CompanyEntity): VendorPaymentTermDTO {
      val toUpdate = vendorPaymentTermValidator.validateUpdate(id, dto, company)

      val startingVendorPaymentTerm = vendorPaymentTermRepository.findOne(dto.id!!, company)
      val startingDescription = startingVendorPaymentTerm!!.description

      val vendorPaymentTermEntity = vendorPaymentTermRepository.update(entity = toUpdate)
      if (processUpdateIsamVendterm) {
         vendtermToISAM("U", vendorPaymentTermEntity, company, startingDescription)
      }
      return VendorPaymentTermDTO(
         vendorPaymentTermEntity
      )
   }

   fun delete(id: UUID, company: CompanyEntity) {
      val deleletedVendterm = vendorPaymentTermRepository.findOne(id, company)
      vendorPaymentTermRepository.delete(id, company)
      if (processUpdateIsamVendterm) {
         vendtermToISAM("D", deleletedVendterm!!, company)
      }
   }

   fun vendtermToISAM(task: String, vendterm: VendorPaymentTermEntity, company: CompanyEntity, beginningDescription: String? = " ") {
      var fileWriter: FileWriter? = null
      var csvPrinter: CSVPrinter? = null
      var discountMonth: String
      var discountDays: String
      var discountPercent: String
      var due_month_1: Int = 0
      var due_days_1: Int = 0
      var due_percent_1: BigDecimal = BigDecimal.ZERO
      var schedule_order_number_1: Int = 0
      var due_month_2: Int = 0
      var due_days_2: Int = 0
      var due_percent_2: BigDecimal = BigDecimal.ZERO
      var schedule_order_number_2: Int = 0
      var due_month_3: Int = 0
      var due_days_3: Int = 0
      var due_percent_3: BigDecimal = BigDecimal.ZERO
      var schedule_order_number_3: Int = 0
      var due_month_4: Int = 0
      var due_days_4: Int = 0
      var due_percent_4: BigDecimal = BigDecimal.ZERO
      var schedule_order_number_4: Int = 0
      var due_month_5: Int = 0
      var due_days_5: Int = 0
      var due_percent_5: BigDecimal = BigDecimal.ZERO
      var schedule_order_number_5: Int = 0
      var due_month_6: Int = 0
      var due_days_6: Int = 0
      var due_percent_6: BigDecimal = BigDecimal.ZERO
      var schedule_order_number_6: Int = 0

      var dataset = company.datasetCode

      val fileName = File.createTempFile("mrvterms", ".csv")

      for ((index, scheduleRecord) in vendterm.scheduleRecords.withIndex()) {
         when (index)
         {
            0 -> {
               if (scheduleRecord.dueMonth != null) due_month_1 = scheduleRecord.dueMonth
               due_days_1 = scheduleRecord.dueDays
               due_percent_1 = scheduleRecord.duePercent
               schedule_order_number_1 = scheduleRecord.scheduleOrderNumber
            }
            1 -> {
               if (scheduleRecord.dueMonth != null) due_month_2 = scheduleRecord.dueMonth
               due_days_2 = scheduleRecord.dueDays
               due_percent_2 = scheduleRecord.duePercent
               schedule_order_number_2 = scheduleRecord.scheduleOrderNumber
            }
            2 -> {
               if (scheduleRecord.dueMonth != null) due_month_3 = scheduleRecord.dueMonth
               due_days_3 = scheduleRecord.dueDays
               due_percent_3 = scheduleRecord.duePercent
               schedule_order_number_3 = scheduleRecord.scheduleOrderNumber
            }
            3 -> {
               if (scheduleRecord.dueMonth != null) due_month_4 = scheduleRecord.dueMonth
               due_days_4 = scheduleRecord.dueDays
               due_percent_4 = scheduleRecord.duePercent
               schedule_order_number_4 = scheduleRecord.scheduleOrderNumber
            }
            4 -> {
               if (scheduleRecord.dueMonth != null) due_month_5 = scheduleRecord.dueMonth
               due_days_5 = scheduleRecord.dueDays
               due_percent_5 = scheduleRecord.duePercent
               schedule_order_number_5 = scheduleRecord.scheduleOrderNumber
            }
            5 -> {
               if (scheduleRecord.dueMonth != null) due_month_6 = scheduleRecord.dueMonth
               due_days_6 = scheduleRecord.dueDays
               due_percent_6 = scheduleRecord.duePercent
               schedule_order_number_6 = scheduleRecord.scheduleOrderNumber
            }
         }
      }

      if (vendterm.discountMonth != null) {
         discountMonth = vendterm.discountMonth.toString()
      } else {
         discountMonth = "0"
      }

      if (vendterm.discountDays != null) {
         discountDays = vendterm.discountDays.toString()
      } else {
         discountDays = "0"
      }

      if (vendterm.discountPercent != null) {
         discountPercent = vendterm.discountPercent.toString()
      } else {
         discountPercent = "0"
      }

      try {
         fileWriter = FileWriter(fileName)
         csvPrinter = CSVPrinter(fileWriter, CSVFormat.DEFAULT.withDelimiter('|').withHeader(
            "action",
            "beginning_description",
            "description",
            "discount_month",
            "discount_days",
            "discount_percent",
            "due_month_1",
            "due_days_1",
            "due_percent_1",
            "schedule_order_number_1",
            "due_month_2",
            "due_days_2",
            "due_percent_2",
            "schedule_order_number_2",
            "due_month_3",
            "due_days_3",
            "due_percent_3",
            "schedule_order_number_3",
            "due_month_4",
            "due_days_4",
            "due_percent_4",
            "schedule_order_number_4",
            "due_month_5",
            "due_days_5",
            "due_percent_5",
            "schedule_order_number_5",
            "due_month_6",
            "due_days_6",
            "due_percent_6",
            "schedule_order_number_6",
            "dummy_field"))

         var data = listOf(
            "action",
            "beginning_description",
            "description",
            "discount_month",
            "discount_days",
            "discount_percent",
            "due_month_1",
            "due_days_1",
            "due_percent_1",
            "schedule_order_number_1",
            "due_month_2",
            "due_days_2",
            "due_percent_2",
            "schedule_order_number_2",
            "due_month_3",
            "due_days_3",
            "due_percent_3",
            "schedule_order_number_3",
            "due_month_4",
            "due_days_4",
            "due_percent_4",
            "schedule_order_number_4",
            "due_month_5",
            "due_days_5",
            "due_percent_5",
            "schedule_order_number_5",
            "due_month_6",
            "due_days_6",
            "due_percent_6",
            "schedule_order_number_6",
            "dummy_field")

         data = listOf(
            task,
            beginningDescription!!,
            vendterm.description,
            discountMonth,
            discountDays,
            discountPercent,
            due_month_1.toString(),
            due_days_1.toString(),
            due_percent_1.toString(),
            schedule_order_number_1.toString(),
            due_month_2.toString(),
            due_days_2.toString(),
            due_percent_2.toString(),
            schedule_order_number_2.toString(),
            due_month_3.toString(),
            due_days_3.toString(),
            due_percent_3.toString(),
            schedule_order_number_3.toString(),
            due_month_4.toString(),
            due_days_4.toString(),
            due_percent_4.toString(),
            schedule_order_number_4.toString(),
            due_month_5.toString(),
            due_days_5.toString(),
            due_percent_5.toString(),
            schedule_order_number_5.toString(),
            due_month_6.toString(),
            due_days_6.toString(),
            due_percent_6.toString(),
            schedule_order_number_6.toString(),
            "1")
         csvPrinter.printRecord(data)

      } catch (e: Exception) {
         logger.error("Error occurred in creating account csv file!", e)
      } finally {
         try {
            fileWriter!!.flush()
            fileWriter.close()
            csvPrinter!!.close()
            val processExecutor: ProcessExecutor = ProcessExecutor()
               .command("/bin/bash", "/usr/bin/ht.updt_isam_vendterm.sh", fileName.canonicalPath, dataset)
               .exitValueNormal()
               .timeout(5, TimeUnit.SECONDS)
               .readOutput(true)
            logger.debug(processExecutor.execute().outputString())
         } catch (e: Throwable) {
            logger.error("Error occurred in creating account csv file!", e)
         }
      }
   }

}
