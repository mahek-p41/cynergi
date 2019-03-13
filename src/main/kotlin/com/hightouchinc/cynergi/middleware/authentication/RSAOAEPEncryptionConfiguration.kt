package com.hightouchinc.cynergi.middleware.authentication

import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import io.micronaut.core.io.ResourceResolver
import io.micronaut.security.token.jwt.encryption.rsa.RSAEncryptionConfiguration
import org.apache.commons.io.input.AutoCloseInputStream
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.security.Security
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Requires(env = ["local", "prod"])
@Named("generator")
class RSAOAEPEncryptionConfiguration @Inject constructor(
   resourceResolver: ResourceResolver,
   @Value("\${cynergi.security.jwt.pem.path}") pemPath: String
): RSAEncryptionConfiguration {
   private val jweAlgorithm = JWEAlgorithm.RSA_OAEP_256
   private val encryptionMethod = EncryptionMethod.A128GCM
   private val rsaPrivateKey: RSAPrivateKey
   private val rsaPublicKey: RSAPublicKey

   init {
      Security.addProvider(BouncyCastleProvider())
      val pemParser = resourceResolver.getResourceAsStream(pemPath)
         .map { AutoCloseInputStream(it) }
         .map { InputStreamReader(it) }
         .map { PEMParser(it) }
         .orElseThrow { FileNotFoundException("Unable to find PEM for JWT encryption") }

      val pemKeyPair: PEMKeyPair = pemParser.readObject() as PEMKeyPair

      val converter = JcaPEMKeyConverter()
      val keyPair = converter.getKeyPair(pemKeyPair)

      rsaPrivateKey = keyPair.private as RSAPrivateKey
      rsaPublicKey = keyPair.public as RSAPublicKey

      pemParser.close()
   }

   override fun getPublicKey(): RSAPublicKey = rsaPublicKey
   override fun getPrivateKey(): RSAPrivateKey = rsaPrivateKey
   override fun getEncryptionMethod(): EncryptionMethod = encryptionMethod
   override fun getJweAlgorithm(): JWEAlgorithm = jweAlgorithm
}
