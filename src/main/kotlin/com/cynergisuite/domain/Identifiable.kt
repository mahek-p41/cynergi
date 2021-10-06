package com.cynergisuite.domain

import java.util.UUID

interface Identifiable {
   fun myId(): UUID?
}
