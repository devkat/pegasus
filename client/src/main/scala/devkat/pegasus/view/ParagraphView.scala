package devkat.pegasus.view

import com.raquo.laminar.api.L.{Element => _, _}
import com.raquo.laminar.nodes.ReactiveSvgElement
import devkat.pegasus.model.{Element, Paragraph}
import org.scalajs.dom.svg.G

object ParagraphView {

  def render(p: Paragraph, y: Int): (ReactiveSvgElement[G], Int) = {
    val (lines, h) = renderLines(p.elements, y)
    (svg.g(lines: _*), h)
  }

  private def renderLines(elements: Vector[Element], y: Int) = {
    val groups = elements
      .grouped(80)
      .toSeq

    val elems = groups
      .zipWithIndex
      .map { case (elems, i) =>
        svg
          .text(
            svg.y := (y + (i + 1) * 20).toString
          )
          .amend(
            elems.map(ElementView.render): _*
          )
      }

    (elems, groups.size * 20)
  }

}
