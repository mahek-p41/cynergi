package com.hightouchinc.cynergi.middleware.data.domain

import kotlin.annotation.AnnotationRetention.*
import kotlin.annotation.AnnotationTarget.*

@Retention(RUNTIME)
@Target(allowedTargets = [CLASS])
annotation class DataTransferObject
