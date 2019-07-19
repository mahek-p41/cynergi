package com.cynergisuite.extensions

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils

fun String?.trimToNull(): String? =
   StringUtils.trimToNull(this)

fun String?.isDigits(): Boolean =
   NumberUtils.isDigits(this)
