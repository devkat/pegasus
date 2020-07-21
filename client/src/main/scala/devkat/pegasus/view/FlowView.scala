package devkat.pegasus.view

import cats.Id
import cats.implicits._
import devkat.pegasus.layout.{Glyph, Layout, LayoutEnv}
import devkat.pegasus.model.CharacterStyle
import devkat.pegasus.model.sequential.Flow
import diode.react.ModelProxy
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.{TagOf, VdomElement}
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react.vdom.all.svg.{`class` => _, fontFamily => _, fontSize => _, fontWeight => _, svg => _, _}
import japgolly.scalajs.react.{BackendScope, ScalaComponent}
import org.scalajs.dom.svg.TSpan

object FlowView {

  private val w = 600
  private val hiddenCharactersColor = "#999999"

  final case class Props(proxy: ModelProxy[Flow],
                         env: LayoutEnv)

  type State = Unit

  class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State): VdomElement = {
      val flow = p.proxy.value
      val (log, _, lines) = Layout[Id](flow, w).run(p.env, ())
      log.foreach(println) // FIXME impure
      svg.svg(
        `class` := "pegasus",
        lines.toTagMod { line =>
          val (tspans, elems, style) = line.elements.foldLeft((
            List.empty[TagOf[TSpan]],
            List.empty[Char],
            CharacterStyle.empty
          )) { case ((tspans, chars, style), Glyph(_, c, hidden)) =>
            val overrideStyle = if (hidden) c.style.copy(color = Some(hiddenCharactersColor)) else c.style
            if (overrideStyle === style)
              (tspans, chars :+ c.char, style)
            else
              (tspans :+ mkTspan(style, chars), List(c.char), overrideStyle)
          }

          val allTspans =
            if (elems.isEmpty) tspans
            else tspans :+ mkTspan(style, elems)

          text(
            x := line.elements.map(_.box.x.svgString).mkString(" "),
            y := line.box.y,
            line.style.fontFamily.whenDefined(fontFamily := _),
            line.style.fontStyle.whenDefined(fontStyle := _),
            line.style.fontSize.whenDefined(fontSize := _.toString),
            line.style.fontWeight.whenDefined(fontWeight := _.toString),
            line.style.color.whenDefined(fill := _),
            allTspans.toTagMod
          )
        }
      )
    }

    private def mkTspan(style: CharacterStyle, chars: List[Char]) =
      tspan(
        style.fontFamily.whenDefined(fontFamily := _),
        style.fontStyle.whenDefined(fontStyle := _),
        style.fontSize.whenDefined(fontSize := _.toString),
        style.fontWeight.whenDefined(fontWeight := _.toString),
        style.color.whenDefined(fill := _),
        chars.mkString
      )

  }

  private implicit class DoubleSyntax(val d: Double) extends AnyVal {
    def svgString: String = f"$d%.3f"
  }

  private lazy val component =
    ScalaComponent
      .builder[Props]("StatusBar")
      .renderBackend[Backend]
      .build

  def apply(proxy: ModelProxy[Flow], env: LayoutEnv): Unmounted[Props, State, Backend] =
    component(Props(proxy, env))

}
