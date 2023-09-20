package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.accounting.account.AccountService
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.vendor.infrastructure.VendorPageRequest
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import com.cynergisuite.middleware.vendor.infrastructure.VendorSearchPageRequest
import io.micronaut.context.annotation.Value
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.io.File
import java.io.FileWriter
import java.util.concurrent.TimeUnit
import java.util.UUID
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor

@Singleton
class VendorService @Inject constructor(
   private val vendorRepository: VendorRepository,
   private val vendorValidator: VendorValidator,
   @Value("\${cynergi.process.update.isam.vendor}") private val processUpdateIsamVendor: Boolean
) {
   private val logger: Logger = LoggerFactory.getLogger(VendorService::class.java)

   fun fetchById(id: UUID, company: CompanyEntity): VendorDTO? =
      vendorRepository.findOne(id, company)?.let { VendorDTO(entity = it) }

   fun create(dto: VendorDTO, company: CompanyEntity): VendorDTO {
      val toCreate = vendorValidator.validateCreate(dto, company)

      val vendorEntity = vendorRepository.insert(entity = toCreate)
      if (processUpdateIsamVendor) {
         vendorToISAM("I", vendorEntity, company)
      }
      return VendorDTO(
         vendorEntity
      )
   }

   fun fetchAll(company: CompanyEntity, pageRequest: VendorPageRequest): Page<VendorDTO> {
      val found = vendorRepository.findAll(company, pageRequest)

      return found.toPage { vendor: VendorEntity ->
         VendorDTO(vendor)
      }
   }

   fun search(company: CompanyEntity, pageRequest: VendorSearchPageRequest): Page<VendorDTO> {
      val found = vendorRepository.search(company, pageRequest)

      return found.toPage { vendor: VendorEntity ->
         VendorDTO(vendor)
      }
   }

   fun update(id: UUID, dto: VendorDTO, company: CompanyEntity): VendorDTO {
      val (existing, toUpdate) = vendorValidator.validateUpdate(id, dto, company)

      val vendorEntity = vendorRepository.update(existing, toUpdate)
      if (processUpdateIsamVendor) {
         vendorToISAM("U", vendorEntity, company)
      }
      return VendorDTO(
         vendorEntity
      )
   }

   fun delete(id: UUID, company: CompanyEntity) {
      val deleletedVendor = vendorRepository.findOne(id, company)
      vendorRepository.delete(id, company)
      if (processUpdateIsamVendor) {
         vendorToISAM("D", deleletedVendor!!, company)
      }
   }

   fun vendorToISAM(task: String, vendor: VendorEntity, company: CompanyEntity) {
      var fileWriter: FileWriter? = null
      var csvPrinter: CSVPrinter? = null

      var dataset = company.datasetCode

      var accountNumber: String
      var payToNumber: String
      var freightOnboardType: String = ""
      var freightCalcMethodType: String = ""
      var returnPolicy: String
      var vendor1099: String
      var FIN: String
      var salesRepName: String
      var salesRepFax: String
      var separateCheck: String
      var freightPct: String
      var freightAmt: String
      var chgInvTax1: String
      var chgInvTax2: String
      var chgInvTax3: String
      var chgInvTax4: String
      var FINVerify: String
      var POSubmitEmail: String
      var allowDropshipToCust: String
      var autoSubmitPO: String
      var vendorGroup: String

      if (vendor.accountNumber != null) {
         accountNumber = vendor.accountNumber!!.toString()
      } else {
         accountNumber = ""
      }

      if (vendor.returnPolicy!!) {
         returnPolicy = "Y"
      } else {
         returnPolicy = "N"
      }

      if (vendor.vendor1099!!) {
         vendor1099 = "Y"
      } else {
         vendor1099 = "N"
      }

      if (vendor.federalIdNumber != null) {
         FIN = vendor.federalIdNumber!!
      } else {
         FIN = ""
      }

      if (vendor.salesRepresentativeName != null) {
         salesRepName = vendor.salesRepresentativeName!!
      } else {
         salesRepName = ""
      }

      if (vendor.salesRepresentativeFax != null) {
         salesRepFax = vendor.salesRepresentativeFax!!
      } else {
         salesRepFax = ""
      }

      if (vendor.separateCheck!!) {
         separateCheck = "Y"
      } else {
         separateCheck = "N"
      }

      if (vendor.freightPercent != null) {
         freightPct = vendor.freightPercent!!.toString()
      } else {
         freightPct = "0"
      }

      if (vendor.freightAmount != null) {
         freightAmt = vendor.freightAmount!!.toString()
      } else {
         freightAmt = "0"
      }

      if (vendor.chargeInventoryTax1!!) {
         chgInvTax1 = "Y"
      } else {
         chgInvTax1 = "N"
      }

      if (vendor.chargeInventoryTax2!!) {
         chgInvTax2 = "Y"
      } else {
         chgInvTax2 = "N"
      }

      if (vendor.chargeInventoryTax3!!) {
         chgInvTax3 = "Y"
      } else {
         chgInvTax3 = "N"
      }

      if (vendor.chargeInventoryTax4!!) {
         chgInvTax4 = "Y"
      } else {
         chgInvTax4 = "N"
      }

      if (vendor.federalIdNumberVerification!!) {
         FINVerify = "Y"
      } else {
         FINVerify = "N"
      }

      if (vendor.purchaseOrderSubmitEmailAddress != null) {
         POSubmitEmail = vendor.purchaseOrderSubmitEmailAddress!!
      } else {
         POSubmitEmail = ""
      }

      if (vendor.allowDropShipToCustomer!!) {
         allowDropshipToCust = "Y"
      } else {
         allowDropshipToCust = "N"
      }

      if (vendor.autoSubmitPurchaseOrder!!) {
         autoSubmitPO = "Y"
      } else {
         autoSubmitPO = "N"
      }

      freightOnboardType = vendor.freightOnboardType!!.value!!

      freightCalcMethodType = vendor.freightCalcMethodType.value

      if (vendor.payTo != null) {
         var payToVendor = vendorRepository.findOne(vendor.payTo.myId()!!, company)
         payToNumber = payToVendor!!.number.toString()
      } else {
         payToNumber = ""
      }

      if (vendor.vendorGroup != null) {
         vendorGroup = vendor.vendorGroup.value
      } else {
         vendorGroup = ""
      }

      val fileName = File.createTempFile("mrvendor", ".csv")

      try {
         fileWriter = FileWriter(fileName)
         csvPrinter = CSVPrinter(fileWriter, CSVFormat.DEFAULT.withDelimiter('|').withHeader(
            "action",
            "number",
            "name",
            "acct_number",
            "pay_to_nbr",
            "freight_on_board_type",
            "pmt_terms",
            "normal_days",
            "return_policy",
            "ship_via",
            "vendor_group",
            "min_qty",
            "min_amt",
            "free_ship_qty",
            "free_ship_amt",
            "1099",
            "FIN",
            "sales_rep_name",
            "sales_rep_fax",
            "separate_check",
            "bump_pct",
            "freight_calc_method",
            "freight_pct",
            "freight_amt",
            "chg_inv_tax_1",
            "chg_inv_tax_2",
            "chg_inv_tax_3",
            "chg_inv_tax_4",
            "FIN_verify",
            "PO_submit_email",
            "allow_dropship",
            "auto_submit_PO",
            "dummy_field"))

         var data = listOf(
            "action",
            "number",
            "name",
            "acct_number",
            "pay_to_nbr",
            "freight_on_board_type",
            "pmt_terms",
            "normal_days",
            "return_policy",
            "ship_via",
            "vendor_group",
            "min_qty",
            "min_amt",
            "free_ship_qty",
            "free_ship_amt",
            "1099",
            "FIN",
            "sales_rep_name",
            "sales_rep_fax",
            "separate_check",
            "bump_pct",
            "freight_calc_method",
            "freight_pct",
            "freight_amt",
            "chg_inv_tax_1",
            "chg_inv_tax_2",
            "chg_inv_tax_3",
            "chg_inv_tax_4",
            "FIN_verify",
            "PO_submit_email",
            "allow_dropship",
            "auto_submit_PO",
            "dummy_field")

         data = listOf(
            task,
            vendor.number.toString(),
            vendor.name,
            accountNumber,
            payToNumber,
            freightOnboardType,
            vendor.paymentTerm.description, //Will use this description to look up the correct record in the ISAM
            vendor.normalDays.toString(),
            returnPolicy,
            vendor.shipVia.description, //Will use this description to look up the correct record in the ISAM
            vendorGroup, //10 char here, but 8 in ISAM. May see issues.
            vendor.minimumQuantity.toString(),
            vendor.minimumAmount.toString(),
            vendor.freeShipQuantity.toString(),
            vendor.freeShipAmount.toString(),
            vendor1099,
            FIN,
            salesRepName,
            salesRepFax,
            separateCheck,
            vendor.bumpPercent.toString(),
            freightCalcMethodType,
            freightPct,
            freightAmt,
            chgInvTax1,
            chgInvTax2,
            chgInvTax3,
            chgInvTax4,
            FINVerify,
            POSubmitEmail,
            allowDropshipToCust,
            autoSubmitPO,
            "1")
         csvPrinter.printRecord(data)

      } catch (e: Exception) {
         logger.error("Error occurred in creating vendor csv file!", e)
      } finally {
         try {
            fileWriter!!.flush()
            fileWriter.close()
            csvPrinter!!.close()
            val processExecutor: ProcessExecutor = ProcessExecutor()
               .command("/bin/bash", "/usr/bin/ht.updt_isam_vendor.sh", fileName.canonicalPath, dataset)
               .exitValueNormal()
               .timeout(5, TimeUnit.SECONDS)
               .readOutput(true)
            logger.debug(processExecutor.execute().outputString())
         } catch (e: Throwable) {
            logger.error("Error occurred in creating vendor csv file!", e)
         }
      }
   }

}
