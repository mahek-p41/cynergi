package com.cynergisuite.middleware.sign.here

import com.lowagie.text.pdf.PdfReader
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import java.io.File
import java.nio.file.Files

private val signerFieldRegex = Regex("^Signer(?<signer>\\d+)(?<fieldType>Signature|Initial|Initials|RadioInitial|RadioInitials)(?<fieldIndex>\\d+)(?:Group(?<groupNumber>\\d+))?\$")

data class Signatory(
   val value: String,
   val index: Int,
)

object SignatoryFinder {

   @JvmStatic fun reduceSignatoriesBasedOnProvidedPdf(pdf: File, signatories: List<String>): List<Signatory> {
      val signatoryIndexes = Files.newInputStream(pdf.toPath()).use { pdfStream ->
         PdfReader(pdfStream).use { pdfReader ->
            pdfReader.acroFields.allFields.asSequence()
               .map { signerFieldRegex.find(it.key) }
               .filter { it != null && !it.groups.isEmpty() }
               .map { it!!.groups }
               .map { it["signer"]?.value }
               .filter(StringUtils::isNotBlank)
               .filter(NumberUtils::isParsable)
               .map(NumberUtils::createInteger)
               .toSet()
         }
      }

      return signatories.asSequence()
         .mapIndexed {index, value -> Signatory(value, index + 1) }
         .filter { value ->
            signatoryIndexes.contains(value.index)
         }.toList()
   }
}

