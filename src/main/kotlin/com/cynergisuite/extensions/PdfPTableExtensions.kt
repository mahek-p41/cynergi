package com.cynergisuite.extensions

import com.lowagie.text.Font
import com.lowagie.text.Paragraph
import com.lowagie.text.Rectangle
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable

fun PdfPTable.makeCell(text: String, vAlignment: Int, hAlignment: Int, font: Font, leading: Float, padding: Float, borders: Rectangle, ascender: Boolean, descender: Boolean) {
   val p = Paragraph(text, font)
   p.setLeading(leading)

   val cell = PdfPCell(p)
   cell.setLeading(leading, 0f)
   cell.setVerticalAlignment(vAlignment)
   cell.setHorizontalAlignment(hAlignment)
   cell.cloneNonPositionParameters(borders)
   cell.setUseAscender(ascender)
   cell.setUseDescender(descender)
   cell.setUseBorderPadding(true)
   cell.setPadding(padding)
   this.addCell(cell)
}
