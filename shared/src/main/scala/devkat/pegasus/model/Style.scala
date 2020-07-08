package devkat.pegasus.model

import shapeless.ops.coproduct.Inject
import shapeless.{:+:, CNil}

sealed abstract class StyleAttr[A](value: A)

object StyleAttr {

  final case class FontFamily(value: String) extends StyleAttr[String](value)

  final case class FontStyle(value: String) extends StyleAttr[String](value)

  final case class FontWeight(value: String) extends StyleAttr[String](value)

}

object Style {
  import StyleAttr._

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


}