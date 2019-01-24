package com.hightouchinc.cynergi.middleware.entity

interface TypeDomainEntity<ENTITY>: Entity<ENTITY> {

   fun myValue(): String

   fun myDescription(): String
}
