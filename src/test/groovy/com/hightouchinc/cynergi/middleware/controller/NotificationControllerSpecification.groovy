package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.controller.spi.ControllerSpecificationBase
import com.hightouchinc.cynergi.test.data.loader.NotificationDataLoaderService

class NotificationControllerSpecification extends ControllerSpecificationBase {
   final def url = "/api/TODO add valid path here"
   final def notificationsDataLoaderService = applicationContext.getBean(NotificationDataLoaderService)

   void "test" () {
      expect:
      1 != 1 // do a lot of testing!
   }
}
