package net.devkat.pegasus.model

trait HasChildren {
  type ChildType <: AnyRef
  val children: Vector[ChildType]
}

case class Flow(children: Vector[Section]) extends HasChildren { type ChildType = Section }

case class Section(children: Vector[Paragraph]) extends HasChildren { type ChildType = Paragraph }

case class Paragraph(children: Vector[Element]) extends HasChildren { type ChildType = Element }

sealed trait Element

case class Character(ch: Char) extends Element
