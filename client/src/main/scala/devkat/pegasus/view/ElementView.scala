package devkat.pegasus.view

import devkat.pegasus.model.EditorModel.Element
import devkat.pegasus.model.EditorModel.Element.{Glyph, ParagraphBreak}

object ElementView {

  def render(elem: Element) =
    elem match {
      case c: Element.Glyph => GlyphView.render(c)
      case ParagraphBreak => GlyphView.render(Glyph('¶'))
    }

}
