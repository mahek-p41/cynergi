package com.cynergisuite.middleware.store

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.location.Location
import com.cynergisuite.middleware.region.RegionEntity

/**
 * Used to differentiate in the model a location that has a number less than 1000.
 *
 * FIXME someday hopefully Location will go away
 */
interface Store : Location {
   fun myRegion(): RegionEntity?
}
