package com.cynergisuite.middleware.load.legacy

import io.micronaut.context.event.ApplicationEvent

class LegacyLoadFinishedEvent(source: String) : ApplicationEvent(source) {
}
