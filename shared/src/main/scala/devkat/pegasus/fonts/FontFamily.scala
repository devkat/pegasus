package devkat.pegasus.fonts

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

final case class FontFamily(name: String, fonts: Map[String, Font])

object FontFamily {
  implicit lazy val decoder: Decoder[FontFamily] = deriveDecoder
  implicit lazy val encoder: Encoder[FontFamily] = deriveEncoder
}

/*
sealed trait FontStyle extends EnumEntry

object FontStyle extends Enum[FontStyle] with CirceEnum[FontStyle] {
  case object Regular extends FontStyle
  case object Bold extends FontStyle
  case object Italic extends FontStyle

  private val ordered = Seq(Regular, Bold, Italic)

  implicit lazy val order: Order[FontStyle] =
    Order.by {
      case Regular => 1
      case Bold => 2
      case Italic => 3
    }

  override def values: IndexedSeq[FontStyle] = findValues
}
 */

final case class FontWeight(weight: Int)

object FontWeight {
  implicit lazy val decoder: Decoder[FontWeight] = Decoder[Int].map(FontWeight.apply)
  implicit lazy val encoder: Encoder[FontWeight] = Encoder[Int].contramap(_.weight)
}

final case class Font(weight: FontWeight, blocks: List[Block], kerning: Map[String, Int])

object Font {
  implicit lazy val decoder: Decoder[Font] = deriveDecoder
  implicit lazy val encoder: Encoder[Font] = deriveEncoder
}

final case class Block(start: Int, end: Int, default: Int, chars: Map[String, Int])

object Block {
  implicit lazy val decoder: Decoder[Block] = deriveDecoder
  implicit lazy val encoder: Encoder[Block] = deriveEncoder
}

