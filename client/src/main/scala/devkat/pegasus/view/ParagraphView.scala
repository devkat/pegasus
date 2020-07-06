package devkat.pegasus.view

import devkat.pegasus.model.{Element, Paragraph}
import scalatags.JsDom.all._
import scalatags.JsDom.svgAttrs.{SeqFrag =>_, _}
import scalatags.JsDom.svgTags.{SeqFrag =>_, _}

object ParagraphView {

  def render(p: Paragraph, y: Int) = {
    val (lines, h) = renderLines(p.elements, y)
    (g(lines: _*), h)
  }

  private def renderLines(elements: Vector[Element], top: Int) = {
    val groups = elements
      .grouped(80)
      .toSeq

    val elems = groups
      .zipWithIndex
      .map { case (elems, i) =>
        text(
          y := top + (i + 1) * 20,
          //elems.map(ElementView.render)
        )
      }

    (elems, groups.size * 20)
  }

}
