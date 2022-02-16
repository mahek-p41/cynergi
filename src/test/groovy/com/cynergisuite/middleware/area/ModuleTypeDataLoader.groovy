package com.cynergisuite.middleware.area

import com.cynergisuite.middleware.area.infrastructure.ModuleRepository
import com.cynergisuite.middleware.company.CompanyEntity
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import jakarta.inject.Inject
import jakarta.inject.Singleton

class ModuleDataLoader {
   private static final List<ModuleTypeEntity> moduleTypes = [
      new ModuleTypeEntity(
         1,
         "APADD",
         "APADD",
         "Add Invoices",
         "add.invoices",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 2 }
      ),
      new ModuleTypeEntity(
         2,
         "APSHO",
         "APSHO",
         "Show Invoices",
         "show.invoices",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 2 }
      ),
      new ModuleTypeEntity(
         3,
         "APCHG",
         "APCHG",
         "AP Change Invoices",
         "change.invoices",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 2 }
      ),
      new ModuleTypeEntity(
         4,
         "APDEL",
         "APDEL",
         "Delete Invoices",
         "delete.invoices",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 2 }
      ),
      new ModuleTypeEntity(
         5,
         "APCHECK",
         "APCHECK",
         "Print Checks",
         "print.checks",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 4 }
      ),
      new ModuleTypeEntity(
         6,
         "APCHKRPT",
         "APCHKRPT",
         "Check Report",
         "check.report",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 4 }
      ),
      new ModuleTypeEntity(
         7,
         "APCLEAR",
         "APCLEAR",
         "Clear Checks",
         "clear.checks",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 4 }
      ),
      new ModuleTypeEntity(
         8,
         "APSEL",
         "APSEL",
         "Select Invoices",
         "select.invoices",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 4 }
      ),
      new ModuleTypeEntity(
         9,
         "APPREVUE",
         "APPREVUE",
         "Check Preview Rpt",
         "check.preview.rpt",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 4 }
      ),
      new ModuleTypeEntity(
         10,
         "APVOID",
         "APVOID",
         "Void Checks",
         "void.checks",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 4 }
      ),
      new ModuleTypeEntity(
         11,
         "APAGERPT",
         "APAGERPT",
         "Aging Report",
         "aging.report",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 5 }
      ),
      new ModuleTypeEntity(
         12,
         "APRPT",
         "APRPT",
         "AP Report",
         "ap.report",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 5 }
      ),
      new ModuleTypeEntity(
         13,
         "CASHOUT",
         "CASHOUT",
         "Cash Requirements",
         "cash.requirements",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 5 }
      ),
      new ModuleTypeEntity(
         14,
         "APSTATUS",
         "APSTATUS",
         "Vendor Statistics",
         "vendor.statistics",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 20 }
      ),
      new ModuleTypeEntity(
         15,
         "POADD",
         "POADD",
         "Add PO",
         "add.po",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 20 }
      ),
      new ModuleTypeEntity(
         16,
         "POCHG",
         "POCHG",
         "Change PO",
         "change.po",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 20 }
      ),
      new ModuleTypeEntity(
         17,
         "PODEL",
         "PODEL",
         "Delete PO",
         "delete.po",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 20 }
      ),
      new ModuleTypeEntity(
         18,
         "POLST",
         "POLST",
         "List by PO",
         "list.by.po",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 20 }
      ),
      new ModuleTypeEntity(
         19,
         "POPURGE",
         "POPURGE",
         "Purge PO Records",
         "purge.po.records",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 20 }
      ),
      new ModuleTypeEntity(
         20,
         "POSHO",
         "POSHO",
         "Inquiry",
         "inquiry",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 20 }
      ),
      new ModuleTypeEntity(
         21,
         "POINLOAD",
         "POINLOAD",
         "Receive From PO",
         "receive.from.po",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 20 }
      ),
      new ModuleTypeEntity(
         22,
         "POUPDT",
         "POUPDT",
         "Update PO",
         "update.po",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 20 }
      ),
      new ModuleTypeEntity(
         23,
         "SPOADD",
         "SPOADD",
         "Special Orders",
         "special.orders",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 20 }
      ),
      new ModuleTypeEntity(
         24,
         "SPOLST",
         "SPOLST",
         "List Special Ord",
         "list.special.ord",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 20 }
      ),
      new ModuleTypeEntity(
         25,
         "POCAN",
         "POCAN",
         "Cancel PO",
         "cancel.po",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 20 }
      ),
      new ModuleTypeEntity(
         26,
         "POCOPY",
         "POCOPY",
         "Copy a PO",
         "copy.a.po",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 20 }
      ),
      new ModuleTypeEntity(
         27,
         "INVORDMT",
         "INVORDMT",
         "Allocate/Inq Special Orders and POs",
         "allocate.inq.special.orders.and.pos",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 20 }
      ),
      new ModuleTypeEntity(
         28,
         "INVCRED",
         "INVCRED",
         "Return Item for Credit to Vendor",
         "return.item.for.credit.to.vendor",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 20 }
      ),
      new ModuleTypeEntity(
         29,
         "INVAVAIL",
         "INVAVAIL",
         "inventory.availability",
         "inventory.availability",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 20 }
      ),
      new ModuleTypeEntity(
         30,
         "POSTAT",
         "POSTAT",
         "Change PO Status to Open",
         "change.po.status.to.open",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 20 }
      ),
      new ModuleTypeEntity(
         31,
         "PODLST",
         "PODLST",
         "List by Items",
         "list.by.items",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 20 }
      ),
      new ModuleTypeEntity(
         32,
         "POSTAT1",
         "POSTAT1",
         "Change PO Status",
         "change.po.status",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 20 }
      ),
      new ModuleTypeEntity(
         33,
         "PODSQLST",
         "PODSQLST",
         "List PO by Sequence #",
         "list.po.by.sequence.#",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 20 }
      ),
      new ModuleTypeEntity(
         34,
         "ITEMMNTS",
         "ITEMMNTS",
         "Model",
         "model.maintenance",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 20 }
      ),
      new ModuleTypeEntity(
         35,
         "VENDOR",
         "VENDOR",
         "Vendor",
         "vendor.maintenance",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 20 }
      ),
      new ModuleTypeEntity(
         36,
         "PODETCHG",
         "PODETCHG",
         "Adjust Receiving Quantities",
         "adjust.receiving.quantities",
   null,
         MenuTypeDataLoader.menuTypes().find { it.id == 20 }
      ),
      new ModuleTypeEntity(
         37,
         "POREC",
         "POREC",
         "Enter Receiving",
         "enter.receiving",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 21 }
      ),
      new ModuleTypeEntity(
         38,
         "PORECLST",
         "PORECLST",
         "List Receiving",
         "list.receiving",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 21 }
      ),
      new ModuleTypeEntity(
         39,
         "PORECRPT",
         "PORECRPT",
         "Receiving Rpt",
         "receiving.rpt",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 21 }
      ),
      new ModuleTypeEntity(
         40,
         "PORPT",
         "PORPT",
         "PO Report",
         "po.report",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 21 }
      ),
      new ModuleTypeEntity(
         41,
         "POWRKSHT",
         "POWRKSHT",
         "Receiving Worksheet",
         "receiving.worksheet",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 21 }
      ),
      new ModuleTypeEntity(
         42,
         "QUOTERPT",
         "QUOTERPT",
         "Quote Report",
         "quote.report",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 21 }
      ),
      new ModuleTypeEntity(
         43,
         "VDRQUOTE",
         "VDRQUOTE",
         "Vendor Quotes",
         "vendor.quotes",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 21 }
      ),
      new ModuleTypeEntity(
         44,
         "SPOPRT",
         "SPOPRT",
         "Prt Special Ord",
         "prt.special.ord",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 21 }
      ),
      new ModuleTypeEntity(
         45,
         "STKRERDR",
         "STKRERDR",
         "Stock Reorder",
         "stock.reorder",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 21 }
      ),
      new ModuleTypeEntity(
         46,
         "GETSTKLV",
         "GETSTKLV",
         "Update Stock Reorder Control File",
         "update.stock.reorder.control.file",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 23 }
      ),
      new ModuleTypeEntity(
         47,
         "PINVBC",
         "PINVBC",
         "Receiver Report",
         "receiver.report",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 21 }
      ),
      new ModuleTypeEntity(
         48,
         "PINORDRT",
         "PINORDRT",
         "Special Orders",
         "special.orders",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 21 }
      ),
      new ModuleTypeEntity(
         50,
         "APGLRPT",
         "APGLRPT",
         "G/L Analysis",
         "gl.analysis",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 14 }
      ),
      new ModuleTypeEntity(
         51,
         "ADDCOMP",
         "ADDCOMP",
         "Add a Company Record",
         "add.a.company.record",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 38 }
      ),
      new ModuleTypeEntity(
         52,
         "CHGCOMP",
         "CHGCOMP",
         "Change a Company Record",
         "change.a.company.record",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 38 }
      ),
      new ModuleTypeEntity(
         53,
         "DELCOMP",
         "DELCOMP",
         "Delete a Company Record",
         "delete.a.company.record",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 38 }
      ),
      new ModuleTypeEntity(
         54,
         "LSTCOMP",
         "LSTCMP",
         "List all Company Records",
         "list.all.company.records",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 38 }
      ),
      new ModuleTypeEntity(
         55,
         "PRTCOMP",
         "PRTCOMP",
         "Print Company Report",
         "print.company.report",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 38 }
      ),
      new ModuleTypeEntity(
         56,
         "SHOCOMP",
         "SHOCOMP",
         "Show a Company Record",
         "show.a.company.record",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 38 }
      ),
      new ModuleTypeEntity(
         57,
         "SETSYS",
         "SETSYS",
         "Modify Company Areas",
         "modify.company.areas",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 38 }
      ),
      new ModuleTypeEntity(
         58,
         "POPARAMS",
         "POPARAMS",
         "PO Control",
         "po.control",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 41 }
      ),
      new ModuleTypeEntity(
         59,
         "APLST",
         "APRPT",
         "Vendor Invoices",
         "vendor.invoices",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 35 }
      ),
      new ModuleTypeEntity(
         60,
         "ADDVEND",
         "ADDVEND",
         "Add a New Vendor",
         "add.a.new.vendor",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 35 }
      ),
      new ModuleTypeEntity(
         61,
         "CHGVEND",
         "CHGVEND",
         "Change a Vendor",
         "change.a.vendor",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 35 }
      ),
      new ModuleTypeEntity(
         62,
         "DELVEND",
         "DELVEND",
         "Delete a Vendor",
         "delete.a.vendor",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 35 }
      ),
      new ModuleTypeEntity(
         63,
         "LSTVEND",
         "LSTVEND",
         "List all Vendors",
         "list.all.vendors",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 35 }
      ),
      new ModuleTypeEntity(
         64,
         "PRTVEND",
         "PRTVEND",
         "Print Vendor Report",
         "print.vendor.report",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 35 }
      ),
      new ModuleTypeEntity(
         65,
         "DEFVEND",
         "DEFVEND",
         "Set Default Vendor Profile",
         "set.default.vendor.profile",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 35 }
      ),
      new ModuleTypeEntity(
         66,
         "SHOVEND",
         "SHOVEND",
         "Show a Vendor",
         "show.a.vendor",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 35 }
      ),
      new ModuleTypeEntity(
         67,
         "SHIPVIA",
         "SHIPVIA",
         "Enter/modify Ship Via",
         "enter.modify.ship.via",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 34 }
      ),
      new ModuleTypeEntity(
         68,
         "ADDVIA",
         "ADDVIA",
         "Add a Ship Via Code",
         "add.a.ship.via.code",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 34 }
      ),
      new ModuleTypeEntity(
         69,
         "CHGVIA",
         "CHGVIA",
         "Change a Ship Via Code",
         "change.a.ship.via.code",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 34 }
      ),
      new ModuleTypeEntity(
         70,
         "DELVIA",
         "DELVIA",
         "Delete a Ship Via Code",
         "delete.a.ship.via.code",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 34 }
      ),
      new ModuleTypeEntity(
         71,
         "PRTVIA",
         "PRTVIA",
         "Print Ship Via Code",
         "print.ship.via.code",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 34 }
      ),
      new ModuleTypeEntity(
         72,
         "SHOVIA",
         "SHOVIA",
         "Show a Ship Via List",
         "show.a.ship.via.list",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 34 }
      ),
      new ModuleTypeEntity(
         73,
         "GETVTERM",
         "GETVTERM",
         "Vendor Term Code",
         "vendor.term.code",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 39 }
      ),
      new ModuleTypeEntity(
         74,
         "APPURGE",
         "APPURGE",
         "Purge AP Records",
         "purge.ap.records",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 40 }
      ),
      new ModuleTypeEntity(
         75,
         "APPARAMS",
         "APPARAMS",
         "AP Control",
         "ap.control",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 40 }
      ),
      new ModuleTypeEntity(
         76,
         "ADDACCT",
         "ADDACCT",
         "Add a G/L Account",
         "add.a.gl.account",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 30 }
      ),
      new ModuleTypeEntity(
         77,
         "CHGACCT",
         "CHGACCT",
         "Change a G/L Account",
         "chg.a.gl.account",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 30 }
      ),
      new ModuleTypeEntity(
         78,
         "DELACCT",
         "DELACCT",
         "Delete a G/L Account",
         "delete.a.gl.account",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 30 }
      ),
      new ModuleTypeEntity(
         79,
         "LSTACCT",
         "LSTACCT",
         "List all G/L Accounts",
         "list.all.gl.accounts",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 30 }
      ),
      new ModuleTypeEntity(
         80,
         "PRTACCT",
         "PRTACCT",
         "Print Chart of Accounts",
         "print.chart.of.accounts",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 30 }
      ),
      new ModuleTypeEntity(
         81,
         "CPYACCT",
         "CPYACCT",
         "Reproduce a G/L Account",
         "reproduce.a.gl.account",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 30 }
      ),
      new ModuleTypeEntity(
         82,
         "SHOACCT",
         "SHOACCT",
         "Show a G/L Account",
         "show.a.gl.account",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 30 }
      ),
      new ModuleTypeEntity(
         83,
         "ADDBANK",
         "ADDBANK",
         "Add a New Bank",
         "add.a.new.bank",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 29 }
      ),
      new ModuleTypeEntity(
         84,
         "CHGBANK",
         "CHGBANK",
         "Change a Bank",
         "change.a.bank",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 29 }
      ),
      new ModuleTypeEntity(
         85,
         "DELBANK",
         "DELBANK",
         "Delete a Bank",
         "delete.a.bank",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 29 }
      ),
      new ModuleTypeEntity(
         86,
         "LSTBANK",
         "LSTBANK",
         "List All Banks",
         "list.all.banks",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 29 }
      ),
      new ModuleTypeEntity(
         87,
         "PRTBANK",
         "PRTBANK",
         "Print Bank Report",
         "print.bank.report",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 29 }
      ),
      new ModuleTypeEntity(
         88,
         "SHOBANK",
         "SHOBANK",
         "Show a Bank",
         "show.a.bank",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 29 }
      ),
      new ModuleTypeEntity(
         89,
         "GLPARAMS",
         "GLPARAMS",
         "GL Control",
         "gl.control",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 42 }
      ),
      new ModuleTypeEntity(
         90,
         "ADDLAY",
         "ADDLAY",
         "Add a Statement Layout",
         "add.a.statement.layout",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 32 }
      ),
      new ModuleTypeEntity(
         91,
         "CHGLAY",
         "CHGLAY",
         "Change a Statement Layout",
         "change.a.statement.layout",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 32 }
      ),
      new ModuleTypeEntity(
         92,
         "DELLAY",
         "DELLAY",
         "Delete a Statement Layout",
         "delete.a.statement.layout",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 32 }
      ),
      new ModuleTypeEntity(
         93,
         "FORMLAY",
         "FORMLAY",
         "Format a Sample Statement",
         "format.a.sample.statement",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 32 }
      ),
      new ModuleTypeEntity(
         94,
         "PRTLAY",
         "PRTLAY",
         "Print a Statement Layout",
         "print.a.statement.layout",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 32 }
      ),
      new ModuleTypeEntity(
         95,
         "CPYLAY",
         "CPYLAY",
         "Reproduce a Statement Layout",
         "reproduce.a.statement.layout",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 32 }
      ),
      new ModuleTypeEntity(
         96,
         "SHOLAY",
         "SHOLAY",
         "Show a Statement Layout",
         "show.a.statement.layout",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 32 }
      ),
      new ModuleTypeEntity(
         97,
         "ADDGLCOD",
         "ADDGLCOD",
         "Add a New G/L Code",
         "add.a.new.gl.code",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 33 }
      ),
      new ModuleTypeEntity(
         98,
         "CHGGLCOD",
         "CHGGLCOD",
         "Change a G/L Code",
         "change.a.gl.code",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 33 }
      ),
      new ModuleTypeEntity(
         99,
         "DELGLCOD",
         "DELGLCOD",
         "Delete a G/L Code",
         "delete.a.gl.code",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 33 }
      ),
      new ModuleTypeEntity(
         100,
         "LSTGLCOD",
         "LSTGLCOD",
         "List all G/L Codes",
         "list.all.gl.codes",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 33 }
      ),
      new ModuleTypeEntity(
         101,
         "PRTGLCOD",
         "PRTGLCOD",
         "Print G/L Code Report",
         "print.gl.code.report",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 33 }
      ),
      new ModuleTypeEntity(
         102,
         "SHOGLCOD",
         "SHOGLCOD",
         "Show a G/L Code",
         "show.a.gl.code",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 33 }
      ),
      new ModuleTypeEntity(
         103,
         "ADDAPDST",
         "ADDAPDST",
         "Add a Distribution Template",
         "add.a.distribution.template",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 31 }
      ),
      new ModuleTypeEntity(
         104,
         "CHGAPDST",
         "CHGAPDST",
         "Change a Distribution Template",
         "change.a.distribution.template",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 31 }
      ),
      new ModuleTypeEntity(
         105,
         "DELAPDST",
         "DELAPDST",
         "Delete a Distribution Template",
         "delete.a.distribution.template",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 31 }
      ),
      new ModuleTypeEntity(
         106,
         "SHOAPDST",
         "SHOAPDST",
         "Show a Distribution Template",
         "show.a.distribution.template",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 31 }
      ),
      new ModuleTypeEntity(
         107,
         "PRTAPDST",
         "PRTAPDST",
         "Print a Distribution Template",
         "print.a.distribution.template",
         null,
         MenuTypeDataLoader.menuTypes().find { it.id == 31 }
      ),
   ]

   static List<ModuleTypeEntity> moduleTypes() { moduleTypes }

   static ModuleTypeEntity random() { moduleTypes.random() }

   static def singleDTO(int moduleTypeId, int level) {
      new ModuleDTO([
         'id': moduleTypeId,
         'level': level
      ])
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class ModuleDataLoaderService {
   private final ModuleRepository repository

   @Inject
   ModuleDataLoaderService(ModuleRepository repository) {
      this.repository = repository
   }

   def predefined() { ModuleDataLoader.moduleTypes() }

   def configureLevel(int id, int level, CompanyEntity company) {
      repository.insertConfig(ModuleDataLoader.moduleTypes().find { it.id == id }.copyMeWithNewLevel(level), company)
   }

   def singleDTO(int moduleTypeId, int level) { ModuleDataLoader.singleDTO(moduleTypeId, level) }
}
