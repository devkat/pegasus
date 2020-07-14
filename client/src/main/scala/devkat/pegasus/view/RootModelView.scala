package devkat.pegasus.view

import devkat.pegasus.Actions.Insert
import devkat.pegasus.AppCircuit
import devkat.pegasus.layout.{LayoutEnv, LayoutSettings}
import devkat.pegasus.model.editor.EditorModel
import diode.data._
import diode.{Dispatcher, ModelRO}
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.KeyboardEvent
import scalatags.JsDom
import scalatags.JsDom.all._
import scalatags.JsDom.svgTags.svg

object RootModelView {

  private lazy val layoutSettings: LayoutSettings =
    LayoutSettings(
      showHiddenCharacters = true
    )

  def render(model: ModelRO[EditorModel], dispatcher: Dispatcher): JsDom.TypedTag[Div] = {
    val status = model.value.status
      .getOrElse(model.value.fonts match {
        case Pending(_) => "Loading fonts …"
        case Ready(_) => "Loaded fonts."
        case Failed(ex) => "Loading fonts failed: " + ex.getMessage
        case _ => ""
      })
    div(
      model.value.fonts.toOption.map(fonts =>
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
            FlowView.render(model.zoom(_.flow), LayoutEnv(fonts, layoutSettings))
          ),
          div(
            fonts.fonts
              .sortBy(f => f.family.value -> f.style.value)
              .map(f => div(f.family.value + " / " + f.style.value + " / " + f.weight.value.toString))
          )
        )
      ),
      div(
        `class` := "status-bar",
        status
      )
    )
  }

}
