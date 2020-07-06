package devkat.pegasus.view

import devkat.pegasus.model.Section
import org.scalajs.dom.svg.G
import scalatags.JsDom.TypedTag
import scalatags.JsDom.svgAttrs._
import scalatags.JsDom.svgTags._

object SectionView {

  def render(section: Section) = {
    val (_, paras) = section
      .paragraphs
      .foldLeft((0, List.empty[TypedTag[G]])) { case ((y, gs), p) =>
        val (g, h) = ParagraphView.render(p, y)
        (y + h, g :: gs)
      }
    g(paras: _*)
  }


    /*: ReactiveSvgElement[G] = {
    val (_, paras) = section
      .paragraphs
      .foldLeft((0, List.empty[ReactiveSvgElement[G]])) { case ((y, gs), p) =>
        val (g, h) = ParagraphView.render(p, y)
        (y + h, g :: gs)
      }
    svg.g(paras: _*)
  }
     */
}
