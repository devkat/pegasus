package devkat.pegasus.layout

import cats.implicits._
import devkat.pegasus.hyphenation.{HyphenationSpec, Hyphenator}
import devkat.pegasus.model.sequential.{Character, Element, Flow, InlineImage, Paragraph}

import scala.annotation.tailrec
import scala.util.matching.Regex

object WordStream {

  //private lazy val isLetter: Predicate[String] = "\\p{L}".r.pattern.asMatchPredicate()
  private lazy val isLetter: Regex = "[a-zA-Z]".r

  private sealed trait State

  // transcoding foo bar baz
  //
  //    Output                         State
  // 1. (transcoding, foo bar baz)     List(trans, co)
  // 2. (transco-, ding foo bar baz)   List(trans)
  // 3. (trans-, coding foo bar baz)   List()

  def apply(flow: Flow, dropSpaces: Boolean, spec: HyphenationSpec): LazyList[(Flow, Flow)] = {

    val (chunk, rest) = nextChunk(flow, dropSpaces)

    def prependSpace(flow: Flow): Flow =
      chunk.space.toList ::: flow

    chunk.content match {

      case ChunkContent.Image(img) =>
        LazyList((prependSpace(List(img)), rest))

      case ChunkContent.Text(chars) =>

        val prefix = chars.takeWhile(c => !isLetter.matches(c.char.toString))
        val noHyphenationResult = LazyList((prependSpace(chars), rest))

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

            LazyList.unfold[(Flow, Flow), State](State.Initial) {

              case State.Initial =>
                Some((prependSpace(chars), rest), State.FullWord)

              case State.FullWord =>

                hyphenate(word, spec) match {

                  case Nil => sys.error("Programming error")

                  case word :: Nil => Some((prependSpace(chars), rest), State.Syllables(Nil))

                  case syllables @ h :: (m :+ t) =>
                    val remainingSyllables = (prefix.map(_.char).mkString + h) :: m
                    val length = remainingSyllables.map(_.length).sum
                    val (output, outputRest) = chars.splitAt(length)
                    Some((prependSpace(appendHyphen(output)), outputRest ::: rest), State.Syllables(remainingSyllables))

                }

              case State.Syllables(Nil) =>
                None

              case State.Syllables(init :+ t) =>
                val length = (init :+ t).map(_.length).sum
                val (output, outputRest) = chars.splitAt(length)
                Some(((prependSpace(appendHyphen(output)), outputRest ::: rest), State.Syllables(init)))

            }

          }
        }

    }

  }

  private final case class Chunk(space: Option[Character], content: ChunkContent)

  private sealed trait ChunkContent

  private object ChunkContent {

    final case class Image(image: InlineImage) extends ChunkContent

    final case class Text(characters: List[Character]) extends ChunkContent

  }

  private def nextChunk(flow: Flow, dropSpaces: Boolean): (Chunk, Flow) = {
    def isSpace: Element => Boolean = {
      case Character(char, _) => char === ' '
      case _ => false
    }

    val space: Option[Character] =
      if (dropSpaces) None
      else flow.headOption.collect {
        case c@Character(' ', _) => c
      }

    val afterSpaces = flow.dropWhile_(isSpace)

    val (content, rest) = calcNextChunk(Nil, afterSpaces)
    (Chunk(space, content), rest)
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
  // FIXME build only once
    Hyphenator.load("en", spec).hyphenate(word.trim)

}
