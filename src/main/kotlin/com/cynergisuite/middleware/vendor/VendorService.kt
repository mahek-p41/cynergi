package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.GeneralLedgerJournalReportFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.domain.Vendor1099FilterRequest
import com.cynergisuite.middleware.accounting.account.AccountService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerPendingReportTemplate
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

   fun fetch1099Report(company: CompanyEntity, filterRequest: Vendor1099FilterRequest): List<Vendor1099DTO> {
      //val found = vendorRepository.fetch1099Report(company, filterRequest)

      //return Vendor1099DTO(found)
      //return found
      return vendorRepository.fetch1099Report(company, filterRequest)
   }

   fun vendorToISAM(task: String, vendor: VendorEntity, company: CompanyEntity) {
      var fileWriter: FileWriter? = null
      var csvPrinter: CSVPrinter? = null

      val dataset = company.datasetCode

      val accountNumber: String = if (vendor.accountNumber != null) { vendor.accountNumber.toString() } else { "" }
      val payToNumber: String
      val freightOnboardType: String = vendor.freightOnboardType.value
      val freightCalcMethodType: String = vendor.freightCalcMethodType.value
      val returnPolicy: String = if (vendor.returnPolicy) { "Y" } else { "N" }
      val vendor1099: String = if (vendor.vendor1099) { "Y" } else { "N" }
      val FIN: String = if (vendor.federalIdNumber != null) { vendor.federalIdNumber } else { "" }
      val salesRepName: String = if (vendor.salesRepresentativeName != null) { vendor.salesRepresentativeName } else { "" }
      val salesRepFax: String = if (vendor.salesRepresentativeFax != null) { vendor.salesRepresentativeFax } else { "" }
      val separateCheck: String = if (vendor.separateCheck) { "Y" } else { "N" }
      val freightPct: String = if (vendor.freightPercent != null) { vendor.freightPercent.toString() } else { "0" }
      val freightAmt: String = if (vendor.freightAmount != null) { vendor.freightAmount.toString() } else { "0" }
      val chgInvTax1: String = if (vendor.chargeInventoryTax1) { "Y" } else { "N" }
      val chgInvTax2: String = if (vendor.chargeInventoryTax2) { "Y" } else { "N" }
      val chgInvTax3: String = if (vendor.chargeInventoryTax3) { "Y" } else { "N" }
      val chgInvTax4: String = if (vendor.chargeInventoryTax4) { "Y" } else { "N" }
      val FINVerify: String = if (vendor.federalIdNumberVerification) { "Y" } else { "N" }
      val POSubmitEmail: String = if (vendor.purchaseOrderSubmitEmailAddress != null) { vendor.purchaseOrderSubmitEmailAddress } else { "" }
      val allowDropshipToCust: String = if (vendor.allowDropShipToCustomer) { "Y" } else { "N" }
      val autoSubmitPO: String = if (vendor.autoSubmitPurchaseOrder) { "Y" } else { "N" }
      val vendorGroup: String = if (vendor.vendorGroup != null) { vendor.vendorGroup.value } else { "" }

      val normalDays: String = if (vendor.normalDays != null) { vendor.normalDays.toString() } else { "" }
      val minimumAmount: String = if (vendor.minimumAmount != null) { vendor.minimumAmount.toString() } else { "" }
      val freeShipAmount: String = if (vendor.freeShipAmount != null) { vendor.freeShipAmount.toString() } else { "" }
      val bumpPercent: String = if (vendor.bumpPercent != null) { vendor.bumpPercent.toString() } else { "" }
      val number: String = if (vendor.number != null) { vendor.number.toString() } else { "" }

      val minimumQuantity: String = if (vendor.minimumQuantity != null) { vendor.minimumQuantity.toString() } else { "" }
      val freeShipQuantity: String = if (vendor.freeShipQuantity != null) { vendor.freeShipQuantity.toString() } else { "" }

      if (vendor.payTo != null) {
         val payToVendor = vendorRepository.findOne(vendor.payTo.myId()!!, company)
         payToNumber = payToVendor!!.number.toString()
      } else {
         payToNumber = ""
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
            number,
            vendor.name,
            accountNumber,
            payToNumber,
            freightOnboardType,
            vendor.paymentTerm.description, //Will use this description to look up the correct record in the ISAM
            normalDays,
            returnPolicy,
            vendor.shipVia.description, //Will use this description to look up the correct record in the ISAM
            vendorGroup, //10 char here, but 8 in ISAM. May see issues.
            minimumQuantity,
            minimumAmount,
            freeShipQuantity,
            freeShipAmount,
            vendor1099,
            FIN,
            salesRepName,
            salesRepFax,
            separateCheck,
            bumpPercent,
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
