/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package devkat.pegasus

import devkat.pegasus.fonts.Font
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

package object hyphenation {

  @SuppressWarnings(Array("org.wartremover.warts.All"))
  private[hyphenation] object !:: {
    def unapply(s: String): Option[(Char, String)] =
      if (s == null || s.isEmpty)
        None
      else
        Some(s.head -> s.tail)
  }

  final case class Pattern(chars: String, points: List[Int])

  object Pattern {
    implicit lazy val decoder: Decoder[Pattern] = deriveDecoder
    implicit lazy val encoder: Encoder[Pattern] = deriveEncoder
  }

  final case class HyphenationSpec(patterns: List[Pattern],
                                   exceptions: List[String]) {

    /**
     * To each lower-case exception word it associate the lower-case
     * split word were hyphenations are possible
     */
    lazy val exceptionMap: Map[String, List[String]] =
      (for {
        word <- exceptions
        lower = word.trim.toLowerCase
      } yield lower.replace("-", "") -> lower.split("-").toList).toMap

    /**
     * Whenever a word is not an exception it is looked for hyphenation
     * using the patterns registered in this list
     */
    lazy val patternTrie: StringTrie[List[Int]] =
      patterns.foldLeft(StringTrie.empty[List[Int]]) { case (trie, Pattern(chars, points)) =>
        trie.updated(chars, points)
      }

  }

  object HyphenationSpec {
    implicit lazy val decoder: Decoder[HyphenationSpec] = deriveDecoder
    implicit lazy val encoder: Encoder[HyphenationSpec] = deriveEncoder
  }

}