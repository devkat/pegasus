package devkat.pegasus.view

import cats.Eval
import cats.implicits._
import devkat.pegasus.Actions.SetCaret
import devkat.pegasus.layout._
import devkat.pegasus.model.CharacterStyle
import devkat.pegasus.model.editor.EditorModel
import devkat.pegasus.view.SelectionView._
import diode.Action
import diode.react.ModelProxy
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.all.svg.{`class` => _, fontFamily => _, fontSize => _, fontWeight => _, svg => _, _}
import japgolly.scalajs.react.vdom.all.{color => _, height => _, width => _, _}
import japgolly.scalajs.react.vdom.{TagOf, VdomElement}
import japgolly.scalajs.react.{BackendScope, Callback, ReactMouseEvent, ScalaComponent}
import org.scalajs.dom.raw.HTMLInputElement
import org.scalajs.dom.svg.TSpan
import org.scalajs.dom.{Element, Node, document}

object FlowView {

  private val w = 600
  private val hiddenCharactersColor = "#999999"

  final case class Props(proxy: ModelProxy[EditorModel],
                         env: LayoutEnv)

  type State = Unit

  class Backend($: BackendScope[Props, State]) {

    private def getIndex(layout: List[Line], x: Double, y: Double): Option[Int] = {

      def halfX(e: LineElement) = e.box.x + e.box.w / 2

      for {
        line <- layout.find(line => line.box.y <= y && y <= line.box.y + line.box.h)
        (first, last) <- (line.elements.headOption, line.elements.lastOption).tupled
        result <-
          if (x <= halfX(first)) Some(first)
          else if (halfX(last) < x) Some(last)
          else line.elements.sliding(2, 1).collectFirst {
            case a :: b :: Nil if halfX(a) <= x && x < halfX(b) => b
          }
      } yield result.index
    }

    def handleClick(layout: List[Line], dispatch: Action => Callback)
                   (e: ReactMouseEvent): Callback =
      e.currentTarget match {
        case target: Element =>
          e.preventDefault()
          e.stopPropagation()
          Option(document.getElementById("pegasus-input")).foreach {
            case e: HTMLInputElement =>
              println(e.toString)
              e.focus()
            case _ => ()
          }
          if (e.button === 0) {
            val r = target.getBoundingClientRect()
            val (x, y) = (e.clientX - r.left, e.clientY - r.top)
            getIndex(layout, x, y).fold(Callback(()))(i => dispatch(SetCaret(i)))
          } else {
            Callback(())
          }
        case _ =>
          Callback(())
      }

    def handleMouseMove(layout: List[Line], dispatch: Action => Callback)
                       (e: ReactMouseEvent): Callback =
      e.currentTarget match {
        case target: Element =>
          if (e.button === 0) {
            val r = target.getBoundingClientRect()
            val (x, y) = (e.clientX - r.left, e.clientY - r.top)
            getIndex(layout, x, y).fold(Callback(()))(i => dispatch(SetCaret(i)))
          } else {
            Callback(())
          }
        case _ =>
          Callback(())
      }

    @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
    def closest[A](n: Node)(f: PartialFunction[Node, A]): Option[A] =
      if (f.isDefinedAt(n)) Some(f(n))
      else Option(n.parentNode).flatMap(closest(_)(f))

    def render(p: Props, s: State): VdomElement = {
      val flow = p.proxy.value.flow
      val selection = p.proxy.value.selection

      val (log, _, lines) = Layout[Eval](flow, w).run(p.env, ()).value

      svg.svg(
        `class` := "pegasus",

        onClick ==> handleClick(lines, p.proxy.dispatchCB),
        //onMouseMove ==> handleMouseMove(lines, p.proxy.dispatchCB),

        selection
          .flatMap(SelectionLayout(_, lines))
          .toTagMod {
            case Caret(box) =>
              rect(
                className := "caret",
                x := box.x.toString,
                y := box.y.toString,
                width := box.w.toString,
                height := box.h.toString,
                stroke := "solid 1px black"
              )
            case Lines(lines) =>
              lines.toTagMod(box =>
                rect(
                  x := box.x.toString,
                  y := box.y.toString,
                  width := box.w.toString,
                  height := box.h.toString,
                  color := "#EEEEEEE"
                )
              )
          },

        lines.toTagMod { line =>
          val (tspans, elems, style) = line.elements.foldLeft((
            List.empty[TagOf[TSpan]],
            List.empty[Char],
            CharacterStyle.empty
          )) { case ((tspans, chars, style), Glyph(_, _, c, hidden)) =>
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
            y := line.box.y + line.box.h,
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

  def apply(proxy: ModelProxy[EditorModel],
            env: LayoutEnv): Unmounted[Props, State, Backend] =
    component(Props(proxy, env))

}
