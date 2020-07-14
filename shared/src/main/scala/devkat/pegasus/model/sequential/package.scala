package devkat.pegasus.model

import devkat.pegasus.model.nested.{Character => NestedCharacter, Element => NestedElement, Flow => NestedFlow}

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

  sealed abstract class InlineElement(style: CharacterStyle) extends Element

  final case class Character(char: Char, style: CharacterStyle) extends InlineElement(style)

  final case class InlineImage(image: String, style: CharacterStyle) extends InlineElement(style)

  final case class Paragraph(style: ParagraphStyle) extends Element

  object Element {

    def fromFlowElement(style: CharacterStyle): NestedElement => Element = {
      case NestedCharacter(c) => Character(c, style)
    }

  }

}
