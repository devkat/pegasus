package devkat.pegasus.model

import devkat.pegasus.model.nested.{Character => NestedCharacter, Element => NestedElement, Flow => NestedFlow}
import devkat.pegasus.model.Style.{CharacterStyle, ParagraphStyle}
import devkat.pegasus.model.StyleAttr.FontFamily
import shapeless.HMap

package object sequential {

  type Flow = List[Element]

  object Flow {
    def fromNestedFlow: NestedFlow => Flow =
      _.paragraphs.flatMap(_.spans.flatMap(_.elements.map(Element.fromFlowElement)))
  }

  sealed trait Element

  final case class Character(char: Char, style: HMap[CharacterStyle]) extends Element

  final case class Paragraph(style: HMap[ParagraphStyle]) extends Element

  object Element {

    def fromFlowElement: NestedElement => Element = {
      case NestedCharacter(c) => Character(c, HMap[CharacterStyle](FontFamily -> "Arvo"))
    }

  }

}
