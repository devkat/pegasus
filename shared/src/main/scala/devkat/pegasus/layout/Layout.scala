package devkat.pegasus.layout

import cats.data.ReaderWriterStateT._
import cats.data.{EitherT, ReaderWriterStateT}
import cats.implicits._
import cats.{Applicative, Monad}
import devkat.pegasus.fonts.{FontKey, Fonts}
import devkat.pegasus.layout.LineElement.Glyph
import devkat.pegasus.model.Style.ParagraphStyle
import devkat.pegasus.model.StyleAttr.{FontFamily, FontSize, FontStyle, FontWeight}
import devkat.pegasus.model.sequential._
import shapeless.HMap

final case class Box(x: Double, y: Double, w: Double, h: Double)

final case class Line(box: Box, elements: List[LineElement])

sealed abstract class LineElement(val box: Box)

object LineElement {

  final case class Glyph(override val box: Box, char: Character) extends LineElement(box)

}

object Layout {

  type LayoutRW[F[_], A] = ReaderWriterStateT[F, Fonts, List[String], Unit, A]

  def apply[
    F[_] : Monad
  ](flow: Flow, width: Double): LayoutRW[F, List[Line]] =
    layout(flow, width, 0)

  def layout[
    F[_] : Monad
  ](flow: Flow, width: Double, y: Double): LayoutRW[F, List[Line]] =
    flow match {
      case Nil => pure(Nil)
      case element :: tail => element match {
        case Paragraph(style) => layoutParagraph(style, tail, width, y)
        case other => tell[F, Fonts, List[String], Unit](List("Invalid element in paragraph position: " + other.toString))
          .as(Nil)
      }
    }

  def layoutParagraph[
    F[_] : Monad
  ](style: HMap[ParagraphStyle], tail: Flow, width: Double, y: Double): LayoutRW[F, List[Line]] = {
    val paraChars = tail.takeWhile {
      case _: Character => true
      case _ => false
    }

    layoutLines[F](paraChars, style, width, y)
      .flatMap { lines =>
        val bottom = lines.lastOption.map(l => l.box.y + l.box.h).getOrElse(y)
        val next: LayoutRW[F, List[Line]] =
          if (paraChars.size == tail.size) pure(List.empty)
          else layout(tail.drop(paraChars.size), width, bottom)
        next.map(lines ::: _)
      }
  }

  def layoutLines[
    F[_] : Monad
  ](flow: Flow, paraStyle: HMap[ParagraphStyle], width: Double, y: Double): LayoutRW[F, List[Line]] =
    flow
      .foldLeftM((
        List.empty[Line],
        List.empty[LineElement],
        Option.empty[Character]
      )) { case ((lines, elems, prev), e) =>
        val x = elems.lastOption.map(e => e.box.x + e.box.w).getOrElse(0d)
        for {
          layoutElementOption <- e match {
            case c: Character =>
              for {
                kernAndWidthE <- kerningAndWidth[F](c, prev, paraStyle)
                kernAndWidthO <- EitherT
                  .fromEither[LayoutRW[F, *]](kernAndWidthE)
                  .map(Some(_))
                  .valueOrF(error => log[F](error).as(Option.empty[(Double, Double)]))
              } yield
                kernAndWidthO.map { case (k, w) => Glyph(Box(x + k, 0, w, 0), c) }
            case other => log[F]("Unsupported element: " + other.toString).as(None)
          }
        } yield
          layoutElementOption.fold(
            (lines, elems, Option.empty[Character])
          ) { glyph =>
            if (x + glyph.box.w > width)
              (lines :+ Line(Box(0, y + (lines.size + 1) * 20, 0, 20), elems), Nil, None)
            else
              (lines, elems :+ glyph, Some(glyph.char))
          }
      }
      .map { case (prevLines, prevElems, _) =>
        prevLines :+ Line(Box(0, y + (prevLines.size + 1) * 20, 0, 20), prevElems)
      }


  /*
  def layoutLines[
    F[_] : Monad
  ](e: Element, tail: Flow, remainingWidth: Double): LayoutRW[F, List[Line]] = {
    e match {
      case c@Character(char, style) =>
        for {
          widthOrError <- referenceWidth[F](c)
          widthOption <- EitherT
            .fromEither[LayoutRW[F, *]](widthOrError)
            .map(Some(_))
            .valueOrF(error => tell[F, Fonts, List[String], Unit](List(error)).as(Option.empty[Int]))
        } yield widthOption.
      case other => tell[F, Fonts, List[String], Unit](List("Unsupported element: " + other.toString))
        .as(Nil)
    }
  }
   */

  private val referenceFontFactor = 10000

  def kerningAndWidth[
    F[_] : Monad
  ](char: Character,
    prev: Option[Character],
    paraStyle: HMap[ParagraphStyle]): LayoutRW[F, Either[String, (Double, Double)]] =
    ask[F, Fonts, List[String], Unit].map(fonts =>
      Tuple4
        .apply(
          char.style.get(FontFamily).orElse(paraStyle.get(FontFamily)).toRight("Font family not set"),
          char.style.get(FontWeight).orElse(paraStyle.get(FontWeight)).toRight("Font weight not set"),
          char.style.get(FontStyle).orElse(paraStyle.get(FontStyle)).toRight("Font style not set"),
          char.style.get(FontSize).orElse(paraStyle.get(FontSize)).toRight("Font size not set")
        )
        .tupled
        .flatMap { case (fontFamily, fontWeight, fontStyle, fontSize) =>
          val key = FontKey(fontFamily, fontWeight, fontStyle)
          for {
            font <- fonts.get(key).toRight(s"Font $key not found")
            block <- font.blocks.find(block => block.start <= char.char.toInt && char.char.toInt <= block.end)
              .toRight(s"Block for character ${char.char} not found")
          } yield {
            val w = block.chars.getOrElse(char.char.toString, block.default)
            val kerning = prev.flatMap(c => font.kerning.get(c.char + "," + char.char)).getOrElse(0)
            def factor(i: Int) = i.toDouble * fontSize.toDouble / referenceFontFactor
            println(s"${prev.map(_.char).getOrElse("–")},${char.char} $w $kerning ${factor(w)}")
            (factor(kerning), factor(w))
          }
        }

    )

  private def log[F[_] : Applicative](msg: String): LayoutRW[F, Unit] =
    tell[F, Fonts, List[String], Unit](List(msg))


}
