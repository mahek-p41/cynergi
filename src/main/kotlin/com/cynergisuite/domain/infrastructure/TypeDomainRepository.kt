package com.cynergisuite.domain.infrastructure

/**
 * Defines the contract of a repository that deals with a "domain" table where the rows in that table aren't managed
 * directly by the business logic.
 */
interface TypeDomainRepository<ENTITY> {

   /**
    * Used to check if a type domain value exists in the database (Basically is it an allowed value for the system)
    *
    * @param value the value to search for in the value column in the database (note this search is required to be case insensitive by implementing classes)
    * @return true|false depending on whether it exists in the database
    */
   fun exists(value: String): Boolean

   /**
    * Return a row in the database defined by ENTITY based on it's ID if it exists otherwise null is returned
    *
    * @param id the ID to be used to look up the entity
    * @return ENTITY that defines the row if it exists, null otherwise
    */
   fun findOne(id: Long): ENTITY?

   fun findOne(value: String): ENTITY?

   fun findAll(): List<ENTITY>
}
