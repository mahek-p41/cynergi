package com.cynergisuite.middleware.inventory

// TODO need to probably convert this to a domain table at some point in the future, but as it stands now with this stuff coming from fastinfo it is easier to deal with as an enum
enum class InventoryStatus(
   private val shortName: String,
   private val description: String
) {
   N("NEW", "Never been out on rent"),
   O("OUT", "Currently out on rent"),
   R("RETURNED", "Returned item - has previously been out on rent and has been returned"),
   D("DEMO", "Demo Item");
}
