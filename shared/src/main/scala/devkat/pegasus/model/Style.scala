package devkat.pegasus.model

sealed trait StyleAttr

object StyleAttr {

  case object FontFamily extends StyleAttr
  case object FontStyle extends StyleAttr
  case object FontWeight extends StyleAttr
  case object FontSize extends StyleAttr
  case object Color extends StyleAttr

}

object Style {
  import StyleAttr._

  class CharacterStyle[K, V]

  object CharacterStyle {
    implicit val fontFamily: CharacterStyle[FontFamily.type, String] = new CharacterStyle[FontFamily.type, String]
    implicit val fontStyle: CharacterStyle[FontStyle.type, String] = new CharacterStyle[FontStyle.type, String]
    implicit val fontWeight: CharacterStyle[FontWeight.type, Int] = new CharacterStyle[FontWeight.type, Int]
    implicit val fontSize: CharacterStyle[FontSize.type, Int] = new CharacterStyle[FontSize.type, Int]
    implicit val color: CharacterStyle[Color.type, String] = new CharacterStyle[Color.type, String]
  }

  class ParagraphStyle[K, V]

  object ParagraphStyle {
    implicit val fontStyle: ParagraphStyle[FontStyle.type, String] = new ParagraphStyle[FontStyle.type, String]
    implicit val fontFamily: ParagraphStyle[FontFamily.type, String] = new ParagraphStyle[FontFamily.type, String]
    implicit val fontWeight: ParagraphStyle[FontWeight.type, Int] = new ParagraphStyle[FontWeight.type, Int]
    implicit val fontSize: ParagraphStyle[FontSize.type, Int] = new ParagraphStyle[FontSize.type, Int]
    implicit val color: CharacterStyle[Color.type, String] = new CharacterStyle[Color.type, String]
  }

}