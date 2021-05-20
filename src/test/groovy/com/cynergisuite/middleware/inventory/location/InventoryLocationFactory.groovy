package com.cynergisuite.middleware.inventory.location

import groovy.transform.CompileStatic

@CompileStatic
class InventoryLocationFactory {
   private static final List<InventoryLocationType> locations = [
      new InventoryLocationType(1, "STORE", "Item is located at the store", "inventory.value.location"),
      new InventoryLocationType(2, "WAREHOUSE", "Item is located in the warehouse", "inventory.value.warehouse"),
      new InventoryLocationType(3, "PENDING", "Item is pending", "inventory.value.pending"),
      new InventoryLocationType(4, "CUSTOMER", "Item is on rent to a customer", "inventory.value.customer"),
      new InventoryLocationType(5, "LOANER", "Item is on loan from the manufacturer", "inventory.value.loaner"),
      new InventoryLocationType(6, "SERVICE", "Item is being serviced", "inventory.value.service"),
      new InventoryLocationType(7, "STOLEN", "Item has been stolen", "inventory.value.stolen"),
      new InventoryLocationType(8, "CHARGEOFF", "Item has been charged off", "inventory.value.chargeoff"),
      new InventoryLocationType(9, "ON-RENT", "Item has been rented out", "inventory.value.on-rent")
   ]

   static def findByValue(String val) { locations.find { it.value == val} }

   static def store() { locations.find { it.value == "STORE" } }

   static def warehouse() { locations.find { it.value == "WAREHOUSE" } }

   static def pending() { locations.find { it.value == "PENDING" } }

   static def customer() { locations.find { it.value == "CUSTOMER" } }

   static def loaner() { locations.find { it.value == "LOANER" } }

   static def service() { locations.find { it.value == "SERVICE" } }

   static def stolen() { locations.find { it.value == "STOLEN" } }

   static def chargoff() { locations.find { it.value == "CHARGEOFF" } }
}
