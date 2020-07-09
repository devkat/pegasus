package devkat.pegasus.view

import devkat.pegasus.layout.LayoutElement
import devkat.pegasus.layout.LayoutElement.Glyph
import devkat.pegasus.model.sequential.Flow
import diode.ModelRO
import org.scalajs.dom.svg.{G, Text}
import scalatags.JsDom
import scalatags.JsDom.svgTags._

object FlowView {

  val w = 500

  def render(flow: ModelRO[Flow]): JsDom.TypedTag[G] = {
    g(
      flow.value match {
        case head :: tail => renderElement(head, tail)
        case Nil => Nil
      }
    )
  }

  def renderElement(e: LayoutElement, tail: List[LayoutElement]): List[JsDom.TypedTag[Text]] = {
    e match {
      case g: Glyph => renderGlyph(g)
      case p: ParagraphStart => renderParagraph(p, tail)
    }
    tail.headOption match {
      case Some(head) => renderElement(head, tail.drop(1))
      case None =>
    }
  }

  def renderParagraph(p: ParagraphStart, tail: Seq[SeqElement]) = ???

  def renderGlyph(glyph: Glyph) = ???


}
