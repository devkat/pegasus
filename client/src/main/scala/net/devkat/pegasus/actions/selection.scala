package net.devkat.pegasus.actions

import diode.Action
import net.devkat.pegasus.model.{Position, Selection}

case class SetSelection(selection: Selection) extends Action

object ClearSelection extends Action