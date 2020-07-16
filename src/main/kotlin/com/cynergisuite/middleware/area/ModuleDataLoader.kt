package com.cynergisuite.middleware.area

import com.cynergisuite.extensions.forId
import com.cynergisuite.middleware.area.infrastructure.ModuleRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyFactory
import io.micronaut.context.annotation.Requires
import java.util.stream.Stream
import javax.inject.Singleton

object ModuleDataLoader {
   private val moduleTypes = listOf(
      ModuleType(
         id = 1,
         value = "APADD",
         program = "AP",
         description = "Add Invoices",
         localizationCode = "add.invoices",
         menuType = MenuTypeDataLoader.menuTypes().forId(5)
      ),
      ModuleType(
         id = 2,
         value = "APAGERPT",
         program = "APAGERPT",
         description = "Aging Report",
         localizationCode = "aging.report",
         menuType = MenuTypeDataLoader.menuTypes().forId(1)
      ),
      ModuleType(
         id = 3,
         value = "APCHECK",
         program = "APCHECK",
         description = "Print Checks",
         localizationCode = "print.checks",
         menuType = MenuTypeDataLoader.menuTypes().forId(6)
      ),
      ModuleType(
         id = 4,
         value = "APCHG",
         program = "AP",
         description = "AP Change Invoices",
         localizationCode = "change.invoices",
         menuType = MenuTypeDataLoader.menuTypes().forId(5)
      ),
      ModuleType(
         id = 5,
         value = "APCHKRPT",
         program = "APCHKRPT",
         description = "Check Report",
         localizationCode = "check.report",
         menuType = MenuTypeDataLoader.menuTypes().forId(6)
      ),
      ModuleType(
         id = 6,
         value = "APCLEAR",
         program = "APCLEAR",
         description = "Clear Checks",
         localizationCode = "clear.checks",
         menuType = MenuTypeDataLoader.menuTypes().forId(6)
      ),
      ModuleType(
         id = 7,
         value = "APDEL",
         program = "AP",
         description = "Delete Invoices",
         localizationCode = "delete.invoices",
         menuType = MenuTypeDataLoader.menuTypes().forId(5)
      ),
      ModuleType(
         id = 8,
         value = "APGLRPT",
         program = "APGLRPT",
         description = "G/L Analysis",
         localizationCode = "gl.analysis",
         menuType = MenuTypeDataLoader.menuTypes().forId(3)
      ),
      ModuleType(
         id = 9,
         value = "APLST",
         program = "APRPT",
         description = "Vendor Invoices",
         localizationCode = "vendor.invoices",
         menuType = MenuTypeDataLoader.menuTypes().forId(1)
      ),
      ModuleType(
         id = 10,
         value = "APPREVUE",
         program = "APPREVUE",
         description = "Check Preview Rpt",
         localizationCode = "check.preview.rpt",
         menuType = MenuTypeDataLoader.menuTypes().forId(6)
      ),
      ModuleType(
         id = 11,
         value = "APPURGE",
         program = "APPURGE",
         description = "Purge AP Records",
         localizationCode = "purge.ap.records",
         menuType = MenuTypeDataLoader.menuTypes().forId(1)
      ),
      ModuleType(
         id = 12,
         value = "APRPT",
         program = "APRPT",
         description = "AP Report",
         localizationCode = "ap.report",
         menuType = MenuTypeDataLoader.menuTypes().forId(7)
      ),
      ModuleType(
         id = 13,
         value = "APSEL",
         program = "APSEL",
         description = "Select Invoices",
         localizationCode = "select.invoices",
         menuType = MenuTypeDataLoader.menuTypes().forId(6)
      ),
      ModuleType(
         id = 14,
         value = "APSHO",
         program = "APSHO",
         description = "Show Invoices",
         localizationCode = "show.invoices",
         menuType = MenuTypeDataLoader.menuTypes().forId(5)
      ),
      ModuleType(
         id = 15,
         value = "APSTATUS",
         program = "APSTATUS",
         description = "Vendor Status",
         localizationCode = "vendor.status",
         menuType = MenuTypeDataLoader.menuTypes().forId(5)
      ),
      ModuleType(
         id = 17,
         value = "APVOID",
         program = "APVOID",
         description = "Void Checks",
         localizationCode = "void.checks",
         menuType = MenuTypeDataLoader.menuTypes().forId(6)
      ),
      ModuleType(
         id = 18,
         value = "CASHOUT",
         program = "CASHOUT",
         description = "Cash Requirements",
         localizationCode = "cash.requirements",
         menuType = MenuTypeDataLoader.menuTypes().forId(7)
      ),
//      ModuleType(
//         id = 19,
//         value = "CHKPURGE",
//         program = "APPURGE",
//         description = "Purge Checks",
//         localizationCode = "purge.checks",
//         menuType = MenuTypeDataLoader.menuTypes().forId(8)
//      ),
//      ModuleType(
//         id = 21,
//         value = "ITMQUOTE",
//         program = "ITMQUOTE",
//         description = "Item Quotes",
//         localizationCode = "item.quotes",
//         menuType = MenuTypeDataLoader.menuTypes().forId(8)
//      ),
      ModuleType(
         id = 19,
         value = "POADD",
         program = "POADD",
         description = "Add PO",
         localizationCode = "add.po",
         menuType = MenuTypeDataLoader.menuTypes().forId(8)
      ),
      ModuleType(
         id = 20,
         value = "POCHG",
         program = "POCHG",
         description = "Change PO",
         localizationCode = "change.po",
         menuType = MenuTypeDataLoader.menuTypes().forId(8)
      ),
      ModuleType(
         id = 21,
         value = "PODEL",
         program = "PODEL",
         description = "Delete PO",
         localizationCode = "delete.po",
         menuType = MenuTypeDataLoader.menuTypes().forId(8)
      ),
      ModuleType(
         id = 22,
         value = "POINLOAD",
         program = "POINLOAD",
         description = "Receive From PO",
         localizationCode = "receive.from.po",
         menuType = MenuTypeDataLoader.menuTypes().forId(8)
      ),
      ModuleType(
         id = 23,
         value = "POLST",
         program = "POLISTS",
         description = "List PO",
         localizationCode = "list.po",
         menuType = MenuTypeDataLoader.menuTypes().forId(8)
      ),
      ModuleType(
         id = 24,
         value = "PARAMS",
         program = "PARAMS",
         description = "Control File Main",
         localizationCode = "control.file.main",
         menuType = MenuTypeDataLoader.menuTypes().forId(10)
      ),
      ModuleType(
         id = 25,
         value = "POPURGE",
         program = "POPURGE",
         description = "Purge PO Records",
         localizationCode = "purge.po.records",
         menuType = MenuTypeDataLoader.menuTypes().forId(8)
      ),
      ModuleType(
         id = 26,
         value = "POREC",
         program = "POREC",
         description = "Enter Receiving",
         localizationCode = "enter.receiving",
         menuType = MenuTypeDataLoader.menuTypes().forId(9)
      ),
      ModuleType(
         id = 27,
         value = "PORECLST",
         program = "PORECLST",
         description = "List Receiving",
         localizationCode = "list.receiving",
         menuType = MenuTypeDataLoader.menuTypes().forId(9)
      ),
      ModuleType(
         id = 28,
         value = "PORECRPT",
         program = "PORECRPT",
         description = "Receiving Rpt",
         localizationCode = "receiving.rpt",
         menuType = MenuTypeDataLoader.menuTypes().forId(9)
      ),
      ModuleType(
         id = 29,
         value = "PORPT",
         program = "PORPT",
         description = "PO Report",
         localizationCode = "po.report",
         menuType = MenuTypeDataLoader.menuTypes().forId(9)
      ),
      ModuleType(
         id = 30,
         value = "POSHO",
         program = "POSHO",
         description = "Show PO",
         localizationCode = "show.po",
         menuType = MenuTypeDataLoader.menuTypes().forId(9)
      ),
      ModuleType(
         id = 31,
         value = "POUPDT",
         program = "POUPDT",
         description = "Update PO",
         localizationCode = "update.po",
         menuType = MenuTypeDataLoader.menuTypes().forId(8)
      ),
      ModuleType(
         id = 32,
         value = "POWRKSHT",
         program = "POWRKSHT",
         description = "Recv Worksheets",
         localizationCode = "recv.worksheets",
         menuType = MenuTypeDataLoader.menuTypes().forId(9)
      ),
      ModuleType(
         id = 33,
         value = "QUOTERPT",
         program = "QUOTERPT",
         description = "Quote Report",
         localizationCode = "quote.report",
         menuType = MenuTypeDataLoader.menuTypes().forId(9)
      ),
      ModuleType(
         id = 34,
         value = "VDRQUOTE",
         program = "VDRQUOTE",
         description = "Vendor Quotes",
         localizationCode = "vendor.quotes",
         menuType = MenuTypeDataLoader.menuTypes().forId(9)
      ),
      ModuleType(
         id = 35,
         value = "SPOADD",
         program = "SPOADD",
         description = "Special Orders",
         localizationCode = "special.orders",
         menuType = MenuTypeDataLoader.menuTypes().forId(8)
      ),
      ModuleType(
         id = 36,
         value = "SPOLST",
         program = "SPOLST",
         description = "List Special Ord",
         localizationCode = "list.special.ord",
         menuType = MenuTypeDataLoader.menuTypes().forId(8)
      ),
      ModuleType(
         id = 37,
         value = "SPOPRT",
         program = "SPOPRT",
         description = "Prt Special Ord",
         localizationCode = "prt.special.ord",
         menuType = MenuTypeDataLoader.menuTypes().forId(9)
      ),
      ModuleType(
         id = 38,
         value = "POCAN",
         program = "POCAN",
         description = "Cancel PO",
         localizationCode = "cancel.po",
         menuType = MenuTypeDataLoader.menuTypes().forId(8)
      ),
      ModuleType(
         id = 43,
         value = "POCOPY",
         program = "POCOPY",
         description = "Copy a PO",
         localizationCode = "copy.a.po",
         menuType = MenuTypeDataLoader.menuTypes().forId(8)
      )
   )

   private val moduleConfigEntities = listOf(
      ModuleEntity(
         company = CompanyFactory.tstds1(),
         moduleType = moduleTypes.forId(1)!!,
         level = 10
      ),
      ModuleEntity(
         company = CompanyFactory.tstds1(),
         moduleType = moduleTypes.forId(2)!!,
         level = 15
      )
//      ModuleEntity(
//         company = CompanyFactory.tstds1(),
//         moduleType = moduleTypes.forId(4)!!,
//         level = 12
//      )
   )

   @JvmStatic
   fun moduleTypes(): List<ModuleType> = moduleTypes

   @JvmStatic
   fun predefinedModules(): List<ModuleEntity> = moduleConfigEntities

   @JvmStatic
   fun random(): ModuleType = moduleTypes.random()
}

@Singleton
@Requires(env = ["develop", "test"])
class ModuleDataLoaderService(
   private val repository: ModuleRepository
) {
   fun predefined() = ModuleDataLoader.moduleTypes()

   fun moduleConfigs(company: Company): Stream<ModuleEntity> = ModuleDataLoader.predefinedModules()
      .stream()
      .filter {
         it.company.myDataset() == company.myDataset()
      }
      .map {
         repository.insert(it, company)
      }
}
