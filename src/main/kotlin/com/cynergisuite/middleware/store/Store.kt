package com.cynergisuite.middleware.store

import com.cynergisuite.middleware.location.Location

/**
 * Used to differentiate in the model a location that has a number less than 1000.
 *
 * FIXME someday hopefully Location will go away
 */
interface Store : Location
