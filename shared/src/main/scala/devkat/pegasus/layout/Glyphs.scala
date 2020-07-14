package devkat.pegasus.layout

import cats.Monad
import cats.data.ReaderWriterStateT.ask
import cats.implicits._
import devkat.pegasus.fonts.FontKey
import devkat.pegasus.layout.Layout.LayoutRW
import devkat.pegasus.model.ParagraphStyle
import devkat.pegasus.model.sequential.Character

object Glyphs {

  private val referenceFontFactor = 10000

  def width[
    F[_] : Monad
  ](char: Character,
    paraStyle: ParagraphStyle): LayoutRW[F, Either[String, Double]] =
    kerningAndWidth[F](char, None, paraStyle)
      .map(_.map { case (_, width) => width })

  def kerningAndWidth[
    F[_] : Monad
  ](char: Character,
    prev: Option[Character],
    paraStyle: ParagraphStyle): LayoutRW[F, Either[String, (Double, Double)]] =
    ask[F, LayoutEnv, List[String], Unit].map(env =>
      Tuple4
        .apply(
          char.style.fontFamily.orElse(paraStyle.fontFamily).toRight("Font family not set"),
          char.style.fontWeight.orElse(paraStyle.fontWeight).toRight("Font weight not set"),
          char.style.fontStyle.orElse(paraStyle.fontStyle).toRight("Font style not set"),
          char.style.fontSize.orElse(paraStyle.fontSize).toRight("Font size not set")
        )
        .tupled
        .flatMap { case (fontFamily, fontWeight, fontStyle, fontSize) =>
          val key = FontKey(fontFamily, fontWeight, fontStyle)
          val fonts = env.fonts
          for {
            font <- fonts.get(key).toRight(s"Font ${key.show} not found")
            block <- font.blocks.find(block => block.start <= char.char.toInt && char.char.toInt <= block.end)
              .toRight(s"Block for character ${char.char.toString} not found")
          } yield {
            val w = block.chars.getOrElse(char.char.toString, block.default)
            val kerning = prev.flatMap(c => font.kerning.get(c.char.toString + "," + char.char.toString)).getOrElse(0)

            def factor(i: Int) = i.toDouble * fontSize.toDouble / referenceFontFactor

            //println(s"${prev.map(_.char).getOrElse("–")},${char.char} $w $kerning ${factor(w)}")
            (factor(kerning), factor(w))
          }
        }

    )

}
