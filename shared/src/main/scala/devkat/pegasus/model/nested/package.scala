package devkat.pegasus.model

import devkat.pegasus.model.Style.{CharacterStyle, ParagraphStyle}
import shapeless.HMap

package object nested {

  final case class Flow(paragraphs: List[Paragraph])

  final case class Paragraph(spans: List[Span], style: HMap[ParagraphStyle])

  final case class Span(elements: List[Element], style: HMap[CharacterStyle])

  sealed trait Element

  final case class Character(ch: Char) extends Element

}
