package devkat.pegasus.model

import shapeless.ops.coproduct.Inject
import shapeless.{:+:, CNil}

sealed abstract class StyleAttr[A](value: A)

object StyleAttr {

  /*
  final case class FontFamily(value: String) extends StyleAttr[String](value)

  final case class FontStyle(value: String) extends StyleAttr[String](value)

  final case class FontWeight(value: String) extends StyleAttr[String](value)
  */

  case object FontFamily extends StyleAttr
  case object FontStyle extends StyleAttr
  case object FontWeight extends StyleAttr

}

object Style {
  import StyleAttr._

  class CharacterStyle[K, V]

  object CharacterStyle {
    implicit val fontFamily = new CharacterStyle[FontFamily.type, String]
    implicit val fontStyle = new CharacterStyle[FontStyle.type, String]
    implicit val fontWeight = new CharacterStyle[FontWeight.type, String]
  }

  class ParagraphStyle[K, V]

  object ParagraphStyle {
    implicit val fontFamily = new CharacterStyle[FontFamily.type, String]
    implicit val fontStyle = new CharacterStyle[FontStyle.type, String]
    implicit val fontWeight = new CharacterStyle[FontWeight.type, String]
  }


  /*
  type CharacterStyle = FontFamily :+: FontStyle :+: FontWeight :+: CNil

  object CharacterStyle {
    def apply[A : Inject[CharacterStyle, *]](a: A) =
      Inject[CharacterStyle, A].apply(a)
  }

  type ParagraphStyle = FontFamily :+: FontStyle :+: FontWeight :+: CNil

  object ParagraphStyle {
    def apply[A : Inject[ParagraphStyle, *]](a: A) =
      Inject[ParagraphStyle, A].apply(a)
  }

   */

}