package devkat.pegasus.model

import devkat.pegasus.model.style.{CharacterStyle, ParagraphStyle}

package object nested {

  final case class Flow(paragraphs: List[Paragraph])

  final case class Paragraph(spans: List[Span], style: ParagraphStyle)

  final case class Span(elements: List[Element], style: CharacterStyle)

  sealed trait Element

  final case class Character(ch: Char) extends Element

}
