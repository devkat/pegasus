package devkat.pegasus.layout

import cats.data.ReaderWriterStateT._
import cats.data.{EitherT, NonEmptyList, ReaderWriterStateT}
import cats.implicits._
import cats.{Applicative, Monad}
import devkat.pegasus.model.ParagraphStyle
import devkat.pegasus.model.sequential._

object Layout {

  type LayoutRW[F[_], A] = ReaderWriterStateT[F, LayoutEnv, List[String], Unit, A]

  def apply[
    F[_] : Monad
  ](flow: Flow, width: Double): LayoutRW[F, List[Line]] =
    layout[F](flow, 0, width, 0)

  def layout[
    F[_] : Monad
  ](flow: Flow, index: Int, width: Double, y: Double): LayoutRW[F, List[Line]] =
    flow match {
      case Nil => pure(Nil)
      case element :: tail => element match {
        case Paragraph(style) => layoutParagraph(style, tail, index + 1, width, y)
        case other =>
          tell[F, LayoutEnv, List[String], Unit](List("Invalid element in paragraph position: " + other.toString))
            .as(Nil)
      }
    }

  def layoutParagraph[
    F[_] : Monad
  ](style: ParagraphStyle,
    flow: Flow,
    index: Int,
    maxWidth: Double,
    y: Double): LayoutRW[F, List[Line]] = {

    val paraChars = flow.takeWhile {
      case _: Character => true
      case _ => false
    }

    layoutLines[F](paraChars, index, style, y, maxWidth)
      .flatMap { lines =>
        val bottom = lines.lastOption.map(l => l.box.y + l.box.h).getOrElse(y)
        val next: LayoutRW[F, List[Line]] =
          if (paraChars.size === flow.size) pure(List.empty)
          else layout(flow.drop(paraChars.size), index + paraChars.size, maxWidth, bottom)
        next.map(lines ::: _)
      }
  }

  def layoutLines[
    F[_] : Monad
  ](flow: Flow,
    index: Int,
    paraStyle: ParagraphStyle,
    y: Double,
    maxWidth: Double): LayoutRW[F, List[Line]] =
    layoutLines2[F](Nil, Nil, flow, index, 0, y, maxWidth, paraStyle)

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  private def layoutLines2[
    F[_] : Monad
  ](lineAcc: List[Line],
    elemAcc: List[LineElement],
    flow: Flow,
    index: Int,
    x: Double,
    y: Double,
    maxWidth: Double,
    paraStyle: ParagraphStyle): LayoutRW[F, List[Line]] = {

    lazy val newAcc =
      elemAcc.toNel.fold(
        lineAcc
      )(
        lineAcc :+ Line(Box(0, y, maxWidth, 20), _, paraStyle)
      )

    flow match {
      case Nil => pure(newAcc)
      case nonEmpty =>
        for {
          chunkOption <- layoutNextChunk[F](nonEmpty, index, x, y, maxWidth, paraStyle)
          result <- chunkOption match {
            case None =>
              layoutLines2[F](
                newAcc,
                List.empty[LineElement],
                flow,
                index,
                0.0,
                y + 20,
                maxWidth,
                paraStyle
              )
            case Some((elems, chunkRest)) =>
              layoutLines2[F](
                lineAcc,
                elemAcc ::: elems,
                chunkRest,
                index + elems.length,
                elems.lastOption.map(e => e.box.x + e.box.w).getOrElse(x),
                y,
                maxWidth,
                paraStyle
              )
          }
        } yield result
    }
  }

  private def createGlyph[
    F[_] : Monad
  ](paraStyle: ParagraphStyle,
    prev: Option[Character],
    c: Character,
    x: Double,
    y: Double,
    index: Int): LayoutRW[F, Option[Glyph]] =
    for {
      kernAndWidthE <- Glyphs.kerningAndWidth[F](c, prev, paraStyle)
      kernAndWidthO <- EitherT
        .fromEither[LayoutRW[F, *]](kernAndWidthE)
        .map(Some(_))
        .valueOrF(error => log[F](error).as(Option.empty[(Double, Double)]))
      glyph <- kernAndWidthO.traverse { case (k, w) => mkGlyph[F](index, Box(x + k, y, w, 20), c) }
    } yield glyph

  private def mkGlyph[
    F[_] : Monad
  ](index: Int, box: Box, c: Character): LayoutRW[F, Glyph] =
    ask[F, LayoutEnv, List[String], Unit].map { env =>
      val (char, hidden) =
        if (env.settings.showHiddenCharacters)
          c.char match {
            case ' ' => ('·', true)
            case other => (other, false)
          }
        else
          (c.char, false)
      Glyph(index, box, c.copy(char = char), hidden)
    }

  private def layoutNextChunk[
    F[_] : Monad
  ](flow: Flow,
    index: Int,
    x: Double,
    y: Double,
    maxWidth: Double,
    paraStyle: ParagraphStyle): LayoutRW[F, Option[(List[LineElement], Flow)]] =
    for {
      env <- ask[F, LayoutEnv, List[String], Unit]
      layoutChunks <- WordStream
        .apply(flow, env.hyphenationSpec)
        .traverse { case (chunk, spaces, rest) =>
          layoutChunk[F](chunk, index, spaces, x, y, paraStyle).map((_, rest))
        }
    } yield
      layoutChunks
        .find {
          case ((_ :+ e, _), _) => e.box.x + e.box.w < maxWidth
          case _ => false
        }
        .map { case ((elems, spaces), rest) => (elems ::: spaces, rest) }

  private def layoutChunk[
    F[_] : Monad
  ](flow: List[Element],
    index: Int,
    spaces: List[Character],
    startX: Double,
    y: Double,
    paraStyle: ParagraphStyle): LayoutRW[F, (List[LineElement], List[LineElement])] =
    flow
      .foldLeftM((
        List.empty[LineElement], // accumulated line elements
        Option.empty[Character], // previous character (for kerning)
        index
      )) { case ((elems, prev, index), e) =>
        val x = elems.lastOption.map(e => e.box.x + e.box.w).getOrElse(startX)
        for {
          layoutElementOption <- e match {
            case c: Character =>
              createGlyph[F](paraStyle, prev, c, x, y, index)
            case other =>
              log[F]("Unsupported element: " + other.toString).as(None)
          }
        } yield
          layoutElementOption.fold(
            (elems, Option.empty[Character], index + 1)
          )(
            glyph => (elems :+ glyph, Some(glyph.char), index + 1)
          )
      }
      .map { case (elems, _, _) => elems }
      .flatMap { elems =>
        val x = elems.lastOption.map(e => e.box.x + e.box.w).getOrElse(startX)
        spaces
          .zipWithIndex
          .flatTraverse { case (space, i) =>
            createGlyph[F](paraStyle, None, space, x, y, index + elems.size + i).map(_.toList)
          }
          .map((elems, _))
      }

  private implicit final class CharSyntax(val c: Char) {

    private lazy val wordBreakChars = List(' ')

    def isWordBreak: Boolean =
      wordBreakChars.contains_(c)

  }

  private def log[F[_] : Applicative](msg: String): LayoutRW[F, Unit] =
    tell[F, LayoutEnv, List[String], Unit](List(msg))

}
