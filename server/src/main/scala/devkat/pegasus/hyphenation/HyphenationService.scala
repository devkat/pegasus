package devkat.pegasus.hyphenation

import cats.effect.{Resource, Sync}
import cats.implicits._

import scala.io.Source
import scala.util.Try

object HyphenationService {

  private object int {
    def unapply(s: String): Option[Int] =
      Try(s.toInt).toOption
  }

  def apply[
    F[_] : Sync
  ](lang: String): F[HyphenationSpec] = {

    /*
    def load(path: String): F[List[String]] = {
      val acquire = Sync[F].delay(Source.fromResource(path))
      Resource
        .fromAutoCloseable(acquire)
        .use(source => Sync[F].delay(source.getLines().filterNot(_.startsWith("#")).toList))
        .adaptErr(e => new Exception(s"Error loading resource $path", e))
    }
     */

    @SuppressWarnings(Array("org.wartremover.warts.Equals"))
    def load(path: String): F[List[String]] = {
      val is = getClass.getResourceAsStream(path) // FIXME
      Resource
        .fromAutoCloseable(Sync[F].delay(Source.fromInputStream(is)))
        .use(source => Sync[F].delay(source.getLines().filterNot(_.startsWith("#")).toList))
        .adaptErr(e => new Exception(s"Error loading resource $path", e))
    }

    Tuple2
      .apply(
        load(s"/hyphenation/patterns-$lang.txt"),
        load(s"/hyphenation/exceptions-$lang.txt")
      )
      .tupled
      .flatMap { case (patterns, exceptions) =>
        patterns
          .traverse { pattern =>
            // a pattern is a sequence of letters and a sequence of hyphenation points
            // e.g. pattern '.tri1o2n' becomes sequences '.trion' and '000012'
            val trimmed = pattern.trim
            val chars = trimmed.replaceAll("[0-9]", "")
            trimmed
              .split("['\\.\\p{L}]")
              .toList
              .traverse {
                case "" => 0.pure[F]
                case int(i) => i.pure[F]
                case s => Sync[F].raiseError[Int](new Exception(s"Malformed pattern $trimmed. Expected integer, but $s found"))
              }
              .map(Pattern(chars, _))
          }
          .map(HyphenationSpec(_, exceptions))

      }
  }

}
