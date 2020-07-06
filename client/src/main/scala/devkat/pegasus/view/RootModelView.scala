package devkat.pegasus.view

import devkat.pegasus.Actions.Insert
import devkat.pegasus.AppCircuit
import devkat.pegasus.model.EditorModel
import diode.{Dispatcher, ModelRO}
import org.scalajs.dom.raw.KeyboardEvent
import scalatags.JsDom.all.{`class` => _, _}
import scalatags.JsDom.svgAttrs.{SeqFrag => _, _}
import scalatags.JsDom.svgTags.{SeqFrag => _, _}

object RootModelView {

  def render(model: ModelRO[EditorModel], dispatcher: Dispatcher) = {
    div(
      div(
        input(
          tpe := "text",
          onkeypress := { (e: KeyboardEvent) =>
            AppCircuit(Insert(e.keyCode.toChar))
          }
        )
      ),
      svg(
        `class` := "pegasus",
        FlowView.render(model.zoom(_.flow))
      )
    )
  }

}
