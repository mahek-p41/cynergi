package com.cynergisuite.domain

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS

@Retention(RUNTIME)
@Target(allowedTargets = [CLASS])
annotation class ValueObject
