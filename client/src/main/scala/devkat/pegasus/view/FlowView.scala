package devkat.pegasus.view

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveSvgElement
import devkat.pegasus.model.Flow
import org.scalajs.dom.svg.G

object FlowView {

  def render(flowSignal: Signal[Flow]): ReactiveSvgElement[G] =
    svg.g(
      children <-- flowSignal.map(_.sections.toSeq).map(_.map(SectionView.render))
    )

}
