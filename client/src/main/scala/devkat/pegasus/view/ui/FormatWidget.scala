package devkat.pegasus.view.ui

import devkat.pegasus.model.editor.EditorModel
import diode.react.ModelProxy
import japgolly.scalajs.react.{BackendScope, ScalaComponent}
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.all._
import typings.materialUiCore.components._

object FormatWidget {

  final case class Props(proxy: ModelProxy[EditorModel], scope: String)

  type State = Unit

  class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State): VdomElement = {
      val model = p.proxy.value
      FormControl(
        InputLabel("Paragraph Format"),
        Select(
          value := "Heading 1",
          MenuItem("Heading 1"),
          MenuItem("Heading 2")
        )
      ).fullWidth(true)
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
