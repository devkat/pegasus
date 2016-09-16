package net.devkat.pegasus.model

case class Selection(anchor: Position, focus: Position)

case class Position(section: Int, paragraph: Int, element: Int)