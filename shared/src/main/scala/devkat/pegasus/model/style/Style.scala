package devkat.pegasus.model

package object style {

  import cats.Eq

  final case class ParagraphStyle(fontFamily: Option[String],
                                  fontStyle: Option[String],
                                  fontWeight: Option[Int],
                                  fontSize: Option[Int],
                                  color: Option[String])

  object ParagraphStyle {

    def empty: ParagraphStyle =
      ParagraphStyle(
        fontFamily = None,
        fontStyle = None,
        fontWeight = None,
        fontSize = None,
        color = None
      )

    implicit lazy val equal: Eq[CharacterStyle] = Eq.fromUniversalEquals

  }

  final case class CharacterStyle(fontFamily: Option[String],
                                  fontStyle: Option[String],
                                  fontWeight: Option[Int],
                                  fontSize: Option[Int],
                                  color: Option[String])

  object CharacterStyle {

    def empty: CharacterStyle =
      CharacterStyle(
        fontFamily = None,
        fontStyle = None,
        fontWeight = None,
        fontSize = None,
        color = None
      )

    implicit lazy val equal: Eq[CharacterStyle] = Eq.fromUniversalEquals

  }

}