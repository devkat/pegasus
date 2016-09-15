package net.devkat.pegasus.model

import java.util.UUID

case class Flow(sections: Seq[Section])

case class Section(id: UUID = UUID.randomUUID(), paragraphs: Seq[Paragraph])

case class Paragraph(id: UUID = UUID.randomUUID(), elements: Seq[Character])

sealed trait Element { val id: UUID }

case class Character(id: UUID = UUID.randomUUID(), ch: Char) extends Element
