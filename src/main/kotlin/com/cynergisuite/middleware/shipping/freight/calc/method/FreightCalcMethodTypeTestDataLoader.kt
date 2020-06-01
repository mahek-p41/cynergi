package com.cynergisuite.middleware.shipping.freight.calc.method

object FreightCalcMethodTypeTestDataLoader {
   private val freightCalcMethodTypes = listOf(
      FreightCalcMethodType(
         id = 1,
         value = "I",
         description = "Item",
         localizationCode = "item"
      ),
      FreightCalcMethodType(
         id = 2,
         value = "N",
         description = "None",
         localizationCode = "none"
      ),
      FreightCalcMethodType(
         id = 3,
         value = "P",
         description = "Percent",
         localizationCode = "percent"
      ),
      FreightCalcMethodType(
         id = 4,
         value = "S",
         description = "Size",
         localizationCode = "size"
      ),
      FreightCalcMethodType(
         id = 5,
         value = "W",
         description = "Weight",
         localizationCode = "weight"
      )
   )

   @JvmStatic
   fun random(): FreightCalcMethodType = freightCalcMethodTypes.random()
}
