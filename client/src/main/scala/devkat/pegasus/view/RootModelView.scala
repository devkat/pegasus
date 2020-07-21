package devkat.pegasus.view

import cats.implicits._
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
      showHiddenCharacters = true,
      hyphenate = true
    )

  def render(model: ModelRO[EditorModel], dispatch: Dispatcher): JsDom.TypedTag[Div] = {
    val status = model.value.status
      .getOrElse(model.value.fonts match {
        case Pending(_) => "Loading fonts …"
        case Ready(_) => "Loaded fonts."
        case Failed(ex) => "Loading fonts failed: " + ex.getMessage
        case _ => ""
      })
    div(
      Tuple2
        .apply(
          model.value.fonts.toOption,
          model.value.hyphenationSpec.toOption
        )
        .mapN { case (fonts, hyphenationSpec) =>
          val layoutEnv =
            LayoutEnv(
              layoutSettings,
              fonts,
              hyphenationSpec
            )
          div(
            div(
              input(
                tpe := "text",
                onkeypress := { (e: KeyboardEvent) =>
                  dispatch(Insert(e.keyCode.toChar))
                }
              )
            ),
            svg(
              `class` := "pegasus",
              FlowView.render(model.zoom(_.flow), layoutEnv)
            ),
            div(
              fonts.fonts
                .sortBy(f => f.family.value -> f.style.value)
                .map(f => div(f.family.value + " / " + f.style.value + " / " + f.weight.value.toString))
            )
          )
        },
      div(
        `class` := "status-bar",
        status
      )
    )
  }

}
