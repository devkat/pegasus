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
package devkat.pegasus.hyphenation

import scala.annotation.tailrec
import scala.util.Try

/** An hyphenator instance for a given language, with given patterns
 * and given exceptions allows you to get the possible hyphenations
 * of a word.
 * This is an implementation of the [Frank Liang's algorithm](http://www.tug.org/docs/liang/)
 *
 * @author Lucas Satabin
 */
@SuppressWarnings(Array("org.wartremover.warts.All"))
class Hyphenator(language: String, spec: HyphenationSpec, threshold: Int) {

  /** List of exceptions for this hyphenator.
   * To each lower-case exception word it associate the lower-case
   * split word were hyphenations are possible */
  lazy val exceptions: Map[String, List[String]] =
    (for {
      word <- spec.exceptions
      lower = word.trim.toLowerCase
    } yield lower.replace("-", "") -> lower.split("-").toList).toMap

  /** List of patterns for this hyphenator.
   * Whenever a word is not an exception it is looked for hyphenation
   * using the patterns registered in this list */
  lazy val patterns: StringTrie[List[Int]] =
    spec.patterns.foldLeft(StringTrie.empty[List[Int]]) { case (trie, Pattern(chars, points)) =>
      trie.updated(chars, points)
    }

  def hyphenate(word: String): List[String] = {
    val lower = word.toLowerCase
    exceptions.get(lower) match {
      case Some(hyphenized) =>
        // this is an exception, return it as is
        hyphenized
      case None =>
        // ok let's do the real job here, this is no exception
        if (word.length <= threshold) {
          // however if the word is ridicilously small, there is no point in hyphenating it
          List(word)
        } else {
          // we first delimit the word with special delimiter
          val delimited = s".$lower."

          @tailrec
          def loopPoints(word: String, pointIdx: Int, acc: Vector[Int]): Vector[Int] = {

            @tailrec
            def loopWord(word: String, patterns: StringTrie[List[Int]], acc: Vector[Int]): Vector[Int] = {
              // if there is a pattern at this point, compute the new hyphenation points
              val acc1 = patterns.value.fold(acc) { pts =>
                pts.foldLeft((0, acc)) {
                  case ((idx, acc), pt) =>
                    (idx + 1, acc.updated(pointIdx + idx, math.max(acc(pointIdx + idx), pt)))
                }._2
              }
              // and then continue if needed
              if (word == null || word.isEmpty)
                acc1
              else if (patterns.derivableAt(word(0)))
                loopWord(word.tail, patterns.derive(word(0)), acc1)
              else
                acc1
            }

            // update the hyphenation points at this point
            val acc1 = loopWord(word, patterns, acc)

            // and continue if needed
            if (word == null || word.isEmpty)
              acc1
            else
              loopPoints(word.tail, pointIdx + 1, acc1)
          }

          // compute the hyphenation points
          val points = loopPoints(delimited, 0, Vector.fill(delimited.size + 1)(0))
          // no hyphenation can occur between the first two and last two letters
          val hyphenationPoints = List(0) ++ points.slice(3, points.size - 2) ++ List(0, 0)

          //println(word)
          //println(hyphenationPoints)

          @tailrec
          def insertHyphens(word: String, points: List[Int], current: String, acc: List[String]): List[String] = (word, points) match {
            case (null | "", _) =>
              if (current == "")
                acc.reverse
              else
                (current :: acc).reverse
            case (c !:: cs, pt :: pts) =>
              val current1 = current + c
              if (pt % 2 == 0)
                insertHyphens(cs, pts, current1, acc)
              else
                insertHyphens(cs, pts, "", current1 :: acc)
          }

          insertHyphens(word, hyphenationPoints, "", Nil)
        }
    }

  }

}

@SuppressWarnings(Array("org.wartremover.warts.All"))
object Hyphenator {

  val defaultThreshold: Int = 4

  def load(lang: String, spec: HyphenationSpec): Hyphenator =
    new Hyphenator(
      lang,
      spec,
      2)

}
