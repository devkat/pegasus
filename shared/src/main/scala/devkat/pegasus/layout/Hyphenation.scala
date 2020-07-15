package devkat.pegasus.layout

import cats.{Applicative, Monad}
import cats.data.ReaderWriterStateT.ask
import cats.implicits._
import devkat.pegasus.hyphenation.Hyphenator
import devkat.pegasus.layout.Layout.LayoutRW
import devkat.pegasus.model.sequential
import devkat.pegasus.model.sequential.{Character, Flow}
import fs2.Stream

import scala.util.matching.Regex

object Hyphenation {

  private lazy val letterRegex: Regex = "\\p{L}".r

  private type State = (
    Option[Flow], // remaining flow
      Option[List[String]] // remaining syllables
    )

  def chunkStream[
    F[_] : Monad
  ](chunk: Flow): Stream[LayoutRW[F, *], Flow] = {

    def pure[A](a: A): LayoutRW[F, A] = Applicative[LayoutRW[F, *]].pure(a)

    Stream
      .unfoldEval[LayoutRW[F, *], State, Flow]((Some(chunk), None)) {
        case (Some(chunk), None) =>

          val prefix = chunk.takeWhile {
            case Character(c, _) => !letterRegex.matches(c.toString)
            case _ => true
          }

          if (prefix.length === chunk.length)
            pure(Some(prefix, (None, Some(Nil))))
          else {
            val wordChunk = chunk.drop(prefix.length)

            val word = wordChunk
              .takeWhile {
                case Character(c, _) => letterRegex.matches(c.toString)
                case _ => false
              }
              .map {
                case Character(c, _) => c
                case _ => sys.error("programming error")
              }
              .mkString

            hyphenate[F](word).map {
              case h :: t =>
                val (output, state) = nextOutputAndState(wordChunk, h, t)
                Some((prefix ::: output, state))
              case _ =>
                None
            }
          }

        case (_, Some(Nil)) =>
          pure(None)

        case (Some(chunk), Some(h :: t)) =>
          pure(Some(nextOutputAndState(chunk, h, t)))

      }
  }

  private def nextOutputAndState(flow: Flow,
                                 h: String,
                                 t: List[String]): (Flow, State) =
    if (t.isEmpty) (flow, (None, None))
    else (appendHyphen(flow.take(h.length)), ((Some(flow.drop(h.length)), Some(t))))

  private def appendHyphen: Flow => Flow = {
    case start :+ (last: Character) => start ::: last :: Character('-', last.style) :: Nil
    case other => other
  }

  private def hyphenate[
    F[_] : Monad
  ](word: String): LayoutRW[F, List[String]] =
    ask[F, LayoutEnv, List[String], Unit]
      .map { env =>
        // FIXME build only once
        Hyphenator.load("en", env.hyphenationSpec).hyphenate(word.trim)
      }

}
