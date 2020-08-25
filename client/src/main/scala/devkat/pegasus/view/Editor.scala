package devkat.pegasus.view

import cats.implicits._
import devkat.pegasus.Actions._
import devkat.pegasus.model.editor.EditorModel
import diode.Action
import diode.react.ModelProxy
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react.{BackendScope, Callback, ReactFormEventFromInput, ReactKeyboardEventFromInput, ScalaComponent}

object Editor {

  final case class Props(proxy: ModelProxy[EditorModel])

  type State = Unit

  class Backend($: BackendScope[Props, State]) {

    def handleKeyDown(dispatch: Action => Callback)
                     (e: ReactKeyboardEventFromInput): Callback =
      e.keyCode match {
        case 8 => dispatch(Backspace)
        case 37 => dispatch(MoveCaret(Direction.Left))
        case 38 => dispatch(MoveCaret(Direction.Up))
        case 39 => dispatch(MoveCaret(Direction.Right))
        case 40 => dispatch(MoveCaret(Direction.Down))
        case _ =>
          println(e.keyCode.toString)
          Callback(())
      }

    def handleInput(dispatch: Action => Callback)
                   (e: ReactFormEventFromInput): Callback = {
      val value = e.target.value
      e.target.value = ""
      dispatch(Insert(value))
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
            div(
              `class` := "flex-shrink-0",
              div(
                input(
                  `type` := "text",
                  id := "pegasus-input",
                  onKeyDown ==> handleKeyDown(p.proxy.dispatchCB),
                  onInput ==> handleInput(p.proxy.dispatchCB)
                )
              ),
              p.proxy.connect(identity).apply(p => FlowView(p)),
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

  private lazy val component =
    ScalaComponent
      .builder[Props]("Editor")
      .renderBackend[Backend]
      .build

  def apply(proxy: ModelProxy[EditorModel]): Unmounted[Props, State, Backend] =
    component(Props(proxy))
}
