package com.cynergisuite.middleware.purchase.order.control

import com.cynergisuite.middleware.accounting.account.payable.DefaultAccountPayableStatusTypeDTO
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.employee.EmployeeService
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.purchase.order.control.infrastructure.PurchaseOrderControlRepository
import com.cynergisuite.middleware.purchase.order.type.ApprovalRequiredFlagDTO
import com.cynergisuite.middleware.purchase.order.type.DefaultPurchaseOrderTypeDTO
import com.cynergisuite.middleware.purchase.order.type.UpdatePurchaseOrderCostTypeValueObject
import com.cynergisuite.middleware.shipping.shipvia.ShipViaEntity
import com.cynergisuite.middleware.vendor.VendorDTO
import com.cynergisuite.middleware.vendor.VendorService
import io.micronaut.context.annotation.Value
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import java.io.File
import java.io.FileWriter
import java.util.UUID
import java.util.concurrent.TimeUnit

@Singleton
class PurchaseOrderControlService @Inject constructor(
   private val purchaseOrderControlValidator: PurchaseOrderControlValidator,
   private val purchaseOrderControlRepository: PurchaseOrderControlRepository,
   private val employeeService: EmployeeService,
   @Value("\${cynergi.process.update.isam.poctl}") private val processUpdateIsamPoCtl: Boolean
) {
   private val logger: Logger = LoggerFactory.getLogger(PurchaseOrderControlService::class.java)

   fun fetchOne(company: CompanyEntity): PurchaseOrderControlDTO? {
      return purchaseOrderControlRepository.findOne(company)?.let { transformEntity(it) }
   }

   fun create(dto: PurchaseOrderControlDTO, company: CompanyEntity): PurchaseOrderControlDTO {
      val toCreate = purchaseOrderControlValidator.validateCreate(dto, company)

      val purchaseOrderControlCreated = purchaseOrderControlRepository.insert(toCreate, company)
      if (processUpdateIsamPoCtl) {
         poControlToISAM("I", purchaseOrderControlCreated, company)
      }
      return transformEntity(purchaseOrderControlCreated)
   }

   fun update(id: UUID, dto: PurchaseOrderControlDTO, company: CompanyEntity): PurchaseOrderControlDTO {
      val (existing, toUpdate) = purchaseOrderControlValidator.validateUpdate(id, dto, company)

      return if (existing != toUpdate) {
         val purchaseOrderControlUpdated = purchaseOrderControlRepository.update(toUpdate, company)
         if (processUpdateIsamPoCtl) {
            poControlToISAM("U", purchaseOrderControlUpdated, company)
         }
         transformEntity(purchaseOrderControlUpdated)
      } else {
         transformEntity(existing)
      }
   }

   private fun transformEntity(purchaseOrderControl: PurchaseOrderControlEntity): PurchaseOrderControlDTO {
      return PurchaseOrderControlDTO(
         entity = purchaseOrderControl,
         defaultAccountPayableStatusType = DefaultAccountPayableStatusTypeDTO(purchaseOrderControl.defaultAccountPayableStatusType),
         defaultVendor = purchaseOrderControl.defaultVendor?.let { VendorDTO(it) },
         updatePurchaseOrderCost = UpdatePurchaseOrderCostTypeValueObject(purchaseOrderControl.updatePurchaseOrderCost),
         defaultPurchaseOrderType = DefaultPurchaseOrderTypeDTO(purchaseOrderControl.defaultPurchaseOrderType),
         approvalRequiredFlagType = ApprovalRequiredFlagDTO(purchaseOrderControl.approvalRequiredFlagType)
      )
   }

   fun fetchApprovers(user: User): List<EmployeeValueObject> {
      return employeeService.fetchPurchaseOrderApprovers(user)
   }

   fun poControlToISAM(task: String, poCtl: PurchaseOrderControlEntity, company: CompanyEntity) {
      var fileWriter: FileWriter? = null
      var csvPrinter: CSVPrinter? = null

      val dropFiveCharOnModel: String
      val updateAccountPayable: String
      val printSecondDescription: String
      val printVendorComments: String
      val includeFreightInCost: String
      val updateCostOnModel: String
      val sortByShipToOnPrint: String
      val invoiceByLocation: String
      val validateInventory: String

      var dataset = company.datasetCode

      val fileName = File.createTempFile("mrpoctl", ".csv")

      dropFiveCharOnModel = if (poCtl.dropFiveCharactersOnModelNumber) {
         "Y"
      } else {
         "N"
      }

      updateAccountPayable = if (poCtl.updateAccountPayable) {
         "Y"
      } else {
         "N"
      }

      printSecondDescription = if (poCtl.printSecondDescription) {
         "Y"
      } else {
         "N"
      }

      printVendorComments = if (poCtl.printVendorComments) {
         "Y"
      } else {
         "N"
      }

      includeFreightInCost = if (poCtl.includeFreightInCost) {
         "Y"
      } else {
         "N"
      }

      updateCostOnModel = if (poCtl.updateCostOnModel) {
         "Y"
      } else {
         "N"
      }

      sortByShipToOnPrint = if (poCtl.sortByShipToOnPrint) {
         "Y"
      } else {
         "N"
      }

      invoiceByLocation = if (poCtl.invoiceByLocation) {
         "Y"
      } else {
         "N"
      }

      validateInventory = if (poCtl.validateInventory) {
         "Y"
      } else {
         "N"
      }

      val defaultStatusType: String = poCtl.defaultAccountPayableStatusType.value

      val defaultVendor: String = if (poCtl.defaultVendor != null) {
         poCtl.defaultVendor.number.toString()
      } else {
         "0"
      }

      val updatePOCost: String = poCtl.updatePurchaseOrderCost.value

      val defaultPOType: String = poCtl.defaultPurchaseOrderType.value

      val defaultApprover: String = if (poCtl.defaultApprover != null) {
         poCtl.defaultApprover.number.toString()
      } else {
         "0"
      }

      val approvalRequiredFlag: String = poCtl.approvalRequiredFlagType.value

      try {
         fileWriter = FileWriter(fileName)
         csvPrinter = CSVPrinter(fileWriter, CSVFormat.DEFAULT.withDelimiter('|').withHeader(
            "action",
            "drop_five",
            "update_acct_payable",
            "print_second_desc",
            "default_ap_status_type",
            "print_vendor_comments",
            "include_freight_in_cost",
            "update_cost_on_model",
            "default_vendor",
            "update_po_cost",
            "default_po_type",
            "sort_ship_to_on_print",
            "invoice_by_location",
            "validate_inventory",
            "default_approver",
            "approval_required_flag",
            "dummy_field"))

         var data = listOf(
            "action",
            "drop_five",
            "update_acct_payable",
            "print_second_desc",
            "default_ap_status_type",
            "print_vendor_comments",
            "include_freight_in_cost",
            "update_cost_on_model",
            "default_vendor",
            "update_po_cost",
            "default_po_type",
            "sort_ship_to_on_print",
            "invoice_by_location",
            "validate_inventory",
            "default_approver",
            "approval_required_flag",
            "dummy_field")

         data = listOf(
            task,
            dropFiveCharOnModel,
            updateAccountPayable,
            printSecondDescription,
            defaultStatusType,
            printVendorComments,
            includeFreightInCost,
            updateCostOnModel,
            defaultVendor,
            updatePOCost,
            defaultPOType,
            sortByShipToOnPrint,
            invoiceByLocation,
            validateInventory,
            defaultApprover,
            approvalRequiredFlag,
            "1")
         csvPrinter.printRecord(data)

      } catch (e: Exception) {
         logger.error("Error occurred in creating PO-CTL csv file!", e)
      } finally {
         try {
            fileWriter!!.flush()
            fileWriter.close()
            csvPrinter!!.close()
            val processExecutor: ProcessExecutor = ProcessExecutor()
               .command("/bin/bash", "/usr/bin/ht.updt_isam_poctl.sh", fileName.canonicalPath, dataset)
               .exitValueNormal()
               .timeout(5, TimeUnit.SECONDS)
               .readOutput(true)
            logger.debug(processExecutor.execute().outputString())
         } catch (e: Throwable) {
            logger.error("Error occurred in creating PO-CTL csv file!", e)
         }
      }
   }
}
