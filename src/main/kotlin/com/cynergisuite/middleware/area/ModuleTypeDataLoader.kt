package com.cynergisuite.middleware.area

import com.cynergisuite.middleware.area.infrastructure.ModuleRepository
import com.cynergisuite.middleware.company.Company
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object ModuleDataLoader {
   private val moduleTypes = listOf(

      ModuleType(
         id = 1,
         value = "APADD",
         program = "APADD",
         description = "Add Invoices",
         localizationCode = "add.invoices",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 2 }
      ),
      ModuleType(
         id = 2,
         value = "APSHO",
         program = "APSHO",
         description = "Show Invoices",
         localizationCode = "show.invoices",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 2 }
      ),
      ModuleType(
         id = 3,
         value = "APCHG",
         program = "APCHG",
         description = "AP Change Invoices",
         localizationCode = "change.invoices",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 2 }
      ),
      ModuleType(
         id = 4,
         value = "APDEL",
         program = "APDEL",
         description = "Delete Invoices",
         localizationCode = "delete.invoices",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 2 }
      ),
      ModuleType(
         id = 5,
         value = "APCHECK",
         program = "APCHECK",
         description = "Print Checks",
         localizationCode = "print.checks",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 4 }
      ),
      ModuleType(
         id = 6,
         value = "APCHKRPT",
         program = "APCHKRPT",
         description = "Check Report",
         localizationCode = "check.report",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 4 }
      ),
      ModuleType(
         id = 7,
         value = "APCLEAR",
         program = "APCLEAR",
         description = "Clear Checks",
         localizationCode = "clear.checks",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 4 }
      ),
      ModuleType(
         id = 8,
         value = "APSEL",
         program = "APSEL",
         description = "Select Invoices",
         localizationCode = "select.invoices",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 4 }
      ),
      ModuleType(
         id = 9,
         value = "APPREVUE",
         program = "APPREVUE",
         description = "Check Preview Rpt",
         localizationCode = "check.preview.rpt",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 4 }
      ),
      ModuleType(
         id = 10,
         value = "APVOID",
         program = "APVOID",
         description = "Void Checks",
         localizationCode = "void.checks",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 4 }
      ),
      ModuleType(
         id = 11,
         value = "APAGERPT",
         program = "APAGERPT",
         description = "Aging Report",
         localizationCode = "aging.report",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 5 }
      ),
      ModuleType(
         id = 12,
         value = "APRPT",
         program = "APRPT",
         description = "AP Report",
         localizationCode = "ap.report",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 5 }
      ),
      ModuleType(
         id = 13,
         value = "CASHOUT",
         program = "CASHOUT",
         description = "Cash Requirements",
         localizationCode = "cash.requirements",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 5 }
      ),
      ModuleType(
         id = 14,
         value = "APSTATUS",
         program = "APSTATUS",
         description = "Vendor Statistics",
         localizationCode = "vendor.statistics",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 20 }
      ),
      ModuleType(
         id = 15,
         value = "POADD",
         program = "POADD",
         description = "Add PO",
         localizationCode = "add.po",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 20 }
      ),
      ModuleType(
         id = 16,
         value = "POCHG",
         program = "POCHG",
         description = "Change PO",
         localizationCode = "change.po",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 20 }
      ),
      ModuleType(
         id = 17,
         value = "PODEL",
         program = "PODEL",
         description = "Delete PO",
         localizationCode = "delete.po",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 20 }
      ),
      ModuleType(
         id = 18,
         value = "POLST",
         program = "POLST",
         description = "List by PO",
         localizationCode = "list.by.po",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 20 }
      ),
      ModuleType(
         id = 19,
         value = "POPURGE",
         program = "POPURGE",
         description = "Purge PO Records",
         localizationCode = "purge.po.records",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 20 }
      ),
      ModuleType(
         id = 20,
         value = "POSHO",
         program = "POSHO",
         description = "Inquiry",
         localizationCode = "inquiry",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 20 }
      ),
      ModuleType(
         id = 21,
         value = "POINLOAD",
         program = "POINLOAD",
         description = "Receive From PO",
         localizationCode = "receive.from.po",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 20 }
      ),
      ModuleType(
         id = 22,
         value = "POUPDT",
         program = "POUPDT",
         description = "Update PO",
         localizationCode = "update.po",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 20 }
      ),
      ModuleType(
         id = 23,
         value = "SPOADD",
         program = "SPOADD",
         description = "Special Orders",
         localizationCode = "special.orders",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 20 }
      ),
      ModuleType(
         id = 24,
         value = "SPOLST",
         program = "SPOLST",
         description = "List Special Ord",
         localizationCode = "list.special.ord",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 20 }
      ),
      ModuleType(
         id = 25,
         value = "POCAN",
         program = "POCAN",
         description = "Cancel PO",
         localizationCode = "cancel.po",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 20 }
      ),
      ModuleType(
         id = 26,
         value = "POCOPY",
         program = "POCOPY",
         description = "Copy a PO",
         localizationCode = "copy.a.po",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 20 }
      ),
      ModuleType(
         id = 27,
         value = "INVORDMT",
         program = "INVORDMT",
         description = "Allocate/Inq Special Orders and POs",
         localizationCode = "allocate.inq.special.orders.and.pos",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 20 }
      ),
      ModuleType(
         id = 28,
         value = "INVCRED",
         program = "INVCRED",
         description = "Return Item for Credit to Vendor",
         localizationCode = "return.item.for.credit.to.vendor",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 20 }
      ),
      ModuleType(
         id = 29,
         value = "INVAVAIL",
         program = "INVAVAIL",
         description = "inventory.availability",
         localizationCode = "inventory.availability",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 20 }
      ),
      ModuleType(
         id = 30,
         value = "POSTAT",
         program = "POSTAT",
         description = "Change PO Status to Open",
         localizationCode = "change.po.status.to.open",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 20 }
      ),
      ModuleType(
         id = 31,
         value = "PODLST",
         program = "PODLST",
         description = "List by Items",
         localizationCode = "list.by.items",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 20 }
      ),
      ModuleType(
         id = 32,
         value = "POSTAT1",
         program = "POSTAT1",
         description = "Change PO Status",
         localizationCode = "change.po.status",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 20 }
      ),
      ModuleType(
         id = 33,
         value = "PODSQLST",
         program = "PODSQLST",
         description = "List PO by Sequence #",
         localizationCode = "list.po.by.sequence.#",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 20 }
      ),
      ModuleType(
         id = 34,
         value = "ITEMMNTS",
         program = "ITEMMNTS",
         description = "Model",
         localizationCode = "model.maintenance",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 20 }
      ),
      ModuleType(
         id = 35,
         value = "VENDOR",
         program = "VENDOR",
         description = "Vendor",
         localizationCode = "vendor.maintenance",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 20 }
      ),
      ModuleType(
         id = 36,
         value = "PODETCHG",
         program = "PODETCHG",
         description = "Adjust Receiving Quantities",
         localizationCode = "adjust.receiving.quantities",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 20 }
      ),
      ModuleType(
         id = 37,
         value = "POREC",
         program = "POREC",
         description = "Enter Receiving",
         localizationCode = "enter.receiving",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 21 }
      ),
      ModuleType(
         id = 38,
         value = "PORECLST",
         program = "PORECLST",
         description = "List Receiving",
         localizationCode = "list.receiving",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 21 }
      ),
      ModuleType(
         id = 39,
         value = "PORECRPT",
         program = "PORECRPT",
         description = "Receiving Rpt",
         localizationCode = "receiving.rpt",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 21 }
      ),
      ModuleType(
         id = 40,
         value = "PORPT",
         program = "PORPT",
         description = "PO Report",
         localizationCode = "po.report",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 21 }
      ),
      ModuleType(
         id = 41,
         value = "POWRKSHT",
         program = "POWRKSHT",
         description = "Receiving Worksheet",
         localizationCode = "receiving.worksheet",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 21 }
      ),
      ModuleType(
         id = 42,
         value = "QUOTERPT",
         program = "QUOTERPT",
         description = "Quote Report",
         localizationCode = "quote.report",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 21 }
      ),
      ModuleType(
         id = 43,
         value = "VDRQUOTE",
         program = "VDRQUOTE",
         description = "Vendor Quotes",
         localizationCode = "vendor.quotes",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 21 }
      ),
      ModuleType(
         id = 44,
         value = "SPOPRT",
         program = "SPOPRT",
         description = "Prt Special Ord",
         localizationCode = "prt.special.ord",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 21 }
      ),
      ModuleType(
         id = 45,
         value = "STKRERDR",
         program = "STKRERDR",
         description = "Stock Reorder",
         localizationCode = "stock.reorder",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 21 }
      ),
      ModuleType(
         id = 46,
         value = "GETSTKLV",
         program = "GETSTKLV",
         description = "Update Stock Reorder Control File",
         localizationCode = "update.stock.reorder.control.file",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 23 }
      ),
      ModuleType(
         id = 47,
         value = "PINVBC",
         program = "PINVBC",
         description = "Receiver Report",
         localizationCode = "receiver.report",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 21 }
      ),
      ModuleType(
         id = 48,
         value = "PINORDRT",
         program = "PINORDRT",
         description = "Special Orders",
         localizationCode = "special.orders",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 21 }
      ),
      ModuleType(
         id = 50,
         value = "APGLRPT",
         program = "APGLRPT",
         description = "G/L Analysis",
         localizationCode = "gl.analysis",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 14 }
      ),
      ModuleType(
         id = 51,
         value = "ADDCOMP",
         program = "ADDCOMP",
         description = "Add a Company Record",
         localizationCode = "add.a.company.record",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 38 }
      ),
      ModuleType(
         id = 52,
         value = "CHGCOMP",
         program = "CHGCOMP",
         description = "Change a Company Record",
         localizationCode = "change.a.company.record",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 38 }
      ),
      ModuleType(
         id = 53,
         value = "DELCOMP",
         program = "DELCOMP",
         description = "Delete a Company Record",
         localizationCode = "delete.a.company.record",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 38 }
      ),
      ModuleType(
         id = 54,
         value = "LSTCOMP",
         program = "LSTCMP",
         description = "List all Company Records",
         localizationCode = "list.all.company.records",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 38 }
      ),
      ModuleType(
         id = 55,
         value = "PRTCOMP",
         program = "PRTCOMP",
         description = "Print Company Report",
         localizationCode = "print.company.report",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 38 }
      ),
      ModuleType(
         id = 56,
         value = "SHOCOMP",
         program = "SHOCOMP",
         description = "Show a Company Record",
         localizationCode = "show.a.company.record",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 38 }
      ),
      ModuleType(
         id = 57,
         value = "SETSYS",
         program = "SETSYS",
         description = "Modify Company Areas",
         localizationCode = "modify.company.areas",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 38 }
      ),
      ModuleType(
         id = 58,
         value = "POPARAMS",
         program = "POPARAMS",
         description = "PO Control",
         localizationCode = "po.control",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 41 }
      ),
      ModuleType(
         id = 59,
         value = "APLST",
         program = "APRPT",
         description = "Vendor Invoices",
         localizationCode = "vendor.invoices",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 35 }
      ),
      ModuleType(
         id = 60,
         value = "ADDVEND",
         program = "ADDVEND",
         description = "Add a New Vendor",
         localizationCode = "add.a.new.vendor",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 35 }
      ),
      ModuleType(
         id = 61,
         value = "CHGVEND",
         program = "CHGVEND",
         description = "Change a Vendor",
         localizationCode = "change.a.vendor",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 35 }
      ),
      ModuleType(
         id = 62,
         value = "DELVEND",
         program = "DELVEND",
         description = "Delete a Vendor",
         localizationCode = "delete.a.vendor",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 35 }
      ),
      ModuleType(
         id = 63,
         value = "LSTVEND",
         program = "LSTVEND",
         description = "List all Vendors",
         localizationCode = "list.all.vendors",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 35 }
      ),
      ModuleType(
         id = 64,
         value = "PRTVEND",
         program = "PRTVEND",
         description = "Print Vendor Report",
         localizationCode = "print.vendor.report",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 35 }
      ),
      ModuleType(
         id = 65,
         value = "DEFVEND",
         program = "DEFVEND",
         description = "Set Default Vendor Profile",
         localizationCode = "set.default.vendor.profile",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 35 }
      ),
      ModuleType(
         id = 66,
         value = "SHOVEND",
         program = "SHOVEND",
         description = "Show a Vendor",
         localizationCode = "show.a.vendor",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 35 }
      ),
      ModuleType(
         id = 67,
         value = "SHIPVIA",
         program = "SHIPVIA",
         description = "Enter/modify Ship Via",
         localizationCode = "enter.modify.ship.via",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 34 }
      ),
      ModuleType(
         id = 68,
         value = "ADDVIA",
         program = "ADDVIA",
         description = "Add a Ship Via Code",
         localizationCode = "add.a.ship.via.code",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 34 }
      ),
      ModuleType(
         id = 69,
         value = "CHGVIA",
         program = "CHGVIA",
         description = "Change a Ship Via Code",
         localizationCode = "change.a.ship.via.code",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 34 }
      ),
      ModuleType(
         id = 70,
         value = "DELVIA",
         program = "DELVIA",
         description = "Delete a Ship Via Code",
         localizationCode = "delete.a.ship.via.code",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 34 }
      ),
      ModuleType(
         id = 71,
         value = "PRTVIA",
         program = "PRTVIA",
         description = "Print Ship Via Code",
         localizationCode = "print.ship.via.code",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 34 }
      ),
      ModuleType(
         id = 72,
         value = "SHOVIA",
         program = "SHOVIA",
         description = "Show a Ship Via List",
         localizationCode = "show.a.ship.via.list",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 34 }
      ),
      ModuleType(
         id = 73,
         value = "GETVTERM",
         program = "GETVTERM",
         description = "Vendor Term Code",
         localizationCode = "vendor.term.code",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 39 }
      ),
      ModuleType(
         id = 74,
         value = "APPURGE",
         program = "APPURGE",
         description = "Purge AP Records",
         localizationCode = "purge.ap.records",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 40 }
      ),
      ModuleType(
         id = 75,
         value = "APPARAMS",
         program = "APPARAMS",
         description = "AP Control",
         localizationCode = "ap.control",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 40 }
      ),
      ModuleType(
         id = 76,
         value = "ADDACCT",
         program = "ADDACCT",
         description = "Add a G/L Account",
         localizationCode = "add.a.gl.account",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 30 }
      ),
      ModuleType(
         id = 77,
         value = "CHGACCT",
         program = "CHGACCT",
         description = "Change a G/L Account",
         localizationCode = "chg.a.gl.account",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 30 }
      ),
      ModuleType(
         id = 78,
         value = "DELACCT",
         program = "DELACCT",
         description = "Delete a G/L Account",
         localizationCode = "delete.a.gl.account",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 30 }
      ),
      ModuleType(
         id = 79,
         value = "LSTACCT",
         program = "LSTACCT",
         description = "List all G/L Accounts",
         localizationCode = "list.all.gl.accounts",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 30 }
      ),
      ModuleType(
         id = 80,
         value = "PRTACCT",
         program = "PRTACCT",
         description = "Print Chart of Accounts",
         localizationCode = "print.chart.of.accounts",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 30 }
      ),
      ModuleType(
         id = 81,
         value = "CPYACCT",
         program = "CPYACCT",
         description = "Reproduce a G/L Account",
         localizationCode = "reproduce.a.gl.account",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 30 }
      ),
      ModuleType(
         id = 82,
         value = "SHOACCT",
         program = "SHOACCT",
         description = "Show a G/L Account",
         localizationCode = "show.a.gl.account",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 30 }
      ),
      ModuleType(
         id = 83,
         value = "ADDBANK",
         program = "ADDBANK",
         description = "Add a New Bank",
         localizationCode = "add.a.new.bank",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 29 }
      ),
      ModuleType(
         id = 84,
         value = "CHGBANK",
         program = "CHGBANK",
         description = "Change a Bank",
         localizationCode = "change.a.bank",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 29 }
      ),
      ModuleType(
         id = 85,
         value = "DELBANK",
         program = "DELBANK",
         description = "Delete a Bank",
         localizationCode = "delete.a.bank",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 29 }
      ),
      ModuleType(
         id = 86,
         value = "LSTBANK",
         program = "LSTBANK",
         description = "List All Banks",
         localizationCode = "list.all.banks",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 29 }
      ),
      ModuleType(
         id = 87,
         value = "PRTBANK",
         program = "PRTBANK",
         description = "Print Bank Report",
         localizationCode = "print.bank.report",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 29 }
      ),
      ModuleType(
         id = 88,
         value = "SHOBANK",
         program = "SHOBANK",
         description = "Show a Bank",
         localizationCode = "show.a.bank",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 29 }
      ),
      ModuleType(
         id = 89,
         value = "GLPARAMS",
         program = "GLPARAMS",
         description = "GL Control",
         localizationCode = "gl.control",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 42 }
      ),
      ModuleType(
         id = 90,
         value = "ADDLAY",
         program = "ADDLAY",
         description = "Add a Statement Layout",
         localizationCode = "add.a.statement.layout",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 32 }
      ),
      ModuleType(
         id = 91,
         value = "CHGLAY",
         program = "CHGLAY",
         description = "Change a Statement Layout",
         localizationCode = "change.a.statement.layout",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 32 }
      ),
      ModuleType(
         id = 92,
         value = "DELLAY",
         program = "DELLAY",
         description = "Delete a Statement Layout",
         localizationCode = "delete.a.statement.layout",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 32 }
      ),
      ModuleType(
         id = 93,
         value = "FORMLAY",
         program = "FORMLAY",
         description = "Format a Sample Statement",
         localizationCode = "format.a.sample.statement",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 32 }
      ),
      ModuleType(
         id = 94,
         value = "PRTLAY",
         program = "PRTLAY",
         description = "Print a Statement Layout",
         localizationCode = "print.a.statement.layout",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 32 }
      ),
      ModuleType(
         id = 95,
         value = "CPYLAY",
         program = "CPYLAY",
         description = "Reproduce a Statement Layout",
         localizationCode = "reproduce.a.statement.layout",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 32 }
      ),
      ModuleType(
         id = 96,
         value = "SHOLAY",
         program = "SHOLAY",
         description = "Show a Statement Layout",
         localizationCode = "show.a.statement.layout",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 32 }
      ),
      ModuleType(
         id = 97,
         value = "ADDGLCOD",
         program = "ADDGLCOD",
         description = "Add a New G/L Code",
         localizationCode = "add.a.new.gl.code",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 33 }
      ),
      ModuleType(
         id = 98,
         value = "CHGGLCOD",
         program = "CHGGLCOD",
         description = "Change a G/L Code",
         localizationCode = "change.a.gl.code",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 33 }
      ),
      ModuleType(
         id = 99,
         value = "DELGLCOD",
         program = "DELGLCOD",
         description = "Delete a G/L Code",
         localizationCode = "delete.a.gl.code",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 33 }
      ),
      ModuleType(
         id = 100,
         value = "LSTGLCOD",
         program = "LSTGLCOD",
         description = "List all G/L Codes",
         localizationCode = "list.all.gl.codes",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 33 }
      ),
      ModuleType(
         id = 101,
         value = "PRTGLCOD",
         program = "PRTGLCOD",
         description = "Print G/L Code Report",
         localizationCode = "print.gl.code.report",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 33 }
      ),
      ModuleType(
         id = 102,
         value = "SHOGLCOD",
         program = "SHOGLCOD",
         description = "Show a G/L Code",
         localizationCode = "show.a.gl.code",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 33 }
      ),
      ModuleType(
         id = 103,
         value = "ADDAPDST",
         program = "ADDAPDST",
         description = "Add a Distribution Template",
         localizationCode = "add.a.distribution.template",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 31 }
      ),
      ModuleType(
         id = 104,
         value = "CHGAPDST",
         program = "CHGAPDST",
         description = "Change a Distribution Template",
         localizationCode = "change.a.distribution.template",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 31 }
      ),
      ModuleType(
         id = 105,
         value = "DELAPDST",
         program = "DELAPDST",
         description = "Delete a Distribution Template",
         localizationCode = "delete.a.distribution.template",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 31 }
      ),
      ModuleType(
         id = 106,
         value = "SHOAPDST",
         program = "SHOAPDST",
         description = "Show a Distribution Template",
         localizationCode = "show.a.distribution.template",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 31 }
      ),
      ModuleType(
         id = 107,
         value = "PRTAPDST",
         program = "PRTAPDST",
         description = "Print a Distribution Template",
         localizationCode = "print.a.distribution.template",
         menuType = MenuTypeDataLoader.menuTypes().first { it.id == 31 }
      ),
   )

   @JvmStatic
   fun moduleTypes(): List<ModuleType> = moduleTypes

   @JvmStatic
   fun random(): ModuleType = moduleTypes.random()

   fun singleDTO(moduleTypeId: Int, level: Int) =
      ModuleDTO(
         id = moduleTypeId,
         level = level
      )
}

@Singleton
@Requires(env = ["develop", "test"])
class ModuleDataLoaderService(
   private val repository: ModuleRepository
) {
   fun predefined() = ModuleDataLoader.moduleTypes()

   fun configureLevel(id: Int, level: Int, company: Company) {
      repository.insertConfig(ModuleDataLoader.moduleTypes().first { it.id == id }.copy(level = level), company)
   }

   fun singleDTO(moduleTypeId: Int, level: Int) = ModuleDataLoader.singleDTO(moduleTypeId, level)
}
