package devkat.pegasus

import devkat.pegasus.model.sequential.Character

package object layout {

  final case class Box(x: Double, y: Double, w: Double, h: Double)

  final case class Line(box: Box, elements: List[LineElement])

  sealed abstract class LineElement(val box: Box)

  final case class Glyph(override val box: Box,
                         char: Character,
                         hidden: Boolean) extends LineElement(box)

}
