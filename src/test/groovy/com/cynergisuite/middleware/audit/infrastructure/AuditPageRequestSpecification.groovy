package com.cynergisuite.middleware.audit.infrastructure

import spock.lang.Specification

import java.time.OffsetDateTime

class AuditPageRequestSpecification extends Specification {
   void "audit page request handles null page, from and thru property yields separation correctly" () {
      expect:
      new AuditPageRequest([size: 1, sortBy: 'id', sortDirection: 'ASC', storeNumber: [1]]).toString() == "?size=1&sortBy=id&sortDirection=ASC&storeNumber=1"
   }

   void "audit page request handles only storeNumber property yields separation correctly" () {
      expect:
      new AuditPageRequest([storeNumber: [1]]).toString() == "?storeNumber=1"
   }

   void "audit page request handles from and thru properties yielding separation and format correctly" () {
      given:
      final from = OffsetDateTime.now()
      final thru = OffsetDateTime.now()

      expect:
      new AuditPageRequest([from: from, thru: thru]).toString() == "?from=$from&thru=$thru"
   }

   void "audit page request handles storeNumber and status properties yielding separation and format correctly" () {
      given:
      final from = OffsetDateTime.now()
      final thru = OffsetDateTime.now()
      final status = ["CREATED", "IN-PROGRESS"]

      expect:
      new AuditPageRequest([from: from, thru: thru, status: status]).toString() == "?from=$from&thru=$thru&status=CREATED&status=IN-PROGRESS"
   }

   void "audit page request handles only status property yielding separation and format correctly" () {
      given:
      final status = ["CREATED", "IN-PROGRESS"]

      expect:
      new AuditPageRequest([status: status]).toString() == "?status=CREATED&status=IN-PROGRESS"
   }
}
