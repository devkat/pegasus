package devkat.pegasus.view

import cats.implicits._
import devkat.pegasus.Actions._
import devkat.pegasus.model.editor.EditorModel
import diode.Action
import diode.react.ModelProxy
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.component.ScalaFn
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react.{BackendScope, Callback, ReactFormEventFromInput, ReactKeyboardEventFromInput, ScalaComponent, ScalaFnComponent}
import org.scalablytyped.runtime.StringDictionary
import typings.materialUiCore.components.Toolbar
import typings.materialUiCore.components._
import typings.materialUiCore.gridGridMod.GridSize._
import typings.materialUiCore.materialUiCoreStrings.fixed
import typings.materialUiCore.typographyTypographyMod.Style
import typings.materialUiCore.zIndexMod.ZIndex
import typings.materialUiStyles.mod.makeStyles
import typings.materialUiStyles.withStylesMod.{CSSProperties, WithStylesOptions}

import scala.scalajs.js

object Editor {

  class Theme(val zIndex: ZIndex) extends js.Object

  val styles: js.Function1[Theme, StringDictionary[CSSProperties]] = theme =>
    StringDictionary(
      "root" -> CSSProperties()
        .setDisplay("flex"),
      "appBar" -> CSSProperties()
        //.setZIndex(theme.zIndex.drawer + 1),
        .setZIndex(1201),
      "drawer" -> CSSProperties()
        .setWidth(240)
        .setFlexShrink(0),
      "drawerPaper" -> CSSProperties()
        .setWidth(240),
      "drawerContainer" -> CSSProperties()
        .setOverflow("auto"),
      "content" -> CSSProperties()
        .setFlexGrow(1)
      //.setPadding(theme.spacing(3)),
    )

  final case class Props(proxy: ModelProxy[EditorModel])

  object Backend {

    def handleKeyDown(dispatch: Action => Callback)
                     (e: ReactKeyboardEventFromInput): Callback = {

      lazy val shift = e.getModifierState("Shift")

      def handleSelection(d: Direction): Action =
        if (shift) ExpandSelection(d) else MoveCaret(d)


      e.keyCode match {
        case 8 => dispatch(Backspace)
        case 37 => dispatch(handleSelection(Direction.Left))
        case 38 => dispatch(handleSelection(Direction.Up))
        case 39 => dispatch(handleSelection(Direction.Right))
        case 40 => dispatch(handleSelection(Direction.Down))
        case _ =>
          println(e.keyCode.toString)
          Callback(())
      }
    }

    def handleInput(dispatch: Action => Callback)
                   (e: ReactFormEventFromInput): Callback = {
      val value = e.target.value
      e.target.value = ""
      dispatch(Insert(value))
    }

    def render(p: Props): VdomElement = {

      // https://gitter.im/ScalablyTyped/community?at=5ec6828b89941d051a13b991
      val useStyles = makeStyles(styles, WithStylesOptions())
      lazy val classes = useStyles()

      div(
        CssBaseline(),
        AppBar(
          Toolbar(
            Typography("Pegasus").variant(Style.h6).noWrap(true)
          )
        )
          .position(fixed)
          .className(classes("appBar")),
        Sidebar(p.proxy),
        Grid(
          div(
            `class` := "pegasus-input-container",
            input(
              `type` := "text",
              id := "pegasus-input",
              onKeyDown ==> handleKeyDown(p.proxy.dispatchCB),
              onInput ==> handleInput(p.proxy.dispatchCB)
            )
          ),
          p.proxy.connect(identity).apply(p => FlowView(p))
        ).xs(`9`).item(true),
        StatusBar(p.proxy)
      )
    }

  }

  private lazy val component =
    ScalaFnComponent[Props](Backend.render)

  def apply(proxy: ModelProxy[EditorModel]): ScalaFn.Unmounted[Props] =
    component(Props(proxy))

  /*
  private lazy val component =
    ScalaComponent
      .builder[Props]("Editor")
      .renderBackend[Backend]
      .build

  def apply(proxy: ModelProxy[EditorModel]): Unmounted[Props, State, Backend] =
    component(Props(proxy))
   */

}
