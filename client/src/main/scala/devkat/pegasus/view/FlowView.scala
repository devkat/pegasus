package devkat.pegasus.view

import cats.Id
import devkat.pegasus.layout.Layout
import devkat.pegasus.layout.LayoutElement.Glyph
import devkat.pegasus.model.EditorModel
import diode.ModelRO
import org.scalajs.dom.svg.G
import scalatags.JsDom
import scalatags.JsDom.all._
import scalatags.JsDom.svgAttrs.{SeqFrag => _, _}
import scalatags.JsDom.svgTags.{SeqFrag => _, _}

object FlowView {

  val w = 500

  def render(model: ModelRO[EditorModel]): JsDom.TypedTag[G] = {
    val flow = model.value.flow
    val fonts = model.value.fonts.getOrElse(sys.error("fonts not loaded")) // FIXME
    val (log, _, lines) = Layout[Id](flow, w).run(fonts, ())
    g(
      lines.map(line =>
        text(
          x := line.x,
          y := line.y,
          line.elements.map {
            case Glyph(x, y, c) => tspan(c.char.toString)
          }
        )
      )
    )
  }

}
