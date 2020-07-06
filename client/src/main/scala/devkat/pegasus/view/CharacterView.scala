package devkat.pegasus.view

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveSvgElement
import devkat.pegasus.model.Element
import org.scalajs.dom.svg.TSpan

object CharacterView {

  def render(char: Element.Character): ReactiveSvgElement[TSpan] =
    svg.tspan(
      char.ch.toString
    )


}
