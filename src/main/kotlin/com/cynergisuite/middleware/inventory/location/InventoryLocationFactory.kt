package com.cynergisuite.middleware.inventory.location

object InventoryLocationFactory {
   private val locations = listOf(
      InventoryLocationType(1, "STORE", "Item is located at the store", "inventory.value.location"),
      InventoryLocationType(2, "WAREHOUSE", "Item is located in the warehouse", "inventory.value.warehouse"),
      InventoryLocationType(3, "PENDING", "Item is pending", "inventory.value.pending"),
      InventoryLocationType(4, "CUSTOMER", "Item is on rent to a customer", "inventory.value.customer"),
      InventoryLocationType(5, "LOANER", "Item is on loan from the manufacturer", "inventory.value.loaner"),
      InventoryLocationType(6, "SERVICE", "Item is being serviced", "inventory.value.service"),
      InventoryLocationType(7, "STOLEN", "Item has been stolen", "inventory.value.stolen"),
      InventoryLocationType(8, "CHARGEOFF", "Item has been charged off", "inventory.value.chargeoff")
   )

   @JvmStatic
   fun store(): InventoryLocationType = locations.first { it.value == "STORE" }

   @JvmStatic
   fun warehouse(): InventoryLocationType = locations.first { it.value == "WAREHOUSE" }

   @JvmStatic
   fun pending(): InventoryLocationType = locations.first { it.value == "PENDING" }

   @JvmStatic
   fun customer(): InventoryLocationType = locations.first { it.value == "CUSTOMER" }

   @JvmStatic
   fun loaner(): InventoryLocationType = locations.first { it.value == "LOANER" }

   @JvmStatic
   fun service(): InventoryLocationType = locations.first { it.value == "SERVICE" }

   @JvmStatic
   fun stolen(): InventoryLocationType = locations.first { it.value == "STOLEN" }

   @JvmStatic
   fun chargoff(): InventoryLocationType = locations.first { it.value == "CHARGEOFF" }
}
