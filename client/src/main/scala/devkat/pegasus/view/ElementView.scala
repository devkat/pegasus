package devkat.pegasus.view

import devkat.pegasus.model.EditorModel.Element
import devkat.pegasus.model.EditorModel.Element.{Glyph, Paragraph}

object ElementView {

  def render(elem: Element) =
    elem match {
      case c: Glyph => GlyphView.render(c)
      case p: Paragraph => GlyphView.render(Glyph('¶', Set.empty))
    }

}
