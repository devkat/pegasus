package devkat.pegasus.view

import cats.implicits._
import devkat.pegasus.Actions.Insert
import devkat.pegasus.layout.{LayoutEnv, LayoutSettings}
import devkat.pegasus.model.editor.EditorModel
import diode.Action
import diode.react.ModelProxy
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react.{BackendScope, Callback, ReactKeyboardEventFromInput, ScalaComponent}

object Editor {

  final case class Props(proxy: ModelProxy[EditorModel])

  type State = Unit

  class Backend($: BackendScope[Props, State]) {

    def handleKeyPress(dispatch: Action => Callback)
                      (e: ReactKeyboardEventFromInput): Callback = {
      def clb = Callback(e.target.value = "")
      e.key.toList match {
        case c :: _ => clb >> dispatch(Insert(c))
        case Nil => clb
      }
    }

    def render(p: Props, s: State): VdomElement = {
      val model = p.proxy.value
      div(
        `class` := "container app-container h-100",
        Tuple2
          .apply(
            model.fonts.toOption,
            model.hyphenationSpec.toOption
          )
          .mapN { case (fonts, hyphenationSpec) =>
            val layoutEnv =
              LayoutEnv(
                layoutSettings,
                fonts,
                hyphenationSpec
              )
            div(
              `class` := "flex-shrink-0",
              div(
                input(
                  `type` := "text",
                  onKeyPress ==> handleKeyPress(p.proxy.dispatchCB)
                )
              ),
              p.proxy.connect(_.flow).apply(p => FlowView(p, layoutEnv)),
              div(
                fonts.fonts
                  .sortBy(f => f.family.value -> f.style.value)
                  .toTagMod(f =>
                    div(f.family.value + " / " + f.style.value + " / " + f.weight.value.toString)
                  )
              )
            )
          },
        StatusBar(p.proxy)
      )
    }


  }

  private lazy val layoutSettings: LayoutSettings =
    LayoutSettings(
      showHiddenCharacters = true,
      hyphenate = true
    )

  private lazy val component =
    ScalaComponent
      .builder[Props]("Editor")
      .renderBackend[Backend]
      .build

  def apply(proxy: ModelProxy[EditorModel]): Unmounted[Props, State, Backend] =
    component(Props(proxy))
}
