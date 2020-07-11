package devkat.pegasus.view

import cats.Id
import devkat.pegasus.fonts.Fonts
import devkat.pegasus.layout.Layout
import devkat.pegasus.layout.LineElement.Glyph
import devkat.pegasus.model.EditorModel
import devkat.pegasus.model.sequential.Flow
import diode.ModelRO
import org.scalajs.dom.svg.G
import scalatags.JsDom
import scalatags.JsDom.all._
import scalatags.JsDom.svgAttrs.{SeqFrag => _, _}
import scalatags.JsDom.svgTags.{SeqFrag => _, _}

object FlowView {

  val w = 500

  def render(flow: ModelRO[Flow], fonts: Fonts): JsDom.TypedTag[G] = {
    val (log, _, lines) = Layout[Id](flow.value, w).run(fonts, ())
    log.foreach(println) // FIXME impure
    g(
      lines.map(line =>
        text(
          x := line.elements.map(_.box.x.svgString).mkString(" "),
          y := line.box.y,
          line.elements.map { case Glyph(_, c) => renderChar(c.char) }.mkString
        )
      )
    )
  }

  private def renderChar: Char => Char = {
    case ' ' => '·'
    case other => other
  }

  implicit class DoubleSyntax(val d: Double) extends AnyVal {
    def svgString: String = f"$d%.3f"
  }

}
