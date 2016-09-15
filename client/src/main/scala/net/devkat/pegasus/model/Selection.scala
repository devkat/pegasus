package net.devkat.pegasus.model

import java.util.UUID

case class Selection(anchor: Position, focus: Position)

case class Position(sectionId: UUID, paragraphId: UUID, elementId: UUID)