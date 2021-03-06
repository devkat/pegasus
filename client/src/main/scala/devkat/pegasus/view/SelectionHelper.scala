package devkat.pegasus.view

import cats.data.NonEmptyList
import cats.implicits._
import devkat.pegasus.layout.{Line, LineElement}

object SelectionHelper {

  def getIndex(layout: List[Line], x: Double, y: Double): Option[Int] = {

    def halfX(e: LineElement) = e.box.x + e.box.w / 2

    for {
      line <- layout.find(line => line.box.y <= y && y <= line.box.y + line.box.h)
      (first, last) = (line.elements.head, line.elements.last)
      result <-
        if (x <= halfX(first)) Some(first)
        else if (halfX(last) < x) Some(last)
        else line.elements.toList.sliding(2, 1).collectFirst {
          case a :: b :: Nil if halfX(a) <= x && x < halfX(b) => b
        }
    } yield result.index
  }

  def getIndexAbove(layout: List[Line], index: Int): Option[Int] =
    getLineAndX(layout, index).flatMap { case (i, x) =>
      if (i === 0)
        layout.get(i).map(_.elements.head.index)
      else
        layout.get(i - 1).map(line => getClosestIndex(line.elements, x))
    }

  def getIndexBelow(layout: List[Line], index: Int): Option[Int] =
    getLineAndX(layout, index).flatMap { case (i, x) =>
      if (i === layout.length - 1)
        layout.get(i).map(_.elements.last.index)
      else
        layout.get(i + 1).map(line => getClosestIndex(line.elements, x))
    }

  private def getLineAndX(layout: List[Line], index: Int): Option[(Int, Double)] =
    layout
      .zipWithIndex
      .collectFirstSome { case (line, i) =>
        line.elements.find(_.index === index).map(e => (i, e.box.x))
      }

  private def getClosestIndex(elements: NonEmptyList[LineElement], x: Double): Int =
    elements.toList.sliding(2, 1)
      .collectFirst {
        case e1 :: e2 :: Nil if x <= (e1.box.x + e2.box.x) / 2 => e1.index
      }
      .getOrElse(elements.last.index)

}
