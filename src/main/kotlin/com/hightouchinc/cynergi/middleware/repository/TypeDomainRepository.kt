package com.hightouchinc.cynergi.middleware.repository

/**
 * Defines the contract of a repository that deals with a "domain" table where the rows in that table aren't managed
 * directly by the business logic.
 */
interface TypeDomainRepository<ENTITY> {

   /**
    * Return a row in the database defined by ENTITY based on it's ID if it exists otherwise null is returned
    *
    * @param id the ID to be used to look up the entity
    * @return ENTITY that defines the row if it exists, null otherwise
    */
   fun findOne(id: Long): ENTITY?

   // TODO fun fetchAll() with paging
}
