package devkat.pegasus

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

package object fonts {

  final case class FontKey(family: String, style: String)

  type Fonts = Map[FontKey, Font]

  object Fonts {
    implicit lazy val decoder: Decoder[FontFamily] = deriveDecoder
    implicit lazy val encoder: Encoder[FontFamily] = deriveEncoder
  }

  final case class FontFamily(value: String)

  object FontFamily {
    implicit lazy val decoder: Decoder[FontFamily] = Decoder[String].map(FontFamily.apply)
    implicit lazy val encoder: Encoder[FontFamily] = Encoder[String].contramap(_.value)
  }

  final case class FontStyle(value: String)

  object FontStyle {
    implicit lazy val decoder: Decoder[FontStyle] = Decoder[String].map(FontStyle.apply)
    implicit lazy val encoder: Encoder[FontStyle] = Encoder[String].contramap(_.value)
  }

  final case class FontWeight(value: Int)

  object FontWeight {
    implicit lazy val decoder: Decoder[FontWeight] = Decoder[Int].map(FontWeight.apply)
    implicit lazy val encoder: Encoder[FontWeight] = Encoder[Int].contramap(_.value)
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

}
