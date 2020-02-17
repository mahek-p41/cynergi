package com.cynergisuite.middleware.schedule

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ScheduleName(
   val value: String
)
