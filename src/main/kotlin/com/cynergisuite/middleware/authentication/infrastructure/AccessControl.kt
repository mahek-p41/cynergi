package com.cynergisuite.middleware.authentication.infrastructure

import io.micronaut.aop.Around
import io.micronaut.context.annotation.Type
import kotlin.annotation.AnnotationRetention.RUNTIME

@MustBeDocumented
@Retention(RUNTIME)
@Target(AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
@Around
@Type(AccessControlService::class)
annotation class AccessControl(
   val asset: String
)
