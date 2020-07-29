package devkat.pegasus.view

import cats.implicits._
import devkat.pegasus.Actions._
import devkat.pegasus.layout.{LayoutEnv, LayoutSettings}
import devkat.pegasus.model.editor.{EditorModel, Selection}
import diode.Action
import diode.react.ModelProxy
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react.{BackendScope, Callback, ReactKeyboardEventFromInput, ScalaComponent}

object Editor {

  final case class Props(proxy: ModelProxy[EditorModel])

  type State = Unit

  class Backend($: BackendScope[Props, State]) {

    def handleKeyDown(dispatch: Action => Callback,
                      selection: Option[Selection])
                     (e: ReactKeyboardEventFromInput): Callback = {
      //println(e.keyCode.toString)
      //println(e.key.toString)
      e.keyCode match {
        case 8 =>
          selection
            .filter(_.anchor > 0)
            .fold(Callback(()))(s => dispatch(Delete(s.anchor - 1, s.anchor)))
        case _ =>
          e.key.toList match {
            case c :: _ => dispatch(Insert(c))
            case Nil => Callback(())
          }
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
                  onKeyDown ==> handleKeyDown(p.proxy.dispatchCB, model.selection)
                )
              ),
              p.proxy.connect(identity).apply(p => FlowView(p, layoutEnv)),
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
