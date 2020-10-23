package devkat.pegasus.view

import devkat.pegasus.model.editor.EditorModel
import devkat.pegasus.view.Editor.{Theme, styles}
import devkat.pegasus.view.ui.{FontFamilyWidget, FormatWidget}
import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.component.ScalaFn
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.all._
import org.scalablytyped.runtime.StringDictionary
import typings.materialUiCore.components._
import typings.materialUiCore.materialUiCoreStrings.permanent
import typings.materialUiCore.typographyTypographyMod.Style
import typings.materialUiStyles.mod.makeStyles
import typings.materialUiStyles.withStylesMod.{CSSProperties, WithStylesOptions}

import scala.scalajs.js

object Sidebar {

  final case class Props(proxy: ModelProxy[EditorModel])

  val styles: js.Function1[Theme, StringDictionary[CSSProperties]] = theme =>
    StringDictionary(
      "drawer" -> CSSProperties()
        .setWidth(240)
        .setFlexShrink(0),
      "drawerPaper" -> CSSProperties()
        .setWidth(240),
      "drawerContainer" -> CSSProperties()
        .setOverflow("auto")
    )

  object Backend {

    //val styles = makeStyles

    def render(p: Props): VdomElement = {
      val useStyles = makeStyles(styles, WithStylesOptions())
      lazy val classes = useStyles()
      Drawer(
        Toolbar(),
        div(
          className := classes("drawerContainer"),
          List(
            ListItem(
              Typography("Paragraph Format").variant(Style.h6),
            ),
            ListItem(
              FormatWidget(p.proxy, "paragraph"),
            ),
            ListItem(
              FontFamilyWidget(p.proxy, "paragraph"),
            ),
            ListItem(
              Typography("Character Format").variant(Style.h6)
            )
          )
        )
      )
        .variant(permanent)
        .className(classes("drawer"))
    }
  }

  private lazy val component =
    ScalaFnComponent[Props](Backend.render)

  def apply(proxy: ModelProxy[EditorModel]): ScalaFn.Unmounted[Props] =
    component(Props(proxy))

}
