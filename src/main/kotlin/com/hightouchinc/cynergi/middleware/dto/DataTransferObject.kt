package com.hightouchinc.cynergi.middleware.dto

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS

@Retention(RUNTIME)
@Target(allowedTargets = [CLASS])
annotation class DataTransferObject
