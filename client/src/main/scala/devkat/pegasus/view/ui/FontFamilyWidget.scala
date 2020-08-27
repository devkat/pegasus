package devkat.pegasus.view.ui

import cats.implicits._

import devkat.pegasus.model.editor.EditorModel
import diode.react.ModelProxy
import japgolly.scalajs.react.{BackendScope, ScalaComponent}
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.all._

object FontFamilyWidget {

  final case class Props(proxy: ModelProxy[EditorModel], scope: String)

  type State = Unit

  class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State): VdomElement = {
      val model = p.proxy.value

      val dataToggle = VdomAttr("data-toggle")

      val fontFamily: String = model.selection
        .flatMap(selection => UiHelper.getCommonCharacterStyle(model.flow, selection, _.style.fontFamily))
        .getOrElse("–")

      div(
        `class` := "form-group",
        label(`for` := p.scope + "_fontFamily", "Font family"),
        div(
          `class` := "dropdown",
          button(
            `type` := "button",
            `class` := "btn btn-secondary dropdown-toggle",
            dataToggle := "dropdown",
            fontFamily
          ),
          div(
            `class` := "dropdown-menu",
            model.fonts.toOption.toTagMod(
              _
                .fonts
                .map(_.family.value)
                .sorted
                .distinct
                .toTagMod(family => a(`class` := "dropdown-item", href := "#", family))
            )
          )
        )
      )
    }
  }

  private lazy val component =
    ScalaComponent
      .builder[Props]("FontWidget")
      .renderBackend[Backend]
      .build

  def apply(proxy: ModelProxy[EditorModel], scope: String): Unmounted[Props, State, Backend] =
    component(Props(proxy, scope))

}
