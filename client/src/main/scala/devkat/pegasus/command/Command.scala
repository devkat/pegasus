package devkat.pegasus.command

sealed trait Command

object Command {

  final case class Insert(c: Char) extends Command

}
