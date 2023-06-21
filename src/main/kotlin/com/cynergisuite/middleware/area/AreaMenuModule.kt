package com.cynergisuite.middleware.area

import com.cynergisuite.domain.TypeDomain
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder

sealed class ModuleType(
   val id: Int,
   val value: String,
   val description: String,
   val localizationCode: String,
   val program: String,
) : TypeDomain() {
   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode

   override fun myEquality(typeDomainEntity: TypeDomain): Boolean {
      return if (typeDomainEntity is ModuleType) {
         EqualsBuilder()
            .appendSuper(super.myEquality(typeDomainEntity))
            .append(this.program, typeDomainEntity.program)
            .build()
      } else {
         false
      }
   }

   override fun myHashCode(): Int {
      return HashCodeBuilder()
         .appendSuper(super.myHashCode())
         .append(this.program)
         .build()
   }
}

object TypeListing {
   object APADD : ModuleType(1, "APADD", "Add Invoices", "add.invoices", "APADD")
   object APSHO : ModuleType(2, "APSHO", "Show Invoices", "show.invoices", "APSHO")
   object APCHG : ModuleType(3, "APCHG", "AP Change Invoices", "change.invoices", "APCHG")
   object APDEL : ModuleType(4, "APDEL", "Delete Invoices", "delete.invoices", "APDEL")
   object APCHECK : ModuleType(5, "APCHECK", "Print Checks", "print.checks", "APCHECK")
   object APCHKRPT : ModuleType(6, "APCHKRPT", "Check Report", "check.report", "APCHKRPT")
   object APCLEAR : ModuleType(7, "APCLEAR", "Clear Checks", "clear.checks", "APCLEAR")
   object APSEL : ModuleType(8, "APSEL", "Select Invoices", "select.invoices", "APSEL")
   object APPREVUE : ModuleType(9, "APPREVUE", "Check Preview Rpt", "check.preview.rpt", "APPREVUE")
   object APVOID : ModuleType(10, "APVOID", "Void Checks", "void.checks", "APVOID")
   object APAGERPT : ModuleType(11, "APAGERPT", "Aging Report", "aging.report", "APAGERPT")
   object APRPT : ModuleType(12, "APRPT", "AP Report", "ap.report", "APRPT")
   object CASHOUT : ModuleType(13, "CASHOUT", "Cash Requirements", "cash.requirements", "CASHOUT")
   object APSTATUS : ModuleType(14, "APSTATUS", "Vendor Statistics", "vendor.statistics", "APSTATUS")
   object POADD : ModuleType(15, "POADD", "Add PO", "add.po", "POADD")
   object POCHG : ModuleType(16, "POCHG", "Change PO", "change.po", "POCHG")
   object PODEL : ModuleType(17, "PODEL", "Delete PO", "delete.po", "PODEL")
   object POLST : ModuleType(18, "POLST", "List by PO", "list.by.po", "POLST")
   object POPURGE : ModuleType(19, "POPURGE", "Purge PO Records", "purge.po.records", "POPURGE")
   object POSHO : ModuleType(20, "POSHO", "Inquiry", "inquiry", "POSHO")
   object POINLOAD : ModuleType(21, "POINLOAD", "Receive From PO", "receive.from.po", "POINLOAD")
   object POUPDT : ModuleType(22, "POUPDT", "Update PO", "update.po", "POUPDT")
   object SPOADD : ModuleType(23, "SPOADD", "Special Orders", "special.orders", "SPOADD")
   object SPOLST : ModuleType(24, "SPOLST", "List Special Ord", "list.special.ord", "SPOLST")
   object POCAN : ModuleType(25, "POCAN", "Cancel PO", "cancel.po", "POCAN")
   object POCOPY : ModuleType(26, "POCOPY", "Copy a PO", "copy.a.po", "POCOPY")
   object INVORDMT : ModuleType(27, "INVORDMT", "Allocate/Inq Special Orders and POs", "allocate.inq.special.orders.and.pos", "INVORDMT")
   object INVCRED : ModuleType(28, "INVCRED", "Return Item for Credit to Vendor", "return.item.for.credit.to.vendor", "INVCRED")
   object INVAVAIL : ModuleType(29, "INVAVAIL", "inventory.availability", "inventory.availability", "INVAVAIL")
   object POSTAT : ModuleType(30, "POSTAT", "Change PO Status to Open", "change.po.status.to.open", "POSTAT")
   object PODLST : ModuleType(31, "PODLST", "List by Items", "list.by.items", "PODLST")
   object POSTAT1 : ModuleType(32, "POSTAT1", "Change PO Status", "change.po.status", "POSTAT1")
   object PODSQLST : ModuleType(33, "PODSQLST", "List PO by Sequence #", "list.po.by.sequence.number", "PODSQLST")
   object ITEMMNTS : ModuleType(34, "ITEMMNTS", "Model", "model.maintenance", "ITEMMNTS")
   object VENDOR : ModuleType(35, "VENDOR", "Vendor", "vendor.maintenance", "VENDOR")
   object PODETCHG : ModuleType(36, "PODETCHG", "Adjust Receiving Quantities", "adjust.receiving.quantities", "PODETCHG")
   object POREC : ModuleType(37, "POREC", "Enter Receiving", "enter.receiving", "POREC")
   object PORECLST : ModuleType(38, "PORECLST", "List Receiving", "list.receiving", "PORECLST")
   object PORECRPT : ModuleType(39, "PORECRPT", "Receiving Rpt", "receiving.rpt", "PORECRPT")
   object PORPT : ModuleType(40, "PORPT", "PO Report", "po.report", "PORPT")
   object POWRKSHT : ModuleType(41, "POWRKSHT", "Receiving Worksheet", "receiving.worksheet", "POWRKSHT")
   object QUOTERPT : ModuleType(42, "QUOTERPT", "Quote Report", "quote.report", "QUOTERPT")
   object VDRQUOTE : ModuleType(43, "VDRQUOTE", "Vendor Quotes", "vendor.quotes", "VDRQUOTE")
   object SPOPRT : ModuleType(44, "SPOPRT", "Prt Special Ord", "prt.special.ord", "SPOPRT")
   object STKRERDR : ModuleType(45, "STKRERDR", "Stock Reorder", "stock.reorder", "STKRERDR")
   object GETSTKLV : ModuleType(46, "GETSTKLV", "Update Stock Reorder Control File", "update.stock.reorder.control.file", "GETSTKLV")
   object PINVBC : ModuleType(47, "PINVBC", "Receiver Report", "receiver.report", "PINVBC")
   object PINORDRT : ModuleType(48, "PINORDRT", "Special Orders", "special.orders", "PINORDRT")
   object APGLRPT : ModuleType(50, "APGLRPT", "G/L Analysis", "gl.analysis", "APGLRPT")
   object ADDCOMP : ModuleType(51, "ADDCOMP", "Add a Company Record", "add.a.company.record", "ADDCOMP")
   object CHGCOMP : ModuleType(52, "CHGCOMP", "Change a Company Record", "change.a.company.record", "CHGCOMP")
   object DELCOMP : ModuleType(53, "DELCOMP", "Delete a Company Record", "delete.a.company.record", "DELCOMP")
   object LSTCOMP : ModuleType(54, "LSTCOMP", "List all Company Records", "list.all.company.records", "LSTCMP")
   object PRTCOMP : ModuleType(55, "PRTCOMP", "Print Company Report", "print.company.report", "PRTCOMP")
   object SHOCOMP : ModuleType(56, "SHOCOMP", "Show a Company Record", "show.a.company.record", "SHOCOMP")
   object SETSYS : ModuleType(57, "SETSYS", "Modify Company Areas", "modify.company.areas", "SETSYS")
   object POPARAMS : ModuleType(58, "POPARAMS", "PO Control", "po.control", "POPARAMS")
   object APLST : ModuleType(59, "APLST", "Vendor Invoices", "vendor.invoices", "APRPT")
   object ADDVEND : ModuleType(60, "ADDVEND", "Add a New Vendor", "add.a.new.vendor", "ADDVEND")
   object CHGVEND : ModuleType(61, "CHGVEND", "Change a Vendor", "change.a.vendor", "CHGVEND")
   object DELVEND : ModuleType(62, "DELVEND", "Delete a Vendor", "delete.a.vendor", "DELVEND")
   object LSTVEND : ModuleType(63, "LSTVEND", "List all Vendors", "list.all.vendors", "LSTVEND")
   object PRTVEND : ModuleType(64, "PRTVEND", "Print Vendor Report", "print.vendor.report", "PRTVEND")
   object DEFVEND : ModuleType(65, "DEFVEND", "Set Default Vendor Profile", "set.default.vendor.profile", "DEFVEND")
   object SHOVEND : ModuleType(66, "SHOVEND", "Show a Vendor", "show.a.vendor", "SHOVEND")
   object SHIPVIA : ModuleType(67, "SHIPVIA", "Enter/modify Ship Via", "enter.modify.ship.via", "SHIPVIA")
   object ADDVIA : ModuleType(68, "ADDVIA", "Add a Ship Via Code", "add.a.ship.via.code", "ADDVIA")
   object CHGVIA : ModuleType(69, "CHGVIA", "Change a Ship Via Code", "change.a.ship.via.code", "CHGVIA")
   object DELVIA : ModuleType(70, "DELVIA", "Delete a Ship Via Code", "delete.a.ship.via.code", "DELVIA")
   object PRTVIA : ModuleType(71, "PRTVIA", "Print Ship Via Code", "print.ship.via.code", "PRTVIA")
   object SHOVIA : ModuleType(72, "SHOVIA", "Show a Ship Via List", "show.a.ship.via.list", "SHOVIA")
   object APPURGE : ModuleType(74, "APPURGE", "Purge AP Records", "purge.ap.records", "APPURGE")
   object APPARAMS : ModuleType(75, "APPARAMS", "AP Control", "ap.control", "APPARAMS")
   object ADDACCT : ModuleType(76, "ADDACCT", "Add a G/L Account", "add.a.gl.account", "ADDACCT")
   object CHGACCT : ModuleType(77, "CHGACCT", "Change a G/L Account", "chg.a.gl.account", "CHGACCT")
   object DELACCT : ModuleType(78, "DELACCT", "Delete a G/L Account", "delete.a.gl.account", "DELACCT")
   object LSTACCT : ModuleType(79, "LSTACCT", "List all G/L Accounts", "list.all.gl.accounts", "LSTACCT")
   object PRTACCT : ModuleType(80, "PRTACCT", "Print Chart of Accounts", "print.chart.of.accounts", "PRTACCT")
   object CPYACCT : ModuleType(81, "CPYACCT", "Reproduce a G/L Account", "reproduce.a.gl.account", "CPYACCT")
   object SHOACCT : ModuleType(82, "SHOACCT", "Show a G/L Account", "show.a.gl.account", "SHOACCT")
   object ADDBANK : ModuleType(83, "ADDBANK", "Add a New Bank", "add.a.new.bank", "ADDBANK")
   object CHGBANK : ModuleType(84, "CHGBANK", "Change a Bank", "change.a.bank", "CHGBANK")
   object DELBANK : ModuleType(85, "DELBANK", "Delete a Bank", "delete.a.bank", "DELBANK")
   object LSTBANK : ModuleType(86, "LSTBANK", "List All Banks", "list.all.banks", "LSTBANK")
   object PRTBANK : ModuleType(87, "PRTBANK", "Print Bank Report", "print.bank.report", "PRTBANK")
   object SHOBANK : ModuleType(88, "SHOBANK", "Show a Bank", "show.a.bank", "SHOBANK")
   object GLPARAMS : ModuleType(89, "GLPARAMS", "GL Control", "gl.control", "GLPARAMS")
   object ADDLAY : ModuleType(90, "ADDLAY", "Add a Statement Layout", "add.a.statement.layout", "ADDLAY")
   object CHGLAY : ModuleType(91, "CHGLAY", "Change a Statement Layout", "change.a.statement.layout", "CHGLAY")
   object DELLAY : ModuleType(92, "DELLAY", "Delete a Statement Layout", "delete.a.statement.layout", "DELLAY")
   object FORMLAY : ModuleType(93, "FORMLAY", "Format a Sample Statement", "format.a.sample.statement", "FORMLAY")
   object PRTLAY : ModuleType(94, "PRTLAY", "Print a Statement Layout", "print.a.statement.layout", "PRTLAY")
   object CPYLAY : ModuleType(95, "CPYLAY", "Reproduce a Statement Layout", "reproduce.a.statement.layout", "CPYLAY")
   object SHOLAY : ModuleType(96, "SHOLAY", "Show a Statement Layout", "show.a.statement.layout", "SHOLAY")
   object ADDGLCOD : ModuleType(97, "ADDGLCOD", "Add a New G/L Code", "add.a.new.gl.code", "ADDGLCOD")
   object CHGGLCOD : ModuleType(98, "CHGGLCOD", "Change a G/L Code", "change.a.gl.code", "CHGGLCOD")
   object DELGLCOD : ModuleType(99, "DELGLCOD", "Delete a G/L Code", "delete.a.gl.code", "DELGLCOD")
   object LSTGLCOD : ModuleType(100, "LSTGLCOD", "List all G/L Codes", "list.all.gl.codes", "LSTGLCOD")
   object PRTGLCOD : ModuleType(101, "PRTGLCOD", "Print G/L Code Report", "print.gl.code.report", "PRTGLCOD")
   object SHOGLCOD : ModuleType(102, "SHOGLCOD", "Show a G/L Code", "show.a.gl.code", "SHOGLCOD")
   object ADDAPDST : ModuleType(103, "ADDAPDST", "Add a Distribution Template", "add.a.distribution.template", "ADDAPDST")
   object CHGAPDST : ModuleType(104, "CHGAPDST", "Change a Distribution Template", "change.a.distribution.template", "CHGAPDST")
   object DELAPDST : ModuleType(105, "DELAPDST", "Delete a Distribution Template", "delete.a.distribution.template", "DELAPDST")
   object SHOAPDST : ModuleType(106, "SHOAPDST", "Show a Distribution Template", "show.a.distribution.template", "SHOAPDST")
   object PRTAPDST : ModuleType(107, "PRTAPDST", "Print a Distribution Template", "print.a.distribution.template", "PRTAPDST")
}

sealed class AreaType(
   val id: Int,
   val value: String,
   val description: String,
   val localizationCode: String,
) : TypeDomain() {
   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}

object Unknown : AreaType(-1, "UNK", "Unknown Area", "unknown.area")
object AccountPayable : AreaType(1, "AP", "Account Payable", "account.payable")
object BankReconciliation : AreaType(2, "BR", "Bank Reconciliation", "bank.reconciliation")
object GeneralLedger : AreaType(3, "GL", "General Ledger", "general.ledger")
object PurchaseOrder : AreaType(4, "PO", "Purchase Order", "purchase.order")
object DarwillUpload : AreaType(5, "DARWILL", "Darwill Upload", "darwill.upload")
object SignatureCapture : AreaType(6, "SIGNATURE_CAPTURE", "Online Signature Capture", "signature.capture")
object WowUpload : AreaType(7, "WOW", "Wow Upload", "wow.upload")

@MappedEntity("area_type_domain")
class AreaTypeEntity(

   @field:Id
   @field:GeneratedValue
   val id: Int,
   val value: String,
   val description: String,
   val localizationCode: String,

) : TypeDomain() {

   constructor(areaType: AreaType) :
      this(
         id = areaType.id,
         value = areaType.value,
         description = areaType.description,
         localizationCode = areaType.localizationCode,
      )

   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}

fun AreaTypeEntity.toAreaType(): AreaType =
   when (this.id) {
      1 -> AccountPayable
      2 -> BankReconciliation
      3 -> GeneralLedger
      4 -> PurchaseOrder
      5 -> DarwillUpload
      else -> Unknown
   }

fun AreaType.toAreaTypeEntity(): AreaTypeEntity =
   AreaTypeEntity(this)

fun AreaType.toAreaEntity(company: CompanyEntity) =
   AreaEntity(
      areaType = this.toAreaTypeEntity(),
      company = company
   )
fun findAreaType(area: String): AreaType =
   when (area.uppercase().trim()) {
      "AP" -> AccountPayable
      "BR" -> BankReconciliation
      "GL" -> GeneralLedger
      "PO" -> PurchaseOrder
      "DARWILL" -> DarwillUpload
      "SIGNATURE_CAPTURE" -> SignatureCapture
      else -> Unknown
   }
