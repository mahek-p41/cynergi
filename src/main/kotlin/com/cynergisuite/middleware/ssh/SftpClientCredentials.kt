package com.cynergisuite.middleware.ssh

import com.cynergisuite.middleware.manager.SftpClientCredentialsDto

data class SftpClientCredentials(
   val username: String,
   val password: String,
   val host: String,
   val port: Int,
) {
   constructor(darwillManagementDto: SftpClientCredentialsDto) :
      this(
         username = darwillManagementDto.username!!,
         password = darwillManagementDto.password!!,
         host = darwillManagementDto.host!!,
         port = darwillManagementDto.port!!,
      )
}
