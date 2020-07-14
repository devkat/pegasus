package devkat.pegasus.layout

import cats.data.ReaderWriterStateT._
import cats.data.{EitherT, ReaderWriterStateT}
import cats.implicits._
import cats.{Applicative, Monad}
import devkat.pegasus.hyphenation.Hyphenator
import devkat.pegasus.model.{CharacterStyle, ParagraphStyle}
import devkat.pegasus.model.sequential._

import scala.annotation.tailrec

object Layout {

  type LayoutRW[F[_], A] = ReaderWriterStateT[F, LayoutEnv, List[String], Unit, A]

  def apply[
    F[_] : Monad
  ](flow: Flow, width: Double): LayoutRW[F, List[Line]] =
    layout[F](flow, width, 0)

  def layout[
    F[_] : Monad
  ](flow: Flow, width: Double, y: Double): LayoutRW[F, List[Line]] =
    flow match {
      case Nil => pure(Nil)
      case element :: tail => element match {
        case Paragraph(style) => layoutParagraph(style, tail, width, y)
        case other =>
          tell[F, LayoutEnv, List[String], Unit](List("Invalid element in paragraph position: " + other.toString))
            .as(Nil)
      }
    }

  def layoutParagraph[
    F[_] : Monad
  ](style: ParagraphStyle,
    tail: Flow,
    maxWidth: Double,
    y: Double): LayoutRW[F, List[Line]] = {

    val paraChars = tail.takeWhile {
      case _: Character => true
      case _ => false
    }

    layoutLines[F](paraChars, style, y, maxWidth)
      .flatMap { lines =>
        val bottom = lines.lastOption.map(l => l.box.y + l.box.h).getOrElse(y)
        val next: LayoutRW[F, List[Line]] =
          if (paraChars.size === tail.size) pure(List.empty)
          else layout(tail.drop(paraChars.size), maxWidth, bottom)
        next.map(lines ::: _)
      }
  }

  def layoutLines[
    F[_] : Monad
  ](flow: Flow,
    paraStyle: ParagraphStyle,
    y: Double,
    maxWidth: Double): LayoutRW[F, List[Line]] =
    layoutLines2[F](Nil, Nil, flow, 0, y + 20, maxWidth, None, paraStyle)

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  private def layoutLines2[
    F[_] : Monad
  ](lineAcc: List[Line],
    elemAcc: List[LineElement],
    rest: Flow,
    x: Double,
    y: Double,
    maxWidth: Double,
    charStyle: Option[CharacterStyle],
    paraStyle: ParagraphStyle): LayoutRW[F, List[Line]] =
    rest match {
      case Nil => pure(if (elemAcc.isEmpty) lineAcc else lineAcc :+ mkLine(x, y, elemAcc))
      case nonEmpty =>
        for {
          tuple <- layoutNextChunk[F](nonEmpty, dropSpaces = x === 0, x, maxWidth, paraStyle)
          (chunk, elems, restAfterChunk) = tuple
          result <- {
            val endX: Double = elems.lastOption.map(e => e.box.x + e.box.w).getOrElse(0)

            if (endX > maxWidth)
              layoutLines2[F](
                lineAcc :+ mkLine(x, y, elemAcc),
                List.empty[LineElement],
                rest,
                0.0,
                y + 20,
                maxWidth,
                charStyle,
                paraStyle
              )
            else
              layoutLines2[F](
                lineAcc,
                elemAcc ::: elems,
                restAfterChunk,
                endX,
                y,
                maxWidth,
                chunk.reverse
                  .collectFirst { case Character(_, style) => style }
                  .orElse(charStyle),
                paraStyle
              )
          }
        } yield result
    }

  private def mkLine(x: Double, y: Double, elems: List[LineElement]): Line =
    Line(Box(x, y, elems.lastOption.map(e => e.box.x + e.box.w).getOrElse(0), 20), elems)

  private def createGlyph[
    F[_] : Monad
  ](paraStyle: ParagraphStyle, prev: Option[Character], c: Character, x: Double): LayoutRW[F, Option[Glyph]] =
    for {
      kernAndWidthE <- Glyphs.kerningAndWidth[F](c, prev, paraStyle)
      kernAndWidthO <- EitherT
        .fromEither[LayoutRW[F, *]](kernAndWidthE)
        .map(Some(_))
        .valueOrF(error => log[F](error).as(Option.empty[(Double, Double)]))
      glyph <- kernAndWidthO.traverse { case (k, w) => mkGlyph[F](Box(x + k, 0, w, 0), c) }
    } yield glyph

  private def mkGlyph[
    F[_] : Monad
  ](box: Box, c: Character): LayoutRW[F, Glyph] =
    ask[F, LayoutEnv, List[String], Unit].map { env =>
      val (char, hidden) =
        if (env.settings.showHiddenCharacters)
          c.char match {
            case ' ' => ('·', true)
            case other => (other, false)
          }
        else
          (c.char, false)
      Glyph(box, c.copy(char = char), hidden)
    }

  private def layoutNextChunk[
    F[_] : Monad
  ](flow: Flow,
    dropSpaces: Boolean,
    x: Double,
    maxWidth: Double,
    paraStyle: ParagraphStyle): LayoutRW[F, (Flow, List[LineElement], Flow)] = {
    val (chunk, restAfterChunk) = nextChunk(flow, dropSpaces)
    for {
      elems <- layoutChunk[F](chunk, x, paraStyle)
      endX = elems.lastOption.map(e => e.box.x + e.box.w).getOrElse(x)
      result <-
        if (endX < maxWidth)
          pure[F, LayoutEnv, List[String], Unit, (Flow, List[LineElement], Flow)]((chunk, elems, restAfterChunk))
        else {
          val word = chunk
            .map {
              case Character(c, _) => c
              case _ => "#"
            }
            .mkString
          for {
            hyphenated <- hyphenate[F](word)
            //_ = println(word + " - " + hyphenated.toString)
          } yield ((chunk, elems, restAfterChunk))
        }

    } yield result
  }

  private def hyphenate[
    F[_] : Monad
  ](word: String): LayoutRW[F, List[String]] =
    ask[F, LayoutEnv, List[String], Unit]
      .map { env =>
        // FIXME build only once
        Hyphenator.load("en", env.hyphenationSpec).hyphenate(word.trim)
      }


  private def nextChunk(flow: Flow, dropSpaces: Boolean): (Flow, Flow) = {
    def isSpace: Element => Boolean = {
      case Character(char, _) => char === ' '
      case _ => false
    }

    val space: Option[Element] =
      if (dropSpaces) Option.empty[Element]
      else flow.headOption.filter(isSpace)

    val afterSpaces = flow.dropWhile_(isSpace)

    val (chunk, rest) = calcNextChunk(Nil, afterSpaces)
    (space.toList ::: chunk, rest)
  }

  @tailrec
  private def calcNextChunk(acc: Flow, flow: Flow): (Flow, Flow) =
    flow match {
      case Nil => (acc, Nil)
      case (image: InlineImage) :: tail => (List(image), tail)
      case (p: Paragraph) :: tail => (List(p), tail)
      case (c@Character(char, _)) :: tail =>
        if (char.isWordBreak) (acc, c :: tail)
        else calcNextChunk(acc :+ c, tail)
    }

  private def layoutChunk[
    F[_] : Monad
  ](flow: List[Element],
    startX: Double,
    paraStyle: ParagraphStyle): LayoutRW[F, List[LineElement]] =
    flow
      .foldLeftM((
        List.empty[LineElement], // accumulated line elements
        Option.empty[Character] // previous character (for kerning)
      )) { case ((elems, prev), e) =>
        val x = elems.lastOption.map(e => e.box.x + e.box.w).getOrElse(startX)
        for {
          layoutElementOption <- e match {
            case c: Character =>
              createGlyph[F](paraStyle, prev, c, x)
            case other =>
              log[F]("Unsupported element: " + other.toString).as(None)
          }
        } yield
          layoutElementOption.fold(
            (elems, Option.empty[Character])
          )(
            glyph => (elems :+ glyph, Some(glyph.char))
          )
      }
      .map { case (elems, _) => elems }

  private implicit final class CharSyntax(val c: Char) {

    private lazy val wordBreakChars = List(' ')

    def isWordBreak: Boolean =
      wordBreakChars.contains_(c)

  }

  private def log[F[_] : Applicative](msg: String): LayoutRW[F, Unit] =
    tell[F, LayoutEnv, List[String], Unit](List(msg))

}
