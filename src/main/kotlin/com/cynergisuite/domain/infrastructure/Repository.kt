package com.cynergisuite.domain.infrastructure

/**
 * Defines the basic operations a class that interacts with rows in the database should provide
 */
interface Repository<ENTITY> {

   /**
    * Return a row in the database defined by ENTITY based on it's ID if it exists otherwise null is returned
    *
    * @param id the ID to be used to look up the entity
    * @return ENTITY that defines the row if it exists, null otherwise
    */
   fun findOne(id: Long): ENTITY?

   /**
    * Check if an ENTITY exists based on it's ID
    *
    * @param id the id to be checked in the database for
    * @return true if a row exists with that ID false otherwise
    */
   fun exists(id: Long): Boolean

   /**
    * insert a row defined by the entity parameter
    *
    * @param entity the row to insert in the database
    * @return the newly saved row in the database
    */
   fun insert(entity: ENTITY): ENTITY

   /**
    * update a row in the database defined by the entity parameter
    *
    * @param entity the row to update in the database
    * @return the freshly updated row in the database
    */
   fun update(entity: ENTITY): ENTITY
}
