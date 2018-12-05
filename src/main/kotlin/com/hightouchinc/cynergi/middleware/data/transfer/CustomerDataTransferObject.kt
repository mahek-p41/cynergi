package com.hightouchinc.cynergi.middleware.data.transfer

import javax.validation.constraints.NotNull

data class CustomerDataTransferObject(

   @NotNull
   var firstName: String?
)
