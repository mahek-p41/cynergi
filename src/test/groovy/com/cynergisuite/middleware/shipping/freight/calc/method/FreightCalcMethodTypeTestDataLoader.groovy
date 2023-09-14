package com.cynergisuite.middleware.shipping.freight.calc.method

class FreightCalcMethodTypeTestDataLoader {
   private static final List<FreightCalcMethodType> freightCalcMethodTypes = [
      new FreightCalcMethodType(
         1,
         "I",
         "Item",
         "item"
      ),
      new FreightCalcMethodType(
         2,
         "N",
         "None",
         "none"
      ),
      new FreightCalcMethodType(
         3,
         "P",
         "Percent",
         "percent"
      ),
      new FreightCalcMethodType(
         4,
         "S",
         "Size",
         "size"
      ),
      new FreightCalcMethodType(
         5,
         "W",
         "Weight",
         "weight"
      )
   ]

   static FreightCalcMethodType random() { freightCalcMethodTypes.random() }
}
