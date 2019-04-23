package com.cynergisuite.test.helper

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

   static boolean allPropertiesFullAndNotEmptyExcept(Object obj, String ...except) {
      final def exceptions = new HashSet(except.collect())

      return obj.properties.findAll { !exceptions.contains(it.key) }.findAll {
         it.value == null ||
            (
               (it.value instanceof Collection || it.value instanceof Map) && it.value.size() < 1
            )
      }.size() == 0
   }
}
