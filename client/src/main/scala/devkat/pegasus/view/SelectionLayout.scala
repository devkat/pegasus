package devkat.pegasus.view

import cats.implicits._
import devkat.pegasus.layout.{Box, Line, LineElement}
import devkat.pegasus.model.editor.Selection
import devkat.pegasus.view.SelectionView.{Caret, Lines}

sealed trait SelectionView

object SelectionView {

  final case class Caret(box: Box) extends SelectionView

  final case class Lines(lines: List[Box]) extends SelectionView

}

object SelectionLayout {

  def apply(selection: Selection, layout: List[Line]): Option[SelectionView] = {

    def findElement(index: Int): Option[LineElement] =
      layout
        .map(_.elements)
        .collectFirst {
          case line if index <= line.last.index => line
        }
        .flatMap(_.find(_.index === index))

    def findLine(index: Int): Option[(Line, Int)] =
      layout
        .zipWithIndex
        .collectFirst {
          case (line@Line(_, elems, _), i1)
            if (elems.head.index <= index && index <= elems.last.index) => (line, i1)
        }

    if (selection.anchor === selection.focus)
      findElement(selection.anchor)
        .map(_.box.copy(w = 1))
        .map(Caret.apply)
    else {
      val index1 = Math.min(selection.anchor, selection.focus)
      val index2 = Math.max(selection.anchor, selection.focus)
      Tuple2
        .apply(
          findLine(index1),
          findLine(index2),
        )
        .tupled
        .flatMap { case ((l1, i1), (l2, i2)) =>
          Tuple2
            .apply(
              l1.elements.find(_.index === index1),
              l2.elements.find(_.index === index2)
            )
            .mapN { case (e1, e2) =>
              if (i1 === i2)
                List(Box(e1.box.x, l1.box.y, e2.box.x + e2.box.w, l1.box.h))
              else
                (Box(e1.box.x, l1.box.y, l1.box.x + l2.box.w - e1.box.x, l1.box.h) ::
                  layout.slice(i1, i2 - 1).map(_.box)) :+
                  Box(e1.box.x, l1.box.y, l1.box.x + l2.box.w - e1.box.x, l1.box.h)
            }
        }
        .map(Lines.apply)
    }

  }

}
