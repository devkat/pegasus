package devkat.pegasus.model

import devkat.pegasus.model.nested.{Character => NestedCharacter, Element => NestedElement, Flow => NestedFlow}
import devkat.pegasus.model.Style.{CharacterStyle, ParagraphStyle}
import devkat.pegasus.model.StyleAttr.FontFamily
import shapeless.HMap

package object sequential {

  type Flow = List[Element]

  object Flow {
    def fromNestedFlow: NestedFlow => Flow =
      _.paragraphs.flatMap(p =>
        Paragraph(p.style) :: p.spans.flatMap(span =>
          span.elements.map(Element.fromFlowElement(span.style))
        )
      )
  }

  sealed trait Element

  sealed abstract class InlineElement(style: HMap[CharacterStyle]) extends Element

  final case class Character(char: Char, style: HMap[CharacterStyle]) extends InlineElement(style)

  final case class InlineImage(image: String, style: HMap[CharacterStyle]) extends InlineElement(style)

  final case class Paragraph(style: HMap[ParagraphStyle]) extends Element

  object Element {

    def fromFlowElement(style: HMap[CharacterStyle]): NestedElement => Element = {
      case NestedCharacter(c) => Character(c, style)
    }

  }

}
