package devkat.pegasus

import com.raquo.laminar.api.L._
import devkat.pegasus.examples.Lipsum
import devkat.pegasus.model.Flow
import devkat.pegasus.view.FlowView

object Pegasus {

  private val flowVar: Var[Flow] = Var(Flow(Vector.empty))

  flowVar.set(Lipsum.flowFromString(Lipsum.lipsum))

  def render(): HtmlElement =
    div(
      svg.svg(
        svg.cls := "pegasus",
        FlowView.render(flowVar.signal)
      )
    )

}