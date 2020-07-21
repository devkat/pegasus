package devkat.pegasus.layout

import cats.implicits._
import devkat.pegasus.hyphenation.{HyphenationSpec, Hyphenator}
import devkat.pegasus.model.sequential.{Character, Element, Flow, InlineImage, Paragraph}

import scala.annotation.tailrec
import scala.util.matching.Regex

object WordStream {

  //private lazy val isLetter: Predicate[String] = "\\p{L}".r.pattern.asMatchPredicate()
  private lazy val isLetter: Regex = "[a-zA-Z]".r

  // transcoding foo bar baz
  //
  //    Output                         State
  // 1. (transcoding, foo bar baz)     List(trans, co)
  // 2. (transco-, ding foo bar baz)   List(trans)
  // 3. (trans-, coding foo bar baz)   List()

  type Result = (Flow, Option[Character], Flow)

  sealed trait Elem

  final case class Word(elems: List[Element]) extends Elem

  final case class Syllables(syllables: LazyList[Result])

  def apply(flow: Flow, spec: HyphenationSpec): LazyList[(Flow, Option[Character])] = ???

  def syllablesStream(flow: Flow, spec: HyphenationSpec): LazyList[Result] = {

    val (chunk, rest) = nextChunk(flow)

    chunk.content match {

      case ChunkContent.Image(img) =>
        LazyList((List(img), chunk.space, rest))

      case ChunkContent.Text(chars) =>

        val prefix = chars.takeWhile(c => !isLetter.matches(c.char.toString))
        val noHyphenationResult = LazyList((chars, chunk.space, rest))

        if (prefix.length === chars.length)
          noHyphenationResult
        else {
          val wordChars = chars
            .drop(prefix.length)
            .takeWhile(c => isLetter.matches(c.char.toString))

          if (wordChars.isEmpty) {
            noHyphenationResult
          } else {
            val word = wordChars.map(_.char).mkString

            sealed trait State
            object State {

              case object Initial extends State

              case object FullWord extends State

              final case class Syllables(syllables: List[String]) extends State

            }

            def next(syllables: List[String], remainingSyllables: List[String]): Option[(Result, State)] = {
              val length = syllables.map(_.length).sum
              val (output, outputRest) = chars.splitAt(length)
              Some(
                (
                  (
                    appendHyphen(output),
                    None,
                    outputRest ::: chunk.space.toList ::: rest
                  ),
                  State.Syllables(remainingSyllables)
                )
              )
            }

            LazyList.unfold[Result, State](State.Initial) {

              case State.Initial =>
                Some((chars, chunk.space, rest), State.FullWord)

              case State.FullWord =>

                hyphenate(word, spec) match {

                  case Nil => sys.error("Programming error")

                  case word :: Nil => Some((chars, chunk.space, rest), State.Syllables(Nil))

                  case syllables@h :: (m :+ _) =>
                    val remainingSyllables = (prefix.map(_.char).mkString + h) :: m
                    next(remainingSyllables, remainingSyllables)
                }

              case State.Syllables(Nil) =>
                None

              case State.Syllables(init :+ t) =>
                next(init :+ t, init)

            }

          }
        }

    }

  }

  private final case class Chunk(content: ChunkContent, space: Option[Character])

  private sealed trait ChunkContent

  private object ChunkContent {

    final case class Image(image: InlineImage) extends ChunkContent

    final case class Text(characters: List[Character]) extends ChunkContent

  }

  private def nextChunk(flow: Flow): (Chunk, Flow) = {
    val (content, rest) = calcNextChunk(Nil, flow)

    val space: Option[Character] = rest.headOption.collect {
      case c@Character(' ', _) => c
    }

    (Chunk(content, space), rest.dropSpaces)
  }

  @tailrec
  private def calcNextChunk(acc: List[Character], rest: Flow): (ChunkContent, Flow) = {
    import ChunkContent._
    (acc, rest) match {

      // end of flow
      case (acc, Nil) => (Text(acc), Nil)

      // wrap before image
      case (h :: t, (image: InlineImage) :: tail) => (Text(h :: t), image :: tail)

      // wrap after image
      case (Nil, (image: InlineImage) :: tail) => (Image(image), tail)

      // wrap before paragraph break
      case (acc, (p: Paragraph) :: tail) => (Text(acc), p :: tail)

      // wrap at space
      case (acc, (c@Character(' ', _)) :: tail) => (Text(acc), c :: tail)

      // don't wrap
      case (acc, (c: Character) :: tail) => calcNextChunk(acc :+ c, tail)
    }
  }

  private def appendHyphen: Flow => Flow = {
    case start :+ (last: Character) => start ::: last :: last.copy(char = '-') :: Nil
    case other => other
  }

  private def hyphenate(word: String, spec: HyphenationSpec): List[String] =
    Hyphenator.hyphenate(word.trim, spec, Hyphenator.defaultThreshold)

}
