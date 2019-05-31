package com.cynergisuite.extensions

import org.apache.commons.lang3.StringUtils

fun String?.trimToNull(): String? =
   StringUtils.trimToNull(this)
