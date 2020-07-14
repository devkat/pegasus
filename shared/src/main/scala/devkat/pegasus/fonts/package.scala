package devkat.pegasus

import cats.implicits._
import cats.{Order, Show}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

package object fonts {

  final case class FontKey(family: String, weight: Int, style: String)

  object FontKey {

    implicit lazy val order: Order[FontKey] =
      Order.by(_.show)

    implicit lazy val show: Show[FontKey] =
      Show.show(f => s"${f.family} ${f.weight.toString} ${f.style}")
  }

  final case class Fonts(fonts: List[Font]) {

    private lazy val fontMap: Map[FontKey, Font] = {
      fonts
        .groupByNel(f => FontKey(f.family.value, f.weight.value, f.style.value))
        .view
        .mapValues(_.head)
        .toMap
    }

    def get(key: FontKey): Option[Font] = fontMap.get(key)

  }

  object Fonts {
    implicit lazy val decoder: Decoder[Fonts] = Decoder[List[Font]].map(Fonts.apply)
    implicit lazy val encoder: Encoder[Fonts] = Encoder[List[Font]].contramap(_.fonts)
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

  final case class Font(family: FontFamily,
                        style: FontStyle,
                        weight: FontWeight,
                        blocks: List[Block],
                        kerning: Map[String, Int])

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
