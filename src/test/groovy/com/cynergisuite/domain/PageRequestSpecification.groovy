package com.cynergisuite.domain

import spock.lang.Specification

class PageRequestSpecification extends Specification {

   void "standard page request handles null page property yields separation correctly" () {
      expect:
      new StandardPageRequest([size: 1, sortBy: 'id', sortDirection: 'ASC']).toString() == "?size=1&sortBy=id&sortDirection=ASC"
   }

   void "standard page request handles only page property yields separation correctly" () {
      expect:
      new StandardPageRequest([page: 1]).toString() == "?page=1"
   }
}
