package com.cynergisuite.domain

@Deprecated("This should only be used on entities coming from fastinfo.  Once that is no longer required this interface should be deleted")
interface LegacyIdentifiable {

   @Deprecated("This method only be used when referencing entities coming from fastinfo.")
   fun myId(): Long?
}
