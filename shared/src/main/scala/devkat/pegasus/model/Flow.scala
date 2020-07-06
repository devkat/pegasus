package devkat.pegasus.model

final case class Flow(sections: Vector[Section])

final case class Section(paragraphs: Vector[Paragraph])

final case class Paragraph(elements: Vector[Element])

sealed trait Element

object Element {

  final case class Character(ch: Char) extends Element

}

