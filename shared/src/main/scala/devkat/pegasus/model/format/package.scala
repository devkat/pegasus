package devkat.pegasus.model

import devkat.pegasus.model.style.{CharacterStyle, ParagraphStyle}

package object format {

  final case class CharacterFormat(name: String, style: CharacterStyle)

  final case class ParagraphFormat(name: String, style: ParagraphStyle)

}

