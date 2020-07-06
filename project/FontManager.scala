import scala.xml.XML

import java.io.File
import org.apache.fop.configuration.DefaultConfigurationBuilder
import org.apache.fop.fonts.{Font => FopFont, Typeface}
import org.apache.fop.svg.PDFDocumentGraphics2DConfigurator
import collection.JavaConverters._
import org.apache.fop.fonts.FontTriplet
import org.apache.fop.fonts.FontMetrics
import org.apache.fop.fonts.LazyFont
import scala.xml.XML
import sbt.util.Logger

object FontManager {

  val unicodeBlocks = Seq(
    0x0000 to 0x024F,
    0x1E00 to 0x1EFF,
    0x2000 to 0x206F,
    0x20A0 to 0x20CF,
    0x2100 to 0x214F,
    0x3000 to 0x9faf
  )

  val referenceSize = 10

  lazy val fopConfigFile = "fonts/default.fop.xconf"

  lazy val fopConfig = {
    val cfgBuilder = new DefaultConfigurationBuilder
    cfgBuilder.buildFromFile(new File(fopConfigFile))
  }

  def getFopFont(font: Font, log: Logger): FopFont = {
    val fontInfo = PDFDocumentGraphics2DConfigurator.createFontInfo(fopConfig, false)
    val triplet = fontInfo.fontLookup(font.family.name, font.style.name, font.weight.weight)
    //findAdjustWeight(font.family.name, font.style.name, font.weight.weight)
    log.info("Loading font: " + triplet.getName() + triplet.getWeight)
    fontInfo.getFontInstance(triplet, referenceSize)
  }

  def getAllFonts(log: Logger): List[Font] = {
    val fontInfo = PDFDocumentGraphics2DConfigurator.createFontInfo(fopConfig, false)
    val typefaces = fontInfo.getFonts
    fontInfo.getFontTriplets.asScala.toList.map { case (triplet, key) =>
      val tf = typefaces.get(key)
      val typeface =
        if (tf.isInstanceOf[LazyFont]) tf.asInstanceOf[LazyFont].getRealFont
        else tf
      val fopFont = fontInfo.getFontInstance(triplet, referenceSize)
      val metrics = fopFont.getFontMetrics
      Font(
        family = FontFamily(typeface.getFullName + ": " + typeface.getFamilyNames.asScala.mkString(", ")),
        style = FontStyle(triplet.getStyle),
        weight = FontWeight(triplet.getWeight),
        blocks = unicodeBlocks.toList.map(range =>
          Block(
            range.head,
            range.end,
            range.toList.map(ch => metrics.getWidth(fopFont.mapChar(ch.toChar), referenceSize))
          )
        ),
        kerning = metrics.getKerningInfo.asScala.flatMap { case (a, m) =>
          //(a.toString, m.toMap.map { case (b, w) => (b.toString -> w.toInt) })
          m.asScala.map { case (b, w) => (a.toChar + "," + b.toChar) -> (w.toInt * fopFont.getFontSize) }
        }.toMap
      )
    }
  }

}
