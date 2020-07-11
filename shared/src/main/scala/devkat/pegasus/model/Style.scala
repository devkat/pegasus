package devkat.pegasus.model

sealed abstract class StyleAttr[A](value: A)

object StyleAttr {

  case object FontFamily extends StyleAttr
  case object FontStyle extends StyleAttr
  case object FontWeight extends StyleAttr
  case object FontSize extends StyleAttr

}

object Style {
  import StyleAttr._

  class CharacterStyle[K, V]

  object CharacterStyle {
    implicit val fontFamily = new CharacterStyle[FontFamily.type, String]
    implicit val fontStyle = new CharacterStyle[FontStyle.type, String]
    implicit val fontWeight = new CharacterStyle[FontWeight.type, Int]
    implicit val fontSize = new CharacterStyle[FontSize.type, Int]
  }

  class ParagraphStyle[K, V]

  object ParagraphStyle {
    implicit val fontStyle = new ParagraphStyle[FontStyle.type, String]
    implicit val fontFamily = new ParagraphStyle[FontFamily.type, String]
    implicit val fontWeight = new ParagraphStyle[FontWeight.type, Int]
    implicit val fontSize = new ParagraphStyle[FontSize.type, Int]
  }

}