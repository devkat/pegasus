package devkat.pegasus.view

import cats.Id
import cats.implicits._
import devkat.pegasus.layout.{Glyph, Layout, LayoutEnv}
import devkat.pegasus.model.CharacterStyle
import devkat.pegasus.model.sequential.Flow
import diode.ModelRO
import org.scalajs.dom.svg.{G, TSpan}
import scalatags.JsDom
import scalatags.JsDom.all._
import scalatags.JsDom.svgAttrs.{x, y, fill}
import scalatags.JsDom.svgTags.{g, text, tspan}

object FlowView {

  private val w = 600
  private val hiddenCharactersColor = "#999999"

  def render(flow: ModelRO[Flow], env: LayoutEnv): JsDom.TypedTag[G] = {
    val (log, _, lines) = Layout[Id](flow.value, w).run(env, ())
    log.foreach(println) // FIXME impure
    g(
      lines.map { line =>

        val (tspans, elems, style) = line.elements.foldLeft((
          List.empty[JsDom.TypedTag[TSpan]],
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
          SeqFrag(allTspans)
        )
      }
    )
  }

  private def mkTspan(style: CharacterStyle, chars: List[Char]): JsDom.TypedTag[TSpan] =
    tspan(
      style.fontFamily.map(fontFamily := _),
      style.fontStyle.map(fontStyle := _),
      style.fontSize.map(fontSize := _),
      style.fontWeight.map(fontWeight := _),
      style.color.map(fill := _),
      chars.mkString
    )

  implicit class DoubleSyntax(val d: Double) extends AnyVal {
    def svgString: String = f"$d%.3f"
  }

}
