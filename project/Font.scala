
final case class FontFamily(name: String)

final case class FontWeight(weight: Int) extends Ordered[FontWeight] {
  def compare(w: FontWeight) = this.weight.compareTo(w.weight)
}

final case class FontStyle(name: String) extends Ordered[FontStyle] {
  def sortKey = name match {
    case "normal" => 0
    case "regular" => 1
    case "italic" => 2
    case "oblique" => 3
  }
  def compare(s: FontStyle) = this.sortKey.compareTo(s.sortKey)
}

final case class Font(family: FontFamily,
                      style: FontStyle,
                      weight: FontWeight,
                      blocks: List[Block],
                      kerning: Map[String, Int]) extends Ordered[Font] {
  def compare(f: Font) = this.family.name.compare(f.family.name) match {
    case 0 => this.style.compare(f.style) match {
      case 0 => this.weight.compare(f.weight)
      case c => c
    }
    case c => c
  }
}

final case class Block(start: Int, end: Int, chars: List[Int])
