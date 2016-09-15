package net.devkat.pegasus.actions

import diode.Action
import net.devkat.pegasus.model.{Flow, Selection}

case class UpdateFlow(flow: Flow) extends Action

case class InsertCharacter(selection: Selection, ch: Char) extends Action