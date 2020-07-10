package devkat.pegasus.view

import devkat.pegasus.Actions.Insert
import devkat.pegasus.AppCircuit
import devkat.pegasus.model.EditorModel
import diode.data._
import diode.{Dispatcher, ModelRO}
import org.scalajs.dom.raw.KeyboardEvent
import scalatags.JsDom.all.{`class` => _, _}
import scalatags.JsDom.svgAttrs.{SeqFrag => _, _}
import scalatags.JsDom.svgTags.{SeqFrag => _, _}

object RootModelView {

  def render(model: ModelRO[EditorModel], dispatcher: Dispatcher) = {
    val status = model.value.status
      .getOrElse(model.value.fonts match {
        case Pending(_) => "Loading fonts …"
        case Ready(_) => "Loaded fonts."
        case Failed(ex) => "Loading fonts failed: " + ex.getMessage
        case _ => ""
      })
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
        FlowView.render(model.zoom(identity))
      ),
      div(
        `class` := "status-bar",
        status
      )
    )
  }

}
