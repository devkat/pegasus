package devkat.pegasus.fonts

import cats.effect.Sync
import cats.implicits._
import org.apache.fop.configuration.{DefaultConfiguration, DefaultConfigurationBuilder}
import org.apache.fop.fonts.{LazyFont, Font => FopFont}
import org.apache.fop.svg.PDFDocumentGraphics2DConfigurator

import scala.jdk.CollectionConverters._

object FontService {

  private val unicodeBlocks = Seq(
    0x0000 to 0x024F,
    0x1E00 to 0x1EFF,
    0x2000 to 0x206F,
    0x20A0 to 0x20CF,
    0x2100 to 0x214F,
    0x3000 to 0x9faf
  )

  private val referenceSize = 10

  private lazy val fopConfigPath = "/fonts/default.fop.xconf"

  private lazy val fopConfig: DefaultConfiguration =
    new DefaultConfigurationBuilder().build(getClass.getResourceAsStream(fopConfigPath))

  private final case class FontInfo(family: FontFamily,
                                    style: FontStyle,
                                    weight: FontWeight,
                                    blocks: List[Block],
                                    kerning: Map[String, Int])

  def getFonts[F[_] : Sync]: F[Fonts] = Sync[F].delay {
    val fontInfo = PDFDocumentGraphics2DConfigurator.createFontInfo(fopConfig, false)
    val typefaces = fontInfo.getFonts
    val fontInfos = fontInfo.getFontTriplets.asScala.toList
      .filter(f => f._1.getName.startsWith("Arvo") || f._1.getName.startsWith("Times"))
      .map { case (triplet, key) =>
      val tf = typefaces.get(key)
      val typeface = tf match {
        case font: LazyFont => font.getRealFont
        case _ => tf
      }
      val fopFont = fontInfo.getFontInstance(triplet, referenceSize)
      val metrics = fopFont.getFontMetrics
      FontInfo(
        family = FontFamily(typeface.getFamilyNames.asScala.headOption.getOrElse(typeface.getFullName)),
        style = FontStyle(triplet.getStyle),
        weight = FontWeight(triplet.getWeight),
        blocks = unicodeBlocks.toList.map(unicodeBlock(fopFont, _)),
        kerning = metrics.getKerningInfo.asScala.flatMap { case (a, m) =>
          //(a.toString, m.toMap.map { case (b, w) => (b.toString -> w.toInt) })
          m.asScala.map { case (b, w) =>
            (a.toChar.toString + "," + b.toChar.toString) -> (w.toInt * fopFont.getFontSize)
          }
        }.toMap
      )
    }

    Fonts(
      fontInfos
        .distinctBy(font => (font.family, font.style, font.weight))
        .map(font => Font(font.family, font.style, font.weight, font.blocks, font.kerning))
    )
  }

  @SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
  private def unicodeBlock(font: FopFont, range: Range.Inclusive): Block = {
    val metrics = font.getFontMetrics
    val char2width: Map[String, Int] =
      range.toList.map(ch => ch.toChar.toString -> metrics.getWidth(font.mapChar(ch.toChar), referenceSize)).toMap
    val default = char2width.values.toList.groupBy(identity).toList.maxBy(_._2.size)._1
    Block(
      start = range.head,
      end = range.end,
      default = default,
      chars = char2width.filterNot { case (ch, w) => w == default }
    )
  }

}
