package devkat.pegasus.view

import com.raquo.laminar.nodes.ReactiveSvgElement
import devkat.pegasus.model.Element
import org.scalajs.dom.svg.TSpan

object ElementView {

  def render(elem: Element): ReactiveSvgElement[TSpan] =
    elem match {
      case c: Element.Character => CharacterView.render(c)
    }

}
