package devkat.pegasus.examples

import devkat.pegasus.model.nested.{Character, Flow, Paragraph, Span}
import devkat.pegasus.model.style.{CharacterStyle, ParagraphStyle}

object Examples {

  val dummyText: String =
    """Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's
      |standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make
      |a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting,
      |remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing
      |Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions
      |of Lorem Ipsum.""".stripMargin.replace("\n", " ")

  private val defaultStyle =
    ParagraphStyle.empty.copy(
      fontFamily = Some("Times"),
      fontWeight = Some(400),
      fontStyle = Some("normal"),
      fontSize = Some(16)
    )

  def flowFromString(s: String): Flow =
    Flow(
      s.split("\n").map(paraFromString).toList
    )

  def paraFromString(s: String): Paragraph =
    Paragraph(List(Span(s.map(Character.apply).toList, CharacterStyle.empty)), defaultStyle)

}
