package devkat.pegasus.model

import cats.Eq
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

  sealed trait Element {

    lazy val isParagraph: Boolean = this match {
      case _: Paragraph => true
      case _ => false
    }

  }

  sealed abstract class InlineElement(val style: CharacterStyle) extends Element

  final case class Character(char: Char, override val style: CharacterStyle) extends InlineElement(style)

  final case class InlineImage(image: String, override val style: CharacterStyle) extends InlineElement(style)

  final case class Paragraph(style: ParagraphStyle) extends Element

  object Element {

    def fromFlowElement(style: CharacterStyle): NestedElement => Element = {
      case NestedCharacter(c) => Character(c, style)
    }

    implicit lazy val eq: Eq[Element] = Eq.fromUniversalEquals

  }

}
