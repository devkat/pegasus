package devkat.pegasus.view

import devkat.pegasus.model.SeqElement.Glyph
import org.scalajs.dom.svg.TSpan
import scalatags.JsDom
import scalatags.JsDom.all._
import scalatags.JsDom.svgTags._

object GlyphView {

  def specialChar: Char => Option[String] = {
    case ' ' => Some("·")
    case _ => None
  }

  def render(glyph: Glyph): JsDom.TypedTag[TSpan] = {
    val s = specialChar(glyph.char).getOrElse(glyph.char.toString)
    tspan(s)
  }

}
