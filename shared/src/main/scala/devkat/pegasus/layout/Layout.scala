package devkat.pegasus.layout

import cats.{Applicative, Monad}
import cats.data.{EitherT, IndexedReaderWriterStateT, OptionT, ReaderWriterStateT}
import cats.data.ReaderWriterStateT._
import cats.implicits._
import devkat.pegasus.fonts.{FontKey, Fonts}
import devkat.pegasus.layout.LayoutElement.Glyph
import devkat.pegasus.model.Style.ParagraphStyle
import devkat.pegasus.model.StyleAttr.{FontFamily, FontStyle}
import devkat.pegasus.model.sequential._
import shapeless.HMap
import shapeless.syntax._

final case class Line(x: Int, y: Int, elements: List[LayoutElement])

sealed trait LayoutElement

object LayoutElement {

  final case class Glyph(x: Int, y: Int, char: Character) extends LayoutElement

}

object Layout {

  type LayoutRW[F[_], A] = ReaderWriterStateT[F, Fonts, List[String], Unit, A]

  def apply[
    F[_] : Monad
  ](flow: Flow, width: Int): LayoutRW[F, List[Line]] = flow match {
    case Nil => pure(Nil)
    case element :: tail => element match {
      case Paragraph(style) => layoutParagraph(style, tail, width)
      case other => tell[F, Fonts, List[String], Unit](List("Invalid element in paragraph position: " + other.toString))
        .as(Nil)
    }
  }

  def layoutParagraph[
    F[_] : Monad
  ](style: HMap[ParagraphStyle], tail: Flow, width: Int): LayoutRW[F, List[Line]] = {
    val paraChars = tail.takeWhile {
      case _: Character => true
      case _ => false
    }
    /*
    val linesF: LayoutRW[F, List[Line]] = paraChars match {
      case Nil => pure(Nil)
      case e :: tail => layoutLines(e, tail, width)
    }
     */
    val linesF: LayoutRW[F, List[Line]] = layoutLines(paraChars, width)
    linesF.flatMap(lines =>
      if (paraChars.size == tail.size) pure(List.empty[Line])
      else Layout(tail.drop(paraChars.size), width)
    )
  }

  def layoutLines[
    F[_] : Monad
  ](flow: Flow, remainingWidth: Double): LayoutRW[F, List[Line]] =
    flow
      .foldLeftM((List.empty[Line], List.empty[LayoutElement], 0)) { case ((prevLines, prevElems, prevX), e) =>
      for {
        layoutElementOption <- e match {
          case c@Character(char, style) =>
            for {
              widthOrError <- referenceWidth[F](c)
              widthOption <- EitherT
                .fromEither[LayoutRW[F, *]](widthOrError)
                .map(Some(_))
                .valueOrF(error => tell[F, Fonts, List[String], Unit](List(error)).as(Option.empty[Int]))
            } yield widthOption.map(w => (Glyph(prevX, 0, c), prevX + w))
          case other => tell[F, Fonts, List[String], Unit](List("Unsupported element: " + other.toString))
            .as(None)
        }
      } yield layoutElementOption.fold(
        (prevLines, prevElems, prevX)
      ) {
        case (e, newX) => (prevLines, e :: prevElems, newX)
      }

    }
    .map { case (prevLines, prevElems, prevX) =>
      prevLines
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

  def referenceWidth[
    F[_] : Monad
  ](char: Character): LayoutRW[F, Either[String, Int]] =
    ask[F, Fonts, List[String], Unit].map(fonts =>
      Tuple2
        .apply(
          char.style.get(FontFamily).toRight("Font family not set"),
          char.style.get(FontStyle).toRight("Font style not set")
        )
        .tupled
        .map((FontKey.apply _).tupled)
        .flatMap(key =>
          for {
            font <- fonts.get(key).toRight(s"Font $key not found")
            block <- font.blocks.find(block => block.start <= char.char.toInt && char.char.toInt <= block.end)
              .toRight(s"Block for character ${char.char} not found")
          } yield block.chars.getOrElse(char.char.toString, block.default)
        )

    )


}
