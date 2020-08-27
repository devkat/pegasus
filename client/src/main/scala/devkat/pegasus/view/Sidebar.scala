package devkat.pegasus.view

import devkat.pegasus.model.editor.EditorModel
import devkat.pegasus.view.ui.FontFamilyWidget
import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react.{BackendScope, ScalaComponent}

object Sidebar {

  final case class Props(proxy: ModelProxy[EditorModel])

  type State = Unit

  class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State): VdomElement = {
      val model = p.proxy.value
      div(
        `class` := "col-3",
        section(
          h3("Paragraph Format"),
          form(
            FontFamilyWidget(p.proxy, "paragraph")
          )
        ),
        section(
          h3("Character Format")
        )
      )
    }
  }

  private lazy val component =
    ScalaComponent
      .builder[Props]("Sidebar")
      .renderBackend[Backend]
      .build

  def apply(proxy: ModelProxy[EditorModel]): Unmounted[Props, State, Backend] =
    component(Props(proxy))

}
