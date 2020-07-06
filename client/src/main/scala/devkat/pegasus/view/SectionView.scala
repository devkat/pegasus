package devkat.pegasus.view

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveSvgElement
import devkat.pegasus.model.Section
import org.scalajs.dom.svg.G

object SectionView {

  def render(section: Section): ReactiveSvgElement[G] = {
    val (_, paras) = section
      .paragraphs
      .foldLeft((0, List.empty[ReactiveSvgElement[G]])) { case ((y, gs), p) =>
        val (g, h) = ParagraphView.render(p, y)
        (y + h, g :: gs)
      }
    svg.g(paras: _*)
  }
}
