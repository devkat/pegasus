package devkat.pegasus

import com.raquo.laminar.api.L._
import devkat.pegasus.command.Command
import devkat.pegasus.command.Command.Insert
import devkat.pegasus.examples.Lipsum
import devkat.pegasus.model.Flow
import devkat.pegasus.view.FlowView

object Pegasus {

  private val flowVar: Var[Flow] = Var(Flow(Vector.empty))

  flowVar.set(Lipsum.flowFromString(Lipsum.lipsum))

  private val commandObserver = Observer[Command] {
    case Insert(c) =>
      flowVar.update(flow => flow.copy(sections = flow.sections.take(1) ++ flow.sections.drop(1)))
  }

  def render(): HtmlElement =
    div(
      div(
        input(
          tpe := "text",
          onKeyPress.map(e => {
            println(e.charCode.toChar);
            Insert(e.charCode.toChar)
          }) --> commandObserver
        )
      ),
      svg.svg(
        svg.cls := "pegasus",
        FlowView.render(flowVar.signal)
      )
    )

}