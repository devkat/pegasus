package devkat.pegasus.view

import devkat.pegasus.model.editor.EditorModel
import diode.data.{Failed, Pending, Ready}
import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.all._

object StatusBar {

  final case class Props(proxy: ModelProxy[EditorModel])

  type State = Unit

  class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State): VdomElement = {
      val model = p.proxy.value
      val status = model.status
        .getOrElse(model.fonts match {
          case Pending(_) => "Loading fonts …"
          case Ready(_) => "Loaded fonts."
          case Failed(ex) => "Loading fonts failed: " + ex.getMessage
          case _ => ""
        })

      footer(
        `class` := "footer mt-auto py-3",
        div(
          `class` := "container",
          status
        )
      )
    }
  }

  private lazy val component =
    ScalaComponent
      .builder[Props]("StatusBar")
      .renderBackend[Backend]
      .build

  def apply(proxy: ModelProxy[EditorModel]): Unmounted[Props, State, Backend] =
    component(Props(proxy))

}
