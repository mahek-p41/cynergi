package com.hightouchinc.cynergi.middleware.data.transfer

import com.hightouchinc.cynergi.middleware.data.domain.DataTransferObject
import javax.validation.constraints.NotNull

@DataTransferObject
data class Customer(

   @NotNull
   var firstName: String?
)
