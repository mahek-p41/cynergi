package com.hightouchinc.cynergi.test.helper

class SpecificationHelpers {

   /**
    * Shallow iteration through an object checking that all the properties defined on it aren't null.  If one of those
    * properties is a collection of some sort it will check that it's size is greater than 0
    * @param obj
    * @return
    */
   static boolean allPropertiesFullAndNotEmpty(Object obj) {
      return obj.properties.findAll {
         it.value == null ||
            (
               (it.value instanceof Collection || it.value instanceof Map) && it.value.size() < 1
            )
      }.size() == 0
   }
}
